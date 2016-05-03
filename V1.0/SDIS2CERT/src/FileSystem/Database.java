package FileSystem;
import java.io.Serializable;
import java.util.HashMap;


public class Database implements Serializable {
	private HashMap<String,PeerFile> myFiles;
	private HashMap<String,PeerFile> peerFiles;
	public HashMap<String, String> testMap;
	
	public Database () {
		myFiles = new HashMap<>();
		peerFiles = new HashMap<>();
		testMap = new HashMap<>();
	}
	
	public void addMyFiles(PeerFile pf) {
		myFiles.put(pf.getName(), pf);
	}
	
	public void addPeerFile(PeerFile pf) {
		peerFiles.put(pf.getName(), pf);
	}
	
	public PeerFile getMyFile(String key) {
		return myFiles.get(key);
	}
	
	public PeerFile getPeerFile(String key) {
		return peerFiles.get(key);
	}
	
	public int getNumMyFiles() {
		return myFiles.size();
	}
	
	public int getNumPeerFiles() {
		return peerFiles.size();
	}
	
	public void printFiles() {
		System.out.println("MY FILES:");
		for (PeerFile file : myFiles.values()) {
			System.out.println(file.getName());
		}
		System.out.println("PEER FILES:");
		for (PeerFile file : peerFiles.values()) {
			System.out.println(file.getName());
		}
	}
	
	public void printTest(){
		System.out.println("TEST:");
		for (String s : testMap.values()) {
			System.out.println(s);
		}
	}
	
}
