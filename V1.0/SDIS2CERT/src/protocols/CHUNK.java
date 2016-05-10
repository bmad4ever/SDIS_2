package protocols;

import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;

public class CHUNK extends TCP_Client{
	private String senderId;
	private String fileId;
	private int chunkNum;
	private byte[] chunkData;

	public CHUNK(int p, String a, String senderId, String fileId, int chunkNum, byte[] data) {
		super(p,a);
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNum = chunkNum;
		this.chunkData = data;
	}

	@Override
	public void run(){
		System.out.println("# Running CHUNK");
		super.baserun();
		
		if(failed_init) return;
		
		System.out.println("Sending a message");
		MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.chunk, senderId, fileId, chunkNum);
		MessagePacket n = new MessagePacket(headMessage, chunkData);
		sendMessage(n);
	}
}