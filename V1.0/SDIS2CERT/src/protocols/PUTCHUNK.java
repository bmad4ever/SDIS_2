package protocols;

import communication.MessageHeader;
import communication.MessagePacket;
import communication.TCP_Client;

public class PUTCHUNK extends TCP_Client{
	private String senderId;
	private String fileId;
	private String chunkNum;
	private int replicationDegree;
	private byte[] chunkData;

	public PUTCHUNK(int p, String a, String senderId, String fileId, String chunkNum, int replicationDegree, byte[] chunkData) {
		super(p,a);
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNum = chunkNum;
		this.replicationDegree = replicationDegree;
		this.chunkData = chunkData;
	}

	@Override
	public void run(){
		System.out.println("# Running PUTCHUNK");
		super.baserun();
		
		if(failed_init) return;
		
		System.out.println("Sending a message");
		MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.putchunk, senderId, fileId, chunkNum, replicationDegree);
		MessagePacket n = new MessagePacket(headMessage, chunkData);
		sendMessage(n);
	}
}