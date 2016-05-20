package FileSystem;
import java.io.Serializable;
import java.util.ArrayList;


public class PeerFile implements Serializable {
	private String fileid;
	private int replicationDegree;
	private ArrayList<Chunk> chunks;
	
	public PeerFile (String id,int degree) {
		fileid = id;
		replicationDegree = degree;
		chunks = new ArrayList<>();
	}
	
	public String getFileid() {
		return fileid;
	}
	
	public int getReplicationDegree() {
		return replicationDegree;
	}
	
	/**should work to get num on recover, as long as they are stored in respectve order*/
	public Chunk getChunk(int i) {
		return chunks.get(i);
	}
	
	/*public Chunk getChunkNum(int chunkNum) {
		 for(Chunk chunk: chunks) chunk.getChunkNum();
			 return null;
	}*/
	
	/*public void addChunk(Chunk c) {
		chunks.add(c);
	}*/
	
	public synchronized Chunk addChunk(String chunkFileId, int chunkNum, int replicationDegree) {
		Chunk tempChunk = new Chunk(chunkFileId, chunkNum, replicationDegree);
		if(!chunks.contains(tempChunk)) chunks.add(tempChunk);
		return tempChunk;
	}
	
	public int getNumberOfChunks(){
		return chunks.size();
	}
	
}
