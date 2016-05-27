package protocols;

import java.util.List;

import FileSystem.DatabaseManager;
import Utilities.MessageStamp;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.PeerMetadata;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;

/**
 * This protocol exists when the peer returns to life so that
 * it can delete files that were requested to delete after the
 * peer died
 */
public class REVIVE extends TCP_Client{

	public REVIVE (int p, String a, RefValue<Boolean> taskCompleted) {
		super(p, a, taskCompleted);
	}
	
	/**
	 * Sends a revive message to the control with
	 * this peer id so that the control can send back
	 * a list with files to delete
	 */
	@Override
	public void run () {
		super.baserun();
		if(failed_init)
			return;
		
		//Send message
		MessageHeader header = new MessageHeader(MessageHeader.MessageType.revive, ProgramDefinitions.mydata.peerID);
		byte[] sendingbody =  SerialU.serialize(PeerMetadata.timestamp_table);
		byte[] body = SymmetricKey.encryptData(ProgramDefinitions.mydata.priv_key, sendingbody);
		MessagePacket message = new MessagePacket(header, body);
		sendMessage(message);
		
		// Wait for message...
		MessagePacket response = (MessagePacket)receiveMessage();
		System.out.println("DELETE LIST RECEIVED!!!");
		if(response != null) {
			if(response.header.getMessageType() == MessageHeader.MessageType.superdelete){
				this.taskCompleted.value = true;
				byte[] tmp = SymmetricKey.decryptData(ProgramDefinitions.mydata.priv_key, response.body);
				List<MessageStamp> deleteList = (List<MessageStamp>) SerialU.deserialize(tmp);
				for (int i = 0; i < deleteList.size(); i++) {
					String fileId = deleteList.get(i).fileid;
					DatabaseManager dbm = ProgramDefinitions.db;
					dbm.getDatabase().deleteFile(fileId);
				}
				System.out.println("REVIVE request was successfull");
			}
		}
	}
	
	

}
