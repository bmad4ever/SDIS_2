package communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import Utilities.ProgramDefinitions;


/**
 * Server Socket. Continuously accepts connections. Upon being sent a connection request, opens a new thread capable of handling protocols.
 * Create it with a port for an argument, then issue .start() to run it. 
 */
public class TCP_Server extends Thread{

	static final boolean DEBUG = true;
	
	ServerSocket peerServerSocket;
	Socket peerSocket;

	volatile protected boolean stop = false;
	public void STOP() { stop=true;}

	public TCP_Server(int p) {
		try {
			peerServerSocket = new ServerSocket(p);
		} catch (IOException e) {e.printStackTrace();}
	}

	@Override
	public void run() {
		while(!stop)
		{
			try {
				peerSocket = peerServerSocket.accept();
				System.out.println("Got a new client, servicing on a new thread");
				
				TCP_Thread newService;
				if(ProgramDefinitions.is_control)
					newService = new CentralServiceThread(peerSocket);
				else
					newService = new PeerServiceThread(peerSocket);
				newService.start();
				

			} catch (IOException e) {e.printStackTrace();}
		}
		
	}


}
