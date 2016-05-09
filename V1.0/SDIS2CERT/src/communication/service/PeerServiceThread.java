package communication.service;

import java.net.Socket;

import FileSystem.DatabaseManager;
import communication.TCP_Thread;
import communication.messages.MessagePacket;

/**
 * Performs the server-side actions in a Protocol.
 * Received messages are handled by state_machine(message)
 *
 */
public class PeerServiceThread extends TCP_Thread{

	private DatabaseManager db;

	public PeerServiceThread(Socket clientSocket, DatabaseManager database){
		socket = clientSocket;
		db = database;
	}	

	public void run() {
		MessagePacket receivedMSG = (MessagePacket) receiveMessage();			
		receivedMSG.print();
	}

	void state_machine(MessagePacket receivedMSG){
		switch (receivedMSG.header.getMessageType()) {
		case hello:
			break;
		case chunk:
			if(DEBUG)
				System.out.println("Service type: CHUNK");
			process_chunk(receivedMSG);
			break;
		case getchunk:
			if(DEBUG)
				System.out.println("Service type: GETCHUNK");
			process_getchunk(receivedMSG);
			break;
		case putchunk:
			if(DEBUG)
				System.out.println("Service type: PUTCHUNK");
			process_putchunk(receivedMSG);
			break;
		case removed:
			if(DEBUG)
				System.out.println("Service type: REMOVED");
			process_removed(receivedMSG);
			break;
		case stored:
			if(DEBUG)
				System.out.println("Service type: STORED");
			process_stored(receivedMSG);
			break;
		default:
			break;
		}
	}

	private void process_stored(MessagePacket receivedMSG) {
		// TODO Auto-generated method stub

	}

	private void process_removed(MessagePacket receivedMSG) {
		// TODO Auto-generated method stub

	}
	
	private void process_getchunk(MessagePacket receivedMSG) {
		// TODO Auto-generated method stub

	}

	private void process_chunk(MessagePacket receivedMSG) {
		// TODO Auto-generated method stub

	}
	
	private void process_putchunk(MessagePacket receivedMSG) {
		// Id of the PUTCHUNK sender
		String backupSenderId = receivedMSG.header.getSenderId();

		// Id of the chunk file to store
		String chunkFileId = receivedMSG.header.getFileId();

		// Num of the chunk file to store
		int numOfChunkToStore = receivedMSG.header.getChunkNo();

		// Replication degree of the chunk to store
		int chunkReplicationDegree = receivedMSG.header.getReplicationDegree();

		byte[] chunkData = receivedMSG.body;
		
		System.out.println("[PUTCHUNK] " + chunkData.length);
		
		// writes the file if it is not already stored
		if(!db.getDatabase().isChunkStored(chunkFileId)){
			db.getDatabase().addStoredChunkFile(chunkFileId, chunkReplicationDegree); // registers the storing
		}

		fm.writeInStoreFolderFile(chunkFileId, numOfChunkToStore, chunkData);

		// send stored
		// TODO
	}
}