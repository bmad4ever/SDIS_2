package communication;

import java.net.Socket;


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
		receivedMSG.print();
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