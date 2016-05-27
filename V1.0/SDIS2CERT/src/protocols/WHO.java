package protocols;

import java.util.HashSet;

import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.PeerMetadata;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;

public class WHO  extends TCP_Client{

	public WHO(int p, String a, RefValue<Boolean> taskCompleted) {
		super(p, a, taskCompleted);
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;
		
		if(DEBUG)
			System.out.println("Sending a WHO request");
		
		MessageHeader header = new MessageHeader(MessageHeader.MessageType.getpeeraddr, ProgramDefinitions.mydata.peerID);
		MessagePacket message = new MessagePacket(header, null);
		sendMessage(message);
		
		MessagePacket response = (MessagePacket) receiveMessage();
		if(response==null)
		{
			return;
		}
		
		if(DEBUG) response.print();
		this.taskCompleted.value = (response.header.getMessageType() == MessageHeader.MessageType.peeraddr);
		if(this.taskCompleted.value){
			byte[] tmp = SymmetricKey.decryptData(ProgramDefinitions.mydata.priv_key, response.body);
			HashSet<PeerData> tmpPD = (HashSet<PeerData>) SerialU.deserialize(tmp);
			PeerMetadata.setPeerMetadataList(tmpPD);
			
			
			System.out.println("WHO request was successfull");
		}
		
		if(DEBUG)
			PeerMetadata.printData();
		
	}
}
