package FileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import Utilities.ProgramDefinitions;

public class Chunk implements Serializable {
	private static final long serialVersionUID = -5618507391328167972L;
	
	private String fileId;
	private int chunkNum;
	private int replicationDegree;
	private ArrayList<String> peersSaved; // peerId's
	
	private byte[] data;
	
	public Chunk(String chunkFileId, int chunkN, int replicationD) {
		fileId = chunkFileId;
		chunkNum = chunkN;
		replicationDegree = replicationD;
	}
	
	public String getFileId(){
		return fileId;
	}
	
	public int getCurrentReplicationDegree() {
		return peersSaved.size();
	}
	
	public boolean isAboveReplicationDegree(){
		return peersSaved.size() >= replicationDegree;
	}
	
	public void addPeerSaved(String peerId) {
		if(!peersSaved.contains(peerId)) peersSaved.add(peerId);
	}
	
	public void removePeerSaved(String peerId){
		if(peersSaved.contains(peerId))
			peersSaved.remove(peerId);
	}
	
	public void writeChunkFile(){
		File chunkFolder = new File(ProgramDefinitions.mydata.peerID + File.separator + fileId);
		if(!chunkFolder.exists())
			chunkFolder.mkdir();
		
		String filePath = ProgramDefinitions.mydata.peerID + File.separator + fileId
				+ File.separator + fileId + "-" + String.format("%08d", chunkNum);
		
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			if(data != null) fos.write(data);
			else fos.write(("").getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] readChunkFileData() {
		String chunkFilePath = ProgramDefinitions.mydata.peerID + File.separator + fileId
				+ File.separator + fileId + "-" + String.format("%08d", chunkNum);

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

		return null;
	}
	
	public boolean hasData(){
		return data != null;
	}
	
	public byte[] getData(){
		return data;
	}
	
	public void setData(byte[] d){
		data = d;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Chunk)){
	        return false;
	    }else{
	    	Chunk that = (Chunk)obj;
	    	
	    	if(!this.fileId.equals(that.fileId)) return false;
	    	if(this.chunkNum != that.chunkNum) return false;
	    	
	    	return true;
	    }
	}
}