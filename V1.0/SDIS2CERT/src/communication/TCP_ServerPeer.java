package communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Server Socket. Continuously accepts connections. Upon being sent a connection request, opens a new thread capable of handling protocols.
 * Create it with a port for an argument, then issue .start() to run it. 
 * 
 */
public class TCP_ServerPeer extends TCP_Thread{

	static final boolean DEBUG = true;
	
	volatile protected boolean stop = false;
	public void STOP() { stop=true;}

	public TCP_ServerPeer(int p) {
		port = p;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {e.printStackTrace();}
	}

	@Override
	public void run() {
		while(true)
		{
			try {
				socket = serverSocket.accept();
				System.out.println("Got a new client, servicing on a new thread");
				
				Thread newService = new Thread(new serviceThread(socket));
				newService.start();
				

			} catch (IOException e) 
			{e.printStackTrace();failed_init = true;}
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
	
	
	public class serviceThread implements Runnable{
		
		Socket socket;	
		public serviceThread(Socket clientSocket)
		{
			socket = clientSocket;
		}
		
		public void run() {					
			while(!stop)
			{
				MessagePacket receivedMSG = (MessagePacket)receiveMessage();			
				receivedMSG.print();
				
			}
		}
	}


	


}