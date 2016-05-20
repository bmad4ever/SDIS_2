package protocols;

import Utilities.RefValue;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.SymmetricKey;

public class GETCHUNK extends TCP_Client{
	
	private static final boolean DEBUG = true;
	
	private String senderId;
	private String fileId;
	private int chunkNum;
	private RefValue<byte[]> received_chunk;

	public GETCHUNK(Utilities.PeerAddress addr, String senderId, String fileId,
			int chunkNum,
			 RefValue<Boolean> accept,RefValue<byte[]> received_chunk) {
		super(addr.port,addr.ip,accept);
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNum = chunkNum;
		this.received_chunk = received_chunk;
	}

	@Override
	public void run(){
		if(DEBUG) System.out.println("# Running GETCHUNK");
		super.baserun();
		
		if(failed_init) return;
		
		if(DEBUG) System.out.println("Sending a GETCHUNK message");
		
		MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.getchunk, senderId, fileId, chunkNum, 0);
		MessagePacket n = new MessagePacket(headMessage, null);
		sendMessage(n);
		
		MessagePacket msg = (MessagePacket) receiveMessage();
		
		//validate received answer
		if(msg==null) 
		{
			if (DEBUG) System.out.println("#GETCHUNK did not receive answer");
			return;
		}
		if(msg.header.getMessageType()!=MessageHeader.MessageType.chunk)
		{
			if (DEBUG) System.out.println("#GETCHUNK did not receive a valid answer");
			return;
		}
		if(msg.body == null || msg.body.length==0)
		{
			if (DEBUG) System.out.println("#GETCHUNK did not receive chunk body");
			return;
		}
		
		//process received chunk
		received_chunk.value = SymmetricKey.decryptData(SymmetricKey.key , msg.body);
		
		this.taskCompleted.value = true;
	}
}