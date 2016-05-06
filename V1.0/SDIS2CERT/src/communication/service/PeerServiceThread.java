package communication.service;

import java.net.Socket;

import communication.TCP_Thread;
import communication.messages.MessagePacket;


/**
 * Performs the server-side actions in a Protocol.
 * Received messages are handled by state_machine(message)
 *
 */
public class PeerServiceThread extends TCP_Thread{
	
	
	public PeerServiceThread(Socket clientSocket)
	{
		socket = clientSocket;
	}	
	
	public void run() {
		MessagePacket receivedMSG = (MessagePacket)receiveMessage();			
		receivedMSG.print();
	}
	
	
	void state_machine(MessagePacket receivedMSG)
	{
		switch (receivedMSG.header.getMessageType()) {
		case hello:
		
			break;

		default:
			break;
		}
	}
}