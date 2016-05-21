package protocols;

import java.util.HashMap;

import FileSystem.DatabaseManager;
import FileSystem.PeerFile;
import Utilities.RefValue;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;

public class PEER_RESTORE_METADATA extends TCP_Client{
	
	private static final boolean DEBUG = true;
	
	private String senderId;
	private String fileId;
	DatabaseManager db;

	public PEER_RESTORE_METADATA(int port,String ip, String senderId, String fileId,
			 RefValue<Boolean> accept,
			 DatabaseManager db
			 ) {
		super(port,ip,accept);
		this.senderId = senderId;
		this.fileId = fileId;
		this.db=db;
	}

	@Override
	public void run(){
		if(DEBUG) System.out.println("# Running GETCHUNK");
		super.baserun();
		
		if(failed_init) return;
		
		if(DEBUG) System.out.println("Sending a GETCHUNK message");
		
		MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.peer_restore_metadata, senderId, fileId, 0, 0);
		MessagePacket n = new MessagePacket(headMessage, null);
		sendMessage(n);
		
		MessagePacket msg = (MessagePacket) receiveMessage();
		
		//validate received answer
		if(msg==null) 
		{
			if (DEBUG) System.out.println("#PEER RESTORE METADATA did not receive answer");
			return;
		}
		if(msg.header.getMessageType()!=MessageHeader.MessageType.peer_medatada)
		{
			if (DEBUG) System.out.println("#PEER RESTORE METADATA did not receive a valid answer");
			return;
		}
		if(msg.body == null || msg.body.length==0)
		{
			if (DEBUG) System.out.println("#PEER RESTORE METADATA did not receive metadata body");
			return;
		}
		
		//process received chunk
		//received_chunk.value = 
		try{
		db.getDatabase().joinPeerBackedUpData(
		(HashMap<String,PeerFile>) SerialU.deserialize( SymmetricKey.decryptData(SymmetricKey.key , msg.body) )
		);
		}catch(Exception e){ e.printStackTrace(); return;}
		
		this.taskCompleted.value = true;
	}
}