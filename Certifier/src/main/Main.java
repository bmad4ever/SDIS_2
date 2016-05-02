package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
		ArrayList<Peer> onlinePeers = new ArrayList<>();
		onlinePeers.add(new Peer("atuamae"));
		onlinePeers.add(new Peer("atuatia"));
		onlinePeers.add(new Peer("queberravaquandofugia"));
		
		peersSaveFile(onlinePeers);
	}
	
	public static void peersSaveFile(ArrayList<Peer> onlinePeers){
		String logFileName = "log.txt";
		
		try {
			FileOutputStream fos = new FileOutputStream(logFileName);
			
			for(Peer peer : onlinePeers){
				fos.write(peer.getPeerId());
				fos.write(peer.getPublicKey());
				fos.write(peer.getPrivateKey());
				
				fos.write(("\n\n").getBytes());
			}
			
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
