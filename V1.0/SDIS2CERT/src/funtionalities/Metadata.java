package funtionalities;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
//import java.util.logging.*;
//import java.util.stream.Collectors;

import Utilities.MessageStamp;
import Utilities.PeerAddress;
import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import communication.messages.MessageHeader.MessageType;


public class Metadata implements Runnable{

	static final private boolean DEBUG=true;

	/**<p>records types and timestamps of received messages for each peer </p>
	 * <p>PEERS will only store the 10 most recent putchunks and deletes</p>
	 * <p>CONTROL will store only delete requests</p>
	 * <p>'Invalid' messages will not be stored ex.: received putchunks after a delete (for the same file) which has higher timestamp</p>
	 * */
	static public Hashtable<String, List<MessageStamp>> message_stamps = new Hashtable<String, List<MessageStamp>>();
	static final int PEER_max_msg_stamps=10;
	static final List<MessageType> CONTROL_ACCEPTED_STAMPS = Arrays.asList(new MessageType[]{MessageType.requestdelete});
	static final List<MessageType> PEER_ACCEPTED_STAMPS = Arrays.asList(new MessageType[]{MessageType.delete,MessageType.putchunk});

	/**saves higher timestamp received so far for each peer*/
	static public Hashtable<String, Long> timestamp_table = new Hashtable<String, Long>();

	/**<p>peers server socket address:port </p>
	 * <p>also stores peer private key ONLY IN CONTROL APP</p>*/
	static public List<PeerData> data;

	static private final Semaphore lock = new Semaphore(1, true);

	static final int ind_timestamp_table=0;
	static final int ind_data=1;
	static final int ind_stamps=2;


	/**
	 * @return true if saved everything ok and deleted previous metadata
	 */
	public static boolean save(){

		//serialize data to a new file (keeps old data so far)
		try (
				OutputStream file = new FileOutputStream("new_metadata.ser");
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);
				){
			try {
				lock.acquire();

				Object[] alldata; 
				if (ProgramDefinitions.is_control) alldata  = new Object[]{timestamp_table,data,message_stamps};
				else alldata  =  new Object[]{timestamp_table,data};

				output.writeObject(alldata);
				lock.release();
			} catch (InterruptedException e) {e.printStackTrace();}
		}  
		catch(IOException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
			return false;
		}

		//if saved ok, then replace old data with new data
		try {
			File file = new File("new_metadata.ser");
			File file2 = new File("metadata.ser");
			file2.delete();
			// Rename file
			boolean success = file.renameTo(file2);
			return success;
		}  		catch(Exception ex){
			return false;
		}

	}

	public static void load() {
		try(
				InputStream file = new FileInputStream("metadata.ser");
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream (buffer);
				){
			//deserialize the List

			Object[] alldata = (Object[]) input.readObject(); 

			Hashtable<String, Long> recovered_timestamp = (Hashtable<String, Long>) alldata[ind_timestamp_table];
			List<PeerData> recovered_data = (List<PeerData>) alldata[ind_data];
			
			timestamp_table = recovered_timestamp;
			data=recovered_data;		

			if (ProgramDefinitions.is_control) 
			{
				Hashtable<String, List<MessageStamp>> recovered_stamps = (Hashtable<String, List<MessageStamp>>) alldata[ind_stamps];
				message_stamps = recovered_stamps;
			} 
			else
			{

			}

		}
		catch(ClassNotFoundException ex){
			// fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.", ex);
		}
		catch(IOException ex){
			// fLogger.log(Level.SEVERE, "Cannot perform input.", ex);
		}
	}

	static public void addNewPeerData(PeerData newData){
		try {
			lock.acquire();
			data.add(newData);
			if(ProgramDefinitions.is_control) active_peers.put(newData.peerID, peer_renewed_service_this_round);
			lock.release();
		} catch (InterruptedException e) {e.printStackTrace();}
	}

	public static PeerData getPeerData(String peerid)
	{
		try {
			lock.acquire();
			for (PeerData peerData : data) {
				if(peerData.peerID.equals(peerid)) 
				{
					lock.release();
					return peerData;
				}
			}
			lock.release();
		} catch (InterruptedException e) {e.printStackTrace();}
		return null;
	}

	public static void updatePeerData(PeerData original, PeerData newdata)
	{
		try {
			lock.acquire();
			original = newdata;
			lock.release();
		} catch (InterruptedException e) {e.printStackTrace();}
		return;
	}

	public static PeerAddress getPeerAddr(String peerid)
	{
		PeerData pd = getPeerData(peerid);
		if (pd == null) return null;
		return pd.addr;
	}

	public static boolean exists_metadata_file()
	{
		File f = new File("metadata.ser");
		return f.exists() && !f.isDirectory();
	}

	public static List<PeerData> getMetadata2send2peer()
	{
		if(data==null) return null;
		List<PeerData> result = new ArrayList<PeerData>();
		try {
			lock.acquire();
			for (PeerData peerData : data) {
				PeerData peerDataWithoutKey = new PeerData(null,peerData.addr.ip,peerData.addr.port,peerData.peerID);
				result.add( peerDataWithoutKey);
			}
			lock.release();
		} catch (InterruptedException e) {e.printStackTrace();}
		return result;
	}

	public static void setPeerMetadataList(List<PeerData> newData){
		try {
			lock.acquire();
			data = newData;
			lock.release();
		} catch (InterruptedException e) {e.printStackTrace();}
		return;
	}

	public static boolean processStamping(String peerid, MessageStamp stamp)
	{
		MessageStamp stamp_copy = new MessageStamp(); //avoid possible future problem on list (due to references)
		stamp_copy.CopyFrom(stamp);

		//[start] COMMON
		Long current_timestamp = timestamp_table.get(peerid);
		if(current_timestamp==null) timestamp_table.put(peerid, stamp_copy.timestamp);
		else if(current_timestamp<stamp_copy.timestamp) 
		{
			timestamp_table.replace(peerid,stamp_copy.timestamp);
		}
		else if (current_timestamp==stamp_copy.timestamp) return false;
		//[end] COMMON

		//CONTROL ONLY
		if(ProgramDefinitions.is_control)
		{
			if(! CONTROL_ACCEPTED_STAMPS.contains(stamp_copy.msg) ) return true;

			List<MessageStamp> stamps = message_stamps.get(peerid);
			if (stamps == null) {
				List<MessageStamp> new_stamps = new ArrayList<MessageStamp>();
				new_stamps.add(stamp_copy);
				message_stamps.put(peerid, new_stamps);
				return true;
			}
			//else
			stamps.add(stamp_copy);
			
			/*note on sorting
			 * this could be heavy if receiving lots of requests
			 * but java does not offer a SortedList implementation
			 * */
			Collections.sort(stamps);
			
			return true;
		}

		//[start] PEER ONLY
		if(! PEER_ACCEPTED_STAMPS.contains(stamp_copy.msg) ) return true;
	
		List<MessageStamp> stamps = message_stamps.get(peerid);
		if (stamps == null) {
			List<MessageStamp> new_stamps = new ArrayList<MessageStamp>();
			for(int i=0;i<PEER_max_msg_stamps;++i) new_stamps.add(stamp_copy);
			message_stamps.put(peerid, new_stamps);
			return true;
		}
		//else 
		
		if(!peerCheckValidStamp(stamps,stamp_copy)) return false;
		
		//simulate a circular fifo queue
		for(int i=PEER_max_msg_stamps-1;i>0;--i)
		{
			MessageStamp st = stamps.get(i);
			MessageStamp prevSt = stamps.get(i-1);
			st.CopyFrom(prevSt);
		}
		MessageStamp st = stamps.get(0);
		st.CopyFrom(stamp_copy);
		//[end] PEER ONLY
		return true;
	}
	
	public static void printData()
	{
		if (data != null)
			for (PeerData peerData : data) {
				System.out.println(peerData.toString());
			}
		else
			System.out.println("Data is empty");
	}

	
	
	
	
	static boolean stop=false;
	public static void STOP(){stop=true;}
	/**time between nonvolatile metadata updates in milliseconds*/
	static final int wait_time = 10000;
	@Override
	public void run() {
		while(!stop)
		{
			try {
				Thread.sleep(wait_time);
				save();
				if (ProgramDefinitions.is_control) update_active_peers();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	
	
	
	
	
	//[start] PEER ONLY
	
	static private boolean peerCheckValidStamp(List<MessageStamp> stamps, MessageStamp new_stamp)
	{
		for(int i=0;i<PEER_max_msg_stamps;++i)
		{
			MessageStamp stamp = stamps.get(i);
			if(stamp.timestamp>new_stamp.timestamp
			&& stamp.fileid == new_stamp.fileid
			&& (
			stamp.msg == MessageType.putchunk && new_stamp.msg == MessageType.delete
			||
			stamp.msg == MessageType.delete && new_stamp.msg == MessageType.putchunk
			)
			) return false;
		}
		
		return true;
	}
	
	//[end] PEER ONLY

	//[start] CONTROL ONLY

	/**<p>stores if between a run iteration the control has received a hello (for each peer)></p> 
	 * ONLY USED BY CONTROL*/
	static public Hashtable<String, Integer> active_peers = new Hashtable<String, Integer>();
	static final int peer_renewed_service_this_round = 2;
	static final int peer_renewed_service_last_round = 1;
	static final int peer_seems_unactive = 0;


	static void update_active_peers(){
		for(String peer : active_peers.keySet())
		{
			if(active_peers.get(peer)==peer_renewed_service_this_round) active_peers.replace(peer, peer_renewed_service_last_round);
			else {
				active_peers.replace(peer, peer_seems_unactive);
				if(DEBUG) System.out.println(peer + " expired service");
			}
		}
	}

	public static List<PeerData> getActivePeersData(){
		List<PeerData> active = new ArrayList<PeerData>();
		for(String peer : active_peers.keySet())
		{
			if(active_peers.get(peer)!=peer_seems_unactive)  active.add( getPeerData(peer) );   
		}
		return active;
	}

	public static void newPeerService(String peerid)
	{
		if(DEBUG) System.out.println(peerid + " renewed service");
		active_peers.replace(peerid, peer_renewed_service_this_round);
	}

	//[end] CONTROL ONLY
	
}
