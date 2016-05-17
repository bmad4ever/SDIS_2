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
	
	public Chunk(String chunkFileId, int chunkN, int replicationD) {
		fileId = chunkFileId;
		chunkNum = chunkN;
		replicationDegree = replicationD;
		peersSaved = new ArrayList<String>();
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
	
	public synchronized void addPeerSaved(String peerId) {
		if(!peersSaved.contains(peerId)) peersSaved.add(peerId);
	}
	
	public synchronized void removePeerSaved(String peerId){
		if(peersSaved.contains(peerId))
			peersSaved.remove(peerId);
	}
		
	public void writeChunkFile(byte[] newdata){
		File chunkFolder = new File(ProgramDefinitions.myID + File.separator + fileId);
		if(!chunkFolder.exists())
			chunkFolder.mkdir();
		
		String filePath = ProgramDefinitions.myID + File.separator + fileId
				+ File.separator + fileId + "-" + String.format("%08d", chunkNum);
		
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			if(newdata != null) fos.write(newdata);
			else fos.write(("").getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] readChunkFileData() {
		String chunkFilePath = ProgramDefinitions.myID + File.separator + fileId
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
		String filePath = ProgramDefinitions.myID + File.separator + fileId
				+ File.separator + fileId + "-" + String.format("%08d", chunkNum);
		
		File f = new File(filePath);
		return f.exists() && !f.isDirectory();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Chunk)){
	        return false;
	    }else{
	    	Chunk that = (Chunk)obj;
	    	
	    	if( (this.fileId != null && that.fileId == null)
	    	|| (that.fileId != null && this.fileId == null)
	    			) return false;

	    	if(this.fileId!=null&&!this.fileId.equals(that.fileId)) return false;
	    	if(this.chunkNum != that.chunkNum) return false;
	    	return true;
	    }
	}

	@Override
	public int hashCode() {
		return (fileId+chunkNum).hashCode();
	}
	
	public ArrayList<String> getPeersSaved(){
		return peersSaved;
	}
	
	public void print()
	{
		System.out.println(fileId + "," + chunkNum + "," + replicationDegree);
	}
}