package main;

import java.security.KeyPair;
import java.util.Timer;
import java.util.TimerTask;

import utils.AsymmetricKey;

public class Peer {
	private final String peerId;
	private final byte[] peerPublicKey;
	private final byte[] peerPrivateKey;
	
	private long loggedTime;
	
	private Timer timer;
	
	public Peer(String id){
		peerId = id;
		
		KeyPair kp = AsymmetricKey.generateKeyPair();
		peerPublicKey = kp.getPublic().getEncoded();
		peerPrivateKey = kp.getPrivate().getEncoded();
		
		loggedTime = 0;
		
		/*timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				loggedTime++;
				//System.out.println(loggedTime);
			}
		}, 0, 1000);*/
	}

	public byte[] getPeerId(){
		return peerId.getBytes();
	}
	
	public byte[] getPublicKey(){
		return peerPublicKey;
	}
	
	public byte[] getPrivateKey(){
		return peerPrivateKey;
	}
}