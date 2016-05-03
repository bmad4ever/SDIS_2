package funtionalities;

import java.io.*;
import java.util.*;
//import java.util.logging.*;
//import java.util.stream.Collectors;

public class Metadata {

	static public List<PeerData> data;

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
			output.writeObject(data);
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

	public static PeerData getPeerData(String peerid)
	{
		for (PeerData peerData : data) {
			if(peerData.peerID.equals(peerid)) return peerData;
		}
		return null;
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
		return !f.exists() && !f.isDirectory();
	}
}
