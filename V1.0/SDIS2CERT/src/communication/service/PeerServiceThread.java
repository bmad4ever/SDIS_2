package communication.service;

import java.net.Socket;

import FileSystem.DatabaseManager;
import Utilities.ProgramDefinitions;
import communication.TCP_Thread;
import communication.messages.MessageHeader;
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
		// Id of the STORED sender
		String storedSenderId = receivedMSG.header.getSenderId();

		// Id of the STORED chunk file
		String chunkStoredFileId = receivedMSG.header.getFileId();

		// Num of the STORED chunk
		int numOfChunkStored = receivedMSG.header.getChunkNum();

		// Saves the chunk storer id
		if(db.getDatabase().isChunkStored(chunkStoredFileId, numOfChunkStored))
			db.getDatabase().getStoredChunkData(chunkStoredFileId, numOfChunkStored).addPeerSaved(storedSenderId);
	}

	private void process_getchunk(MessagePacket receivedMSG) {
		// Id of the GETCHUNK sender
		/*String getChunkSenderId = */receivedMSG.header.getSenderId();

		// Id of the chunk file to restore
		String getChunkFileId = receivedMSG.header.getFileId();

		// Num of the chunk file to restore
		int numOfChunkToRestore = receivedMSG.header.getChunkNum();

		// Verifies if it is a received chunk
		if(!db.getDatabase().isChunkStored(getChunkFileId, numOfChunkToRestore)) return;

		// TODO encryption??
		byte[] chunkToSendData = db.getDatabase().getStoredChunkData(getChunkFileId, numOfChunkToRestore).readChunkFileData();

		if(chunkToSendData != null){
			MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.chunk, ProgramDefinitions.mydata.peerID, getChunkFileId, numOfChunkToRestore);
			MessagePacket n = new MessagePacket(headMessage, chunkToSendData);
			sendMessage(n);
		}
	}

	private void process_chunk(MessagePacket receivedMSG) {
		// Id of the CHUNK sender
		/*String chunkSenderId = */receivedMSG.header.getSenderId();

		// Id of the chunk file received
		String chunkFileId = receivedMSG.header.getFileId();

		// Num of the chunk file received
		int numOfChunkReceived = receivedMSG.header.getChunkNum();
		
		byte[] receivedData = receivedMSG.body;
		
		if(!db.getDatabase().getStoredChunkData(chunkFileId, numOfChunkReceived).hasData())
			db.getDatabase().getStoredChunkData(chunkFileId, numOfChunkReceived).setData(receivedData);
	}

	private void process_putchunk(MessagePacket receivedMSG) {		
		// Id of the PUTCHUNK sender
		String backupSenderId = receivedMSG.header.getSenderId();

		// Id of the chunk file to store
		String chunkFileId = receivedMSG.header.getFileId();

		// Num of the chunk file to store
		int numOfChunkToStore = receivedMSG.header.getChunkNum();

		// Replication degree of the chunk to store
		int chunkReplicationDegree = receivedMSG.header.getReplicationDegree();

		byte[] chunkData = receivedMSG.body;

		// writes the file if it is not already stored
		if(!db.getDatabase().isChunkStored(chunkFileId, numOfChunkToStore)){
			db.getDatabase().addStoredChunkFile(chunkFileId, numOfChunkToStore, chunkReplicationDegree); // registers the storing
		}

		// write chunk file
		db.getDatabase().getStoredChunkData(chunkFileId, numOfChunkToStore).setData(chunkData);
		db.getDatabase().getStoredChunkData(chunkFileId, numOfChunkToStore).writeChunkFile();

		MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.stored, backupSenderId, chunkFileId, numOfChunkToStore);
		MessagePacket n = new MessagePacket(headMessage, null);
		sendMessage(n);
	}
}