package funtionalities;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
//import java.util.logging.*;
//import java.util.stream.Collectors;

import Utilities.PeerAddress;
import Utilities.PeerData;

public class Metadata implements Runnable{

	static public List<PeerData> data;
	static private final Semaphore lock = new Semaphore(1, true);
	
	
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
			List<PeerData> recoveredData = (List<PeerData>) input.readObject();
			data = recoveredData;
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
	void STOP(){stop=true;}
	/**time between nonvolatile metadata updates in miliseconds*/
	static final int wait_time = 10000;
	@Override
	public void run() {
				while(!stop)
				{
					try {
						Thread.sleep(wait_time);
						save();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		
	}
	
	
	
	

}
