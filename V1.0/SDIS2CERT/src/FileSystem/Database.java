package FileSystem;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Database implements Serializable {
	private static final long serialVersionUID = 2933521614615136756L;

	//private HashMap<String, String> myOriginalFiles; // original file name : fileId generated
	private HashMap<String,PeerFile> myOriginalFilesMetadata;	
	private HashSet<Chunk> storedChunkFiles; // fileId : chunk data

	public Database () {
		//myOriginalFiles = new HashMap<String, String>();
		storedChunkFiles = new HashSet<Chunk>();
		myOriginalFilesMetadata = new HashMap<String,PeerFile>();
	}

	public PeerFile addOriginalFile(String originalFileName,int degree) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(originalFileName.getBytes());
			byte[] mdBytes = md.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < mdBytes.length; i++) {
				hexString.append(Integer.toHexString(0xFF & mdBytes[i]));
			}

			String fileId = hexString.toString();

			if(!myOriginalFilesMetadata.containsKey(originalFileName)){			
				PeerFile pf= new PeerFile(fileId,degree);
				myOriginalFilesMetadata.put(originalFileName,pf);
				return pf;
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public synchronized Chunk addStoredChunkFile(String chunkFileId, int chunkNum, int replicationDegree) {
		Chunk tempChunk = new Chunk(chunkFileId, chunkNum, replicationDegree);
		if(!storedChunkFiles.contains(tempChunk)) storedChunkFiles.add(tempChunk);
		return tempChunk;
	}

	//getters
	/*public String getOriginalFileName(String fileId){
		if(myOriginalFiles.containsValue(fileId)){
			for(Map.Entry<String, String> entry : myOriginalFiles.entrySet()){
				if(entry.getValue().equals(fileId)) return entry.getKey();
			}
		}

		return null;
	}*/

	public String getFileId(String originalFileName) {
		if(myOriginalFilesMetadata.containsKey(originalFileName)) return myOriginalFilesMetadata.get(originalFileName).getFileid();
		return null;
	}

	public PeerFile getFileMetadata(String originalFileName) {
		if(myOriginalFilesMetadata.containsKey(originalFileName)) return myOriginalFilesMetadata.get(originalFileName);
		return null;
	}
	
	public boolean isChunkStored(String chunkFileId, int chunkNum){
		return storedChunkFiles.contains(new Chunk(chunkFileId, chunkNum, -1));
	}

	public Chunk getStoredChunkData(String chunkFileId, int chunkNum){
		if(isChunkStored(chunkFileId, chunkNum)){
			Chunk tempChunk = new Chunk(chunkFileId, chunkNum, -1);
			for(Chunk chunk : storedChunkFiles)
				if(chunk.equals(tempChunk)) return chunk;
		}

		return null;
	}

	public void printChucksStored()
	{
		System.out.println("LIST storedChunkFiles:");
		for(Chunk c : storedChunkFiles) c.print();
	}
	
}