package communication;

import java.net.Socket;

import Utilities.AsymmetricKey;
import Utilities.SerialU;
import funtionalities.Metadata;
import funtionalities.PeerData;


/**
 * Performs the server-side actions in a Protocol.
 * Received messages are handled by state_machine(message)
 *
 */
public class CentralServiceThread extends TCP_Thread{
	
	static final boolean DEBUG = true;

	public CentralServiceThread(Socket clientSocket)
	{
		socket = clientSocket;
	}	
	
	public void run() {
		MessagePacket receivedMSG = (MessagePacket)receiveMessage();			
		if(DEBUG)
			receivedMSG.print();
		
		state_machine(receivedMSG);
	}
	
	
	void state_machine(MessagePacket receivedMSG)
	{
		switch (receivedMSG.header.MessageType()) {
		case hello:
			process_hello();
			break;

		default:
			break;
		}
	}
	
	
	void process_hello()
	{		
		MessageHeader header = new MessageHeader(
				MessageHeader.MessageType.cred_pubkey
				,"CRED"	,null,null,0,1);
		byte[] body = AsymmetricKey.pubk.getEncoded();
		MessagePacket msg = new MessagePacket(header, body);
		
		sendMessage(msg);
		
		MessagePacket msgPack = (MessagePacket) receiveMessage();
		PeerData new_pd = (PeerData) SerialU.deserialize(msgPack.body);
		if (new_pd!=null) 
		{
			Metadata.data.add(new_pd);
		}
		
		
		
		
	}
}