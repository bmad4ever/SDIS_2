package FileSystem;
import java.io.Serializable;
import java.util.ArrayList;


public class Chunk implements Serializable {
	private ArrayList<String> peersSaved;
	private int num;
	
	public Chunk (int n) {
		num = n;
		peersSaved = new ArrayList<>();
	}
	
	public int getReplicationDegree() {
		return peersSaved.size();
	}
	
	public int getNum() {
		return num;
	}
	
	public void addPeerSaved(String id) {
		peersSaved.add(id);
	}
	
}
