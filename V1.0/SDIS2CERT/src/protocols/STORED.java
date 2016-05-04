package protocols;

import communication.MessageHeader;
import communication.MessagePacket;
import communication.TCP_Client;

public class STORED extends TCP_Client{
	private String senderId;
	private String fileId;
	private String chunkNum;

	public STORED(int p, String a, String senderId, String fileId, String chunkNum) {
		super(p,a);
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNum = chunkNum;
	}

	@Override
	public void run(){
		System.out.println("# Running STORED");
		super.baserun();
		
		if(failed_init) return;
		
		System.out.println("Sending a message");
		MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.stored, senderId, fileId, chunkNum);
		MessagePacket n = new MessagePacket(headMessage, null);
		sendMessage(n);
	}
}