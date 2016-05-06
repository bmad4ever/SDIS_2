package communication;

import java.net.Socket;
import java.util.Arrays;

import Utilities.AsymmetricKey;
import Utilities.SerialU;
import Utilities.SymmetricKey;
import funtionalities.DeleteRequestBody;
import funtionalities.Metadata;
import funtionalities.PeerData;


/**
 * Performs the server-side actions in a Protocol.
 * Received messages are handled by state_machine(message)
 *
 */
public class CentralServiceThread extends TCP_Thread{
	
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
		switch (receivedMSG.header.getMessageType()) {
		case hello:
			if(DEBUG)
				System.out.println("Service type: HELLO");
			process_hello(receivedMSG);
			break;
		case requestdelete:
			if(DEBUG)
				System.out.println("Service type: DELETE");
			process_delete(receivedMSG);
			break;
			
		default:
			break;
		}
	}
	
	
	void process_hello(MessagePacket receivedMSG)
	{
		MessageHeader header = new MessageHeader(
				MessageHeader.MessageType.cred_pubkey
				,"CRED"	,null,null,0,1);
		byte[] body = AsymmetricKey.pubk.getEncoded();
		MessagePacket msg = new MessagePacket(header, body);
		sendMessage(msg);
		
		MessagePacket msgPack = (MessagePacket) receiveMessage();
		if(DEBUG)
			msgPack.print();
		byte[] msgContent = AsymmetricKey.decrypt(AsymmetricKey.prvk, msgPack.body);
		PeerData new_pd = (PeerData) SerialU.deserialize(msgContent);
		
		PeerData existingData = Metadata.getPeerData(receivedMSG.header.getSenderId());
		
		
		if(new_pd==null)
		{
			MessageHeader h = new MessageHeader(
					MessageHeader.MessageType.deny
					,"CRED",null,null,0,1);
			MessagePacket m = new MessagePacket(h, null);
			sendMessage(m);
			return;
		}
		
		if (existingData != null)
		{
			if(Arrays.equals(new_pd.priv_key,existingData.priv_key))
			{
				existingData = new_pd;
			}
			else
			{
				MessageHeader h = new MessageHeader(
						MessageHeader.MessageType.deny
						,"CRED",null,null,0,1);
				MessagePacket m = new MessagePacket(h, null);
				sendMessage(m);
				return;
			}
		}
		else
			Metadata.data.add(new_pd);
		
		MessageHeader h = new MessageHeader(
				MessageHeader.MessageType.confirm
				,"CRED",null,null,0,1);
		MessagePacket m = new MessagePacket(h, null);
		sendMessage(m);
		
		
	}
	
	void process_delete(MessagePacket receivedMSG)
	{
		String sender = receivedMSG.header.getSenderId();
		byte[] senderKey = Metadata.getPeerData(sender).priv_key;
		byte[] unencryptBody = SymmetricKey.decryptData(senderKey, receivedMSG.body);
		DeleteRequestBody msgBody = (DeleteRequestBody) SerialU.deserialize(unencryptBody);
		
		for(int i = 0; i < msgBody.PeerIDs.size(); i++)
			System.out.println(msgBody.PeerIDs.get(i));
		
	}
}