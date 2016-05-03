package communication;

import java.io.IOException;
import java.net.ServerSocket;

public class TCP_ServerPeer extends TCP_Thread{

	static final boolean DEBUG = true;
	
	volatile protected boolean stop = false;
	public void STOP() { stop=true;}

	public TCP_ServerPeer(int p) {
		port = p;
	}

	@Override
	public void run() {
		try {
			testSer = new ServerSocket(port);
			testSocket = testSer.accept();


		} catch (IOException e) 
		{
			e.printStackTrace();
			failed_init = true;
		}
	
			while(!stop)
			{
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
	


}