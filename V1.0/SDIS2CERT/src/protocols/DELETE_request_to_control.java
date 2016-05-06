package protocols;

import java.util.List;

import Utilities.ProgramDefinitions;
import communication.TCP_Client;
import communication.messages.DeleteRequestBody;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;

public class DELETE_request_to_control extends TCP_Client{

	String FileID;
	List<String> PeerIDs;

	public DELETE_request_to_control(int p, String a, String FileID, List<String> PeerIDs) {
		super(p,a);
		this.FileID = FileID;
		this.PeerIDs = PeerIDs;
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;

		DeleteRequestBody msgBody = new DeleteRequestBody(FileID,PeerIDs);
		byte[] encMsgBody = SymmetricKey.encryptData(ProgramDefinitions.mydata.priv_key,SerialU.serialize(msgBody));

		MessageHeader header = new MessageHeader(MessageHeader.MessageType.requestdelete, ProgramDefinitions.mydata.peerID,null,null,0);
		MessagePacket packet = new MessagePacket(header, encMsgBody);
		sendMessage(packet);
	}
}