package FileSystem;
import java.io.Serializable;
import java.util.ArrayList;


public class PeerFile implements Serializable {
	private String name;
	private int replicationDegree;
	private ArrayList<Chunk> chunks;
	
	public PeerFile (String n, int degree) {
		name = n;
		replicationDegree = degree;
		chunks = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}
	
	public int getReplicationDegree() {
		return replicationDegree;
	}
	
	public Chunk getChunk(int i) {
		return chunks.get(i);
	}
	
	public void addChunk(Chunk c) {
		chunks.add(c);
	}
	
	public int getNumberOfChunks(){
		return chunks.size();
	}
	
}
