package protocols;

import java.util.List;

import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Client;
import communication.messages.DeleteRequestBody;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;

public class REQUESTDEL extends TCP_Client{
	String fileId;
	String fileName;
	List<String> peerIds;

	public REQUESTDEL (int p, String a, String fId, String fName, List<String> pIds, RefValue<Boolean> task) {
		super(p, a, task);
		fileId = fId;
		peerIds = pIds;
		fileName = fName;
	}
	
	@Override
	public void run () {
		super.baserun();
		
		DeleteRequestBody msgBody = new DeleteRequestBody(fileId,peerIds);
		byte[] encMsgBody = SymmetricKey.encryptData(ProgramDefinitions.mydata.priv_key,SerialU.serialize(msgBody));
		MessageHeader header = new MessageHeader(MessageHeader.MessageType.requestdelete, ProgramDefinitions.mydata.peerID);
		MessagePacket message = new MessagePacket(header, encMsgBody);
		sendMessage(message);
		
		MessagePacket response = (MessagePacket) receiveMessage();
		if(response.header.getMessageType() == MessageHeader.MessageType.confirm)
			{
				ProgramDefinitions.db.getDatabase().myOriginalFilesMetadata.remove(fileName);
				if(DEBUG) System.out.println("DELETE request was successfull");
			}
	}
	
}
