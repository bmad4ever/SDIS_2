package protocols;

import java.util.ArrayList;
import java.util.List;

import Utilities.ProgramDefinitions;
import Utilities.SerialU;
import Utilities.SymmetricKey;
import communication.MessageHeader;
import communication.MessagePacket;
import communication.TCP_Client;
import funtionalities.DeleteRequestBody;

public class DELETE_request_to_control extends TCP_Client{

	String FileID;

	public DELETE_request_to_control(int p, String a, String FileID ) {
		super(p,a);
		this.FileID = FileID;
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;

		List<String> PeerIDs = new ArrayList<String>();
		PeerIDs.add("Peer1");
		PeerIDs.add("Peer2");
		PeerIDs.add("Peer3");
		DeleteRequestBody msgBody = new DeleteRequestBody(FileID,PeerIDs);
		byte[] encMsgBody = SymmetricKey.encryptData(ProgramDefinitions.mydata.priv_key,SerialU.serialize(msgBody));

		MessageHeader header = new MessageHeader(MessageHeader.MessageType.requestdelete, ProgramDefinitions.mydata.peerID,null,null,0,0);
		MessagePacket packet = new MessagePacket(header, encMsgBody);
		sendMessage(packet);
	}
}