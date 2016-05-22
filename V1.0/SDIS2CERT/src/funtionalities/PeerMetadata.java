package funtionalities;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

import Utilities.MessageStamp;
import Utilities.PeerAddress;
import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import communication.messages.MessageHeader.MessageType;

public class PeerMetadata {

	/*DEV NOTES
	 * peer data can be saved by both the control and the peer with save_peers
	 * own timestamp in ProgramDefinitions is currently only saved by peers in save_timestamps
	 * list of timestaps is saved by both is save_timestamps
	 * list os messagestamps is currently only saved by Control
	 * */
	
	static final public boolean DEBUG=true;

	/**<p>records types and timestamps of received messages for each peer </p>
	 * <p>PEERS will only store the 10 most recent putchunks and deletes</p>
	 * <p>CONTROL will store only delete requests</p>
	 * <p>'Invalid' messages will not be stored ex.: received putchunks after a delete (for the same file) which has higher timestamp</p>
	 * */
	static public Hashtable<String, List<MessageStamp>> message_stamps;
	static final int PEER_max_msg_stamps=10;
	static final List<MessageType> CONTROL_ACCEPTED_STAMPS = Arrays.asList(new MessageType[]{MessageType.requestdelete});
	static final List<MessageType> PEER_ACCEPTED_STAMPS = Arrays.asList(new MessageType[]{MessageType.delete,MessageType.putchunk});

	/**saves higher timestamp received so far for each peer*/
	static public Hashtable<String, Long> timestamp_table;

	/**<p>peers server socket address:port </p>
	 * <p>also stores peer private key ONLY IN CONTROL APP</p>*/
	static public HashSet<PeerData> data;

	static private final Semaphore lock = new Semaphore(1, true);


	static private String peerInfoDatabaseName;
	static private String timestampsDatabaseName;

	static public void setDatabaseNames(String peers, String timestamps)
	{
		peerInfoDatabaseName = peers;
		timestampsDatabaseName = timestamps;
	}


	//[start] SAVE & LOAD DATA

	/**saves peersData hashset only*/
	public static boolean save_peers(){

		//serialize data to a new file (keeps old data so far)
		try (
				OutputStream peerFile = new FileOutputStream(peerInfoDatabaseName+"_new");
				OutputStream buffer = new BufferedOutputStream(peerFile);
				ObjectOutput output = new ObjectOutputStream(buffer);
				){
			try {
				lock.acquire();
				output.writeObject(data);
				lock.release();
			} catch (InterruptedException e) {e.printStackTrace();}
		}  
		catch(IOException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
			return false;
		}

		//if saved ok, then replace old data with new data
		try {
			File peerFile = new File(peerInfoDatabaseName+"_new");
			File peerFile2 = new File(peerInfoDatabaseName);
			peerFile2.delete();
			// Rename file
			boolean success = peerFile.renameTo(peerFile2);
			return success;
		}  		catch(Exception ex){
			return false;
		}

	}

	/**<p>if peer-> saves peers maxtimestamp received list and also own timestamp</p>
	 *<p>if control-> saves peers maxtimestamp received list and also message_stamps list</p> */
	public static boolean save_timestamps(){
		boolean locked = false;
		//serialize data to a new file (keeps old data so far)
		try (
				OutputStream stampFile = new FileOutputStream(timestampsDatabaseName+"_new");
				OutputStream buffer = new BufferedOutputStream(stampFile);
				ObjectOutput output = new ObjectOutputStream(buffer);
				){
			try {
				lock.acquire();
				locked=true;
				Object[] alldata; 
				if (ProgramDefinitions.is_control) alldata  = new Object[]{timestamp_table,message_stamps};
				else alldata = new Object[]{timestamp_table,ProgramDefinitions.timestamp};
				output.writeObject(alldata);
				lock.release();
				locked=false;
			} catch (InterruptedException e) {
				e.printStackTrace();
				if(locked) lock.release();
				}
		}  
		catch(IOException ex){
			//ex.printStackTrace();
			if(locked) lock.release(); //fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
			return false;
		}

		//if saved ok, then replace old data with new data
		try {
			File stampsFile = new File(timestampsDatabaseName+"_new");
			File stampsFile2 = new File(timestampsDatabaseName);
			stampsFile2.delete();			// Rename file
			boolean success = stampsFile.renameTo(stampsFile2);
			return success;
		}  		catch(Exception ex){
			//ex.printStackTrace();
			return false;
		}

	}

	/**loads peersData hashset only*/
	public static boolean load_peers() {
		try(
				InputStream peerFile = new FileInputStream(peerInfoDatabaseName);
				InputStream buffer = new BufferedInputStream(peerFile);
				ObjectInput input = new ObjectInputStream (buffer);
				){
			HashSet<PeerData> recovered_data = (HashSet<PeerData>) input.readObject();
			data=recovered_data;		
			return true;
		}
		catch(ClassNotFoundException ex){
			// fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.", ex);
		}
		catch(IOException ex){
			// fLogger.log(Level.SEVERE, "Cannot perform input.", ex);
		}
		return false;
	}

	/**saves peers maxtimestamp received list and also own timestamp*/
	public static boolean load_timestamps() {
		try(
				InputStream peerFile = new FileInputStream(timestampsDatabaseName);
				InputStream buffer = new BufferedInputStream(peerFile);
				ObjectInput input = new ObjectInputStream (buffer);
				){
			Object[] alldata = (Object[]) input.readObject(); 

			Hashtable<String, Long> recovered_timestamp = (Hashtable<String, Long>) alldata[0];

			if (ProgramDefinitions.is_control) {
				Hashtable<String, List<MessageStamp>> msg_stamps = (Hashtable<String, List<MessageStamp>>) alldata[0];
				message_stamps = msg_stamps;
			}
			else {
				long recovered_own_timestamp = (long) alldata[1];
				ProgramDefinitions.timestamp = recovered_own_timestamp;		
			}

			timestamp_table = recovered_timestamp;

			return true;
		}
		catch(ClassNotFoundException ex){
			// fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.", ex);
		}
		catch(IOException ex){
			// fLogger.log(Level.SEVERE, "Cannot perform input.", ex);
		}
		return false;
	}

	public static boolean exists_peerMetadata_file()
	{
		File f = new File(peerInfoDatabaseName);
		return f.exists() && !f.isDirectory();
	}
	
	public static boolean exists_timestampMetadata_file()
	{
		File f = new File(timestampsDatabaseName);
		return f.exists() && !f.isDirectory();
	}
	
	public static void INIT()
	{
		if(PeerMetadata.exists_peerMetadata_file()) load_peers();
		else data = new HashSet<PeerData>();
		
		if(PeerMetadata.exists_timestampMetadata_file()) load_timestamps();
		else 
		{
			ProgramDefinitions.timestamp = 0;
		}
		if (timestamp_table==null) timestamp_table = new Hashtable<String, Long>();
		if (message_stamps==null) message_stamps = new Hashtable<String, List<MessageStamp>>();
	}
	
	//[end] SAVE & LOAD DATA


	static public void addNewPeerData(PeerData newData){
		try {
			lock.acquire();
			data.remove(newData); 
			data.add(newData);
			if(ProgramDefinitions.is_control) active_peers.put(newData.peerID, peer_renewed_service_this_round);
			lock.release();
		} catch (InterruptedException e) {e.printStackTrace(); lock.release();}
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
		} catch (InterruptedException e) {e.printStackTrace(); lock.release();}
		return null;
	}

	public static void updatePeerData(PeerData original, PeerData newdata)
	{
		try {
			lock.acquire();
			original = newdata;
			lock.release();
		} catch (InterruptedException e) {e.printStackTrace(); lock.release();}
		return;
	}

	public static PeerAddress getPeerAddr(String peerid)
	{
		PeerData pd = getPeerData(peerid);
		if (pd == null) return null;
		return pd.addr;
	}

	/**<p>used by control. removes private keys so that that the data can be sent to other peers.</p>
	 * <p>can also be used by peers to avoid locking data list</p>*/
	public static HashSet<PeerData> getMetadata2send2peer()
	{
		if(data==null) return null;
		HashSet<PeerData> result = new HashSet<PeerData>();
		try {
			lock.acquire();
			for (PeerData peerData : data) {
				PeerData peerDataWithoutKey = new PeerData(null,peerData.addr.ip,peerData.addr.port,peerData.peerID);
				result.add( peerDataWithoutKey);
			}
			lock.release();
		} catch (InterruptedException e) {e.printStackTrace(); lock.release();}
		return result;
	}

	public static List<PeerData> getPeersListRandomlySorted()
	{
		if(data==null) return null;
		try {
			lock.acquire();
			List<PeerData> list = new ArrayList<PeerData>(data);
			long seed = System.nanoTime();
			Collections.shuffle(list, new Random(seed));
			lock.release();
			return list;
		} catch (InterruptedException e) {e.printStackTrace(); lock.release();}
		return null;
	}
	
	public static void setPeerMetadataList(HashSet<PeerData> newData){
		try {
			lock.acquire();
			data = newData;	
			lock.release();
		} catch (InterruptedException e) {e.printStackTrace(); lock.release();}
		return;
	}

	/**returns false if message shouldn't be processed (ex.: delete a file after a putchunk with higher timestamp*/
	public static boolean processStamping(String peerid, MessageStamp stamp)
	{
		MessageStamp stamp_copy = new MessageStamp(); //avoid possible future problem on list (due to references)
		stamp_copy.CopyFrom(stamp);

		//[start] COMMON
		if(DEBUG) System.out.println(peerid);
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

			if(!message_stamps.contains(peerid))  message_stamps.put(peerid, new ArrayList<MessageStamp>());
			List<MessageStamp> stamps = message_stamps.get(peerid);
			if (stamps.size() == 0) {
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
		
		if(DEBUG) if(peerid==null) System.out.println("NULL PEERID ASDFG");
		if(!message_stamps.contains(peerid))  message_stamps.put(peerid, new ArrayList<MessageStamp>());
		List<MessageStamp> stamps = message_stamps.get(peerid);
		if (stamps.size()==0) {//this fills 10 spaces with the same stamp (since this will be "used queue like")
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

	static public void updateActivePeer(String peerid){
		try {
			lock.acquire();
			if(ProgramDefinitions.is_control) active_peers.put(peerid, peer_renewed_service_this_round);
			lock.release();
		} catch (InterruptedException e) {e.printStackTrace();}
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

	public static void printMaxTimeStamps()
	{
		System.out.println("TSV");
		for(String peer:timestamp_table.keySet())
			System.out.println(peer + "(ts)="+timestamp_table.get(peer));
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
			//System.out.println("update_active_peers("+peer+")");
			if(active_peers.get(peer)==peer_renewed_service_this_round) active_peers.replace(peer, peer_renewed_service_last_round);
			else {
				active_peers.replace(peer, peer_seems_unactive);
				if(DEBUG) System.out.println(peer + " expired service");
			}
		}
	}

	public static HashSet<PeerData> getActivePeersData(){
		HashSet<PeerData> active = new HashSet<PeerData>();
		for(String peer : active_peers.keySet())
		{
			if(active_peers.get(peer)!=peer_seems_unactive)
				active.add( getPeerData(peer) );
		}
		return active;
	}

	public static void newPeerService(String peerid)
	{
		if(DEBUG) System.out.println(peerid + " renewed service");
		active_peers.replace(peerid, peer_renewed_service_this_round);
	}

	public static boolean isPeerActive(String peerid)
	{
		if(!active_peers.containsKey(peerid)) return false;
		return active_peers.get(peerid)>0;
	}
	
	//[end] CONTROL ONLY

}
