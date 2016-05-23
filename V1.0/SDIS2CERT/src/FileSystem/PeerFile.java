package FileSystem;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class PeerFile implements Serializable {
	private String fileId;
	private int replicationDegree;
	private HashMap<Integer, Chunk> chunks;

	public PeerFile (String id,int degree) {
		fileId = id;
		replicationDegree = degree;
		chunks = new HashMap<Integer,Chunk>();
	}

	public String getFileid() {
		return fileId;
	}

	public int getReplicationDegree() {
		return replicationDegree;
	}

	/**should work to get num on recover, as long as they are stored in respective order*/
	public Chunk getChunk(int i) {
		return chunks.get(i);
	}

	public synchronized Chunk addChunk(String chunkFileId, int chunkNum, int replicationDegree) {
		Chunk tempChunk = new Chunk(chunkFileId, chunkNum, replicationDegree);
		if(!chunks.containsKey(chunkNum)) {
			chunks.put(chunkNum, tempChunk);
		}
		return tempChunk;
	}

	public int getNumberOfChunks(){
		return chunks.size();
	}

	public HashSet<String> getPeersWithChunks(){
		HashSet<String> set = new HashSet<String>();
		for(Integer chunkNum : chunks.keySet()){
			List<String> peerList = chunks.get(chunkNum).getPeersSaved();
			for (int i = 0; i < peerList.size(); i++) {
				String pid = peerList.get(i);
				if(!set.contains(pid)) {
					set.add(pid);
				}
			}
		}
		return set;
	}

	public boolean hasChunk(int chunkNum){
		if(chunks.containsKey(chunkNum)){
			return true;
		}else{
			return false;
		}
	}
}