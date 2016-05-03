package communication;

import java.io.IOException;
import java.net.ServerSocket;
import Utilities.AsymmetricKey;
import Utilities.SerialU;
import funtionalities.Metadata;
import funtionalities.PeerData;

public class TCP_ServerCentral extends TCP_Thread{

	static final boolean DEBUG = true;
	
	volatile protected boolean stop = false;
	public void STOP() { stop=true;}

	public TCP_ServerCentral(int p) {
		port = p;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			System.out.println("Estou aqui");
			System.out.flush();	



		} catch (IOException e) 
		{
			System.out.println("FDS");	
			System.out.flush();	
			e.printStackTrace();
			failed_init = true;
		}
	
			while(!stop)
			{
				System.out.println("repeat");	
				MessagePacket receivedMSG = (MessagePacket)receiveMessage();			
				receivedMSG.print();

			}
	}


	void state_machine(MessagePacket receivedMSG)
	{
		switch (receivedMSG.header.MessageType()) {
		case hello:
		
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