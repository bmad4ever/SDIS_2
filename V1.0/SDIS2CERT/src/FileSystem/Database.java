package FileSystem;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Database implements Serializable {
	private static final long serialVersionUID = 2933521614615136756L;
	
	private HashMap<String, String> myOriginalFiles; // original file name : fileId generated
	private HashMap<String, Chunk> storedChunkFiles; // fileId : chunk data
	
	public Database () {
		myOriginalFiles = new HashMap<>();
		storedChunkFiles = new HashMap<>();
	}
	
	public void addOriginalFile(String originalFileName) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(originalFileName.getBytes());
			byte[] mdBytes = md.digest();
			
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < mdBytes.length; i++) {
				hexString.append(Integer.toHexString(0xFF & mdBytes[i]));
			}

			String fileId = hexString.toString();
			
			if(!myOriginalFiles.containsKey(originalFileName))
				myOriginalFiles.put(originalFileName, fileId);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public void addStoredChunkFile(String chunkFileId, int replicationDegree) {
		storedChunkFiles.put(chunkFileId, new Chunk(chunkFileId, replicationDegree));
	}
	
	//getters
	public String getOriginalFileName(String fileId){
		if(myOriginalFiles.containsValue(fileId)){
			for(Map.Entry<String, String> entry : myOriginalFiles.entrySet()){
				if(entry.getValue().equals(fileId)) return entry.getKey();
			}
		}
		
		return null;
	}
	
	public String getFileId(String originalFileName) {
		if(myOriginalFiles.containsKey(originalFileName)) return myOriginalFiles.get(originalFileName);
		
		return null;
	}
	
	public boolean isChunkStored(String chunkFileId){
		return storedChunkFiles.containsKey(chunkFileId);
	}
	
	public Chunk getStoredChunkData(String chunkFileId){
		if(storedChunkFiles.containsKey(chunkFileId)) return storedChunkFiles.get(chunkFileId);
		
		return null;
	}
}