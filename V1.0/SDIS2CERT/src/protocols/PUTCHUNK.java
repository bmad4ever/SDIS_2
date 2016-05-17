package protocols;

import Utilities.RefValue;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;

public class PUTCHUNK extends TCP_Client{
	
	private static final boolean DEBUG = true;
	
	private String senderId;
	private String fileId;
	private int chunkNum;
	private int replicationDegree;
	private byte[] chunkData;

	public PUTCHUNK(Utilities.PeerAddress addr, String senderId, String fileId,
			int chunkNum, int replicationDegree, byte[] chunkData,
			 RefValue<Boolean> accept) {
		super(addr.port,addr.ip,accept);
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNum = chunkNum;
		this.replicationDegree = replicationDegree;
		this.chunkData = chunkData;
	}

	@Override
	public void run(){
		if(DEBUG) System.out.println("# Running PUTCHUNK");
		super.baserun();
		
		if(failed_init) return;
		
		if(DEBUG) System.out.println("Sending a message");
		MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.putchunk, senderId, fileId, chunkNum, replicationDegree);
		MessagePacket n = new MessagePacket(headMessage, chunkData);
		sendMessage(n);
		MessagePacket ans = (MessagePacket)receiveMessage();
		if(ans==null) return;
		this.taskCompleted.value =  ans.header.getMessageType()==MessageHeader.MessageType.stored;
	}
}