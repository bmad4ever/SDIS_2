package FileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import Utilities.ProgramDefinitions;

public class Database implements Serializable {
	private static final long serialVersionUID = 2933521614615136756L;

	public HashMap<String, PeerFile> myOriginalFilesMetadata;	
	public HashMap<String, PeerFile> storedFiles;

	public Database () {
		storedFiles = new HashMap<String,PeerFile>();
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

	public synchronized boolean addStoredChunkFile(String fileId, int chunkNum, int replicationDegree, byte[] data) {
		File fileFolder = new File(ProgramDefinitions.myID + File.separator + fileId);
		
		//Creates folder of the file if it doesn't exist
		if(!fileFolder.exists()) {
			fileFolder.mkdir();
		}
		String filePath = fileFolder.getPath() + File.separator + fileId + "-" + String.format("%08d", chunkNum);
		
		//Write data into chunk file
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			if(data != null) {
				fos.write(data);
			}
			else {
				fos.write(("").getBytes());
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Save meta data of the chunk file in the database
		if(!storedFiles.containsKey(fileId)) {
			PeerFile pf = new PeerFile(fileId, replicationDegree);
			storedFiles.put(fileId, pf);
		}
		storedFiles.get(fileId).addChunk(fileId, chunkNum, replicationDegree);
		return true;
	}

	//getters
	public String getFileId(String originalFileName) {
		if(myOriginalFilesMetadata.containsKey(originalFileName)) return myOriginalFilesMetadata.get(originalFileName).getFileid();
		return null;
	}

	public PeerFile getFileMetadata(String originalFileName) {
		if(myOriginalFilesMetadata.containsKey(originalFileName)) return myOriginalFilesMetadata.get(originalFileName);
		return null;
	}
	
	public boolean isFileStored(String fileId) {
		return storedFiles.containsKey(fileId);
	}
	
	public boolean isChunkStored(String fileId, int chunkNum) {
		PeerFile pf = storedFiles.get(fileId);
		if(pf != null) {
			if(pf.hasChunk(chunkNum)){
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}

	public byte[] getStoredChunkData(String fileId, int chunkNum) {
		if(isChunkStored(fileId, chunkNum)) {
			String chunkFilePath = ProgramDefinitions.myID + File.separator + fileId + File.separator + fileId + "-" + String.format("%08d", chunkNum);
			try {
				File chunkFile = new File(chunkFilePath);
				FileInputStream fis = new FileInputStream(chunkFile);
				byte[] data = new byte[(int) chunkFile.length()];
				fis.read(data);
				fis.close();
				return data;
			} catch (IOException e) {
				e.printStackTrace();
				e.getCause();
			}
		}
		return null;
	}
	
	/**
	 * Deletes all the chunk files and folder
	 * associated with this fileId and removes
	 * those references from the database
	 * @param fileID The file identifier
	 */
	public boolean deleteFile(String fileId){
		String folderPath = ProgramDefinitions.myID + File.separator + fileId;
		File folder = new File(folderPath);
		if(folder.exists()){
			File[] files = folder.listFiles();
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
			folder.delete();
			return true;
		}
		return false;
		//TODO: Delete in metadata
	}
	
	
	//ONLINE BACKUP RELATED
	public void joinPeerBackedUpData(HashMap<String,PeerFile> backedupData)
	{
		for(String file: backedupData.keySet())
		{
			if(!myOriginalFilesMetadata.containsKey(file))
			myOriginalFilesMetadata.put(file, backedupData.get(file));
		}
	}
	
}