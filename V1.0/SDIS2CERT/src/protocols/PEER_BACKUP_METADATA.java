package protocols;

import FileSystem.DatabaseManager;
import Utilities.RefValue;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;

public class PEER_BACKUP_METADATA extends TCP_Client{
	
	private static final boolean DEBUG = true;
	
	private DatabaseManager db;
	private String senderId;
	private String fileId;
	//private int chunkNum;
	//private int replicationDegree;
	private byte[] chunkData;

	public PEER_BACKUP_METADATA(int port,String ip, String senderId, String fileId,
			//int chunkNum, int replicationDegree,
			DatabaseManager db,
			 RefValue<Boolean> accept) {
		super(port,ip,accept);
		this.senderId = senderId;
		this.fileId = fileId;
		this.db=db;
	}

	@Override
	public void run(){
		if(DEBUG) System.out.println("# Running BACKUP METADATA");
		super.baserun();
		
		if(failed_init) return;
		
		if(db==null) System.out.println("DBMAN!!!"); 
		if(db.getDatabase()==null) System.out.println("DB!!!");
		if(db.getDatabase().myOriginalFilesMetadata==null) System.out.println("MYORIG!!!");
		chunkData = SymmetricKey.encryptData(SymmetricKey.key, SerialU.serialize(db.getDatabase().myOriginalFilesMetadata));
		//if(true) return;
		
		if(DEBUG) System.out.println("Sending a message");
		MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.peer_backup_metadata, senderId, fileId, 0, 0);
		MessagePacket n = new MessagePacket(headMessage, chunkData);
		sendMessage(n);
		MessagePacket ans = (MessagePacket)receiveMessage();
		if(ans==null) return;
		this.taskCompleted.value =  ans.header.getMessageType()==MessageHeader.MessageType.confirm;
	}
}