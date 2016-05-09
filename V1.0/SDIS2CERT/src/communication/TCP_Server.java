package communication;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import FileSystem.DatabaseManager;
import Utilities.ProgramDefinitions;
import communication.service.ControlServiceThread;
import communication.service.PeerServiceThread;

/**
 * Server Socket. Continuously accepts connections. Upon being sent a connection request, opens a new thread capable of handling protocols.
 * Create it with a port for an argument, then issue .start() to run it. 
 */
public class TCP_Server extends Thread{

	static final boolean DEBUG = true;

	private ServerSocket peerServerSocket;
	private Socket peerSocket;
	
	private DatabaseManager db;
	private final String databaseFileName = "metada.ser";

	volatile protected boolean stop = false;
	public void STOP() { 
		stop=true;
		try {
			peerServerSocket.close();
		} catch (IOException e) {e.printStackTrace();} 
	}

	public TCP_Server(int p) {
		try {
			peerServerSocket = new ServerSocket(p);
		} catch (IOException e) {e.printStackTrace();}
		
		db = new DatabaseManager(ProgramDefinitions.mydata.peerID + File.separator + databaseFileName);
	}

	@Override
	public void run() {
		while(!stop){
			try {
				peerSocket = peerServerSocket.accept();
				if(DEBUG)
					System.out.println("Got a new client, servicing on a new thread");

				TCP_Thread newService;
				if(ProgramDefinitions.is_control)
					newService = new ControlServiceThread(peerSocket, db);
				else
					newService = new PeerServiceThread(peerSocket, db);
				newService.start();


			} catch (IOException e) 
			{if(DEBUG) System.out.println("Server Thread Closing"); }
		}
	}
}