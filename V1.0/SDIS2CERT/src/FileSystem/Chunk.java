package FileSystem;

import java.io.Serializable;
import java.util.ArrayList;

public class Chunk implements Serializable {
	private static final long serialVersionUID = -5618507391328167972L;
	
	private String fileId;
	private int replicationDegree;
	private ArrayList<String> peersSaved; // peerId's
	
	public Chunk(String chunkFileId, int replicationD) {
		fileId = chunkFileId;
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
		peersSaved.add(peerId);
	}
}