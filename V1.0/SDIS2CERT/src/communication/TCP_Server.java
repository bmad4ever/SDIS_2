package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Utilities.SerialU;
import funtionalities.Metadata;
import funtionalities.PeerData;

public class TCP_Server extends TCP_Thread{

	static final boolean DEBUG = true;
	
	volatile protected boolean stop = false;
	public void STOP() { stop=true;}

	public TCP_Server(int p) {
		port = p;
	}

	@Override
	public void run() {
		try {
			testSer = new ServerSocket(port);
			testSocket = testSer.accept();
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
		MessageKeyPacket msg = new MessageKeyPacket(header, AsymmetricKey.pubk);
	
		sendMessage(msg);
		
		MessagePacket msgPack = (MessagePacket) receiveMessage();
		PeerData new_pd = (PeerData) SerialU.deserialize(msgPack.body);
		if (new_pd!=null) 
		{
			Metadata.data.add(new_pd);
		}
		
		
	}

}