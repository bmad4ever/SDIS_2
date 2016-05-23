package communication.service;

import java.net.Socket;

import FileSystem.Chunk;
import FileSystem.DatabaseManager;
import Utilities.ProgramDefinitions;
import communication.TCP_Thread;
import communication.messages.DeleteBody;
import communication.messages.DeleteRequestBody;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.PeerMetadata;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;

/**
 * Performs the server-side actions in a Protocol.
 * Received messages are handled by state_machine(message)
 *
 */
public class PeerServiceThread extends TCP_Thread{

	public PeerServiceThread(Socket clientSocket){
		socket = clientSocket;
	}	

	@Override
	public void run() {
		MessagePacket receivedMSG = (MessagePacket) receiveMessage();
		state_machine(receivedMSG);
	}

	void state_machine(MessagePacket receivedMSG){
		if(receivedMSG.header.getSenderId().equals(ProgramDefinitions.mydata.peerID))
		{
			if(DEBUG) System.out.println("NOT EXPECTED 102:" + receivedMSG.header.getSenderId() + "---" + ProgramDefinitions.mydata.peerID);
			return;
		}
		switch (receivedMSG.header.getMessageType()) {
		case hello:
			break;
		case chunk:
			if(DEBUG)System.out.println("Service type: CHUNK");
			//process_chunk(receivedMSG);
			break;
		case getchunk:
			if(DEBUG)System.out.println("Service type: GETCHUNK");
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
			//process_stored(receivedMSG);
			break;
		case delete:
			System.out.println("Service type: DELETE");
			processDelete(receivedMSG);
			break;
		default:
			break;
		}
	}

	/*private void process_stored(MessagePacket receivedMSG) {
		// Id of the STORED sender
		String storedSenderId = receivedMSG.header.getSenderId();

		// Id of the STORED chunk file
		String chunkStoredFileId = receivedMSG.header.getFileId();

		// Num of the STORED chunk
		int numOfChunkStored = receivedMSG.header.getChunkNum();

		// Saves the chunk storer id
		if(db.getDatabase().isChunkStored(chunkStoredFileId, numOfChunkStored))
			db.getDatabase().getStoredChunkData(chunkStoredFileId, numOfChunkStored).addPeerSaved(storedSenderId);
	}*/

	private void process_getchunk(MessagePacket receivedMSG) {
		// Id of the GETCHUNK sender
		//String getChunkSenderId = receivedMSG.header.getSenderId();

		// Id of the chunk file to restore
		String getChunkFileId = receivedMSG.header.getFileId();

		// Num of the chunk file to restore
		int numOfChunkToRestore = receivedMSG.header.getChunkNum();

		// Verifies if it is a received chunk
		System.out.println(getChunkFileId +" MMMM "+ numOfChunkToRestore);
		if(!ProgramDefinitions.db.getDatabase().isChunkStored(getChunkFileId, numOfChunkToRestore)) 
			{
				if(DEBUG) System.out.println("chunk not owned ZZZZZZZZZZ");
				return;			
			}

		byte[] chunkToSendData = ProgramDefinitions.db.getDatabase().getStoredChunkData(getChunkFileId, numOfChunkToRestore);
		if(chunkToSendData != null){
			MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.chunk, ProgramDefinitions.mydata.peerID, getChunkFileId, numOfChunkToRestore);
			MessagePacket n = new MessagePacket(headMessage, chunkToSendData);
			sendMessage(n);
			if(DEBUG) System.out.println("chunk sent XXXXXXXXXXXX");
		}
	}

	/*private void process_chunk(MessagePacket receivedMSG) {
		// Id of the CHUNK sender
		String chunkSenderId = receivedMSG.header.getSenderId();

		// Id of the chunk file received
		String chunkFileId = receivedMSG.header.getFileId();

		// Num of the chunk file received
		int numOfChunkReceived = receivedMSG.header.getChunkNum();
		
		byte[] receivedData = receivedMSG.body;
		
		if(!db.getDatabase().getStoredChunkData(chunkFileId, numOfChunkReceived).hasData())
			db.getDatabase().getStoredChunkData(chunkFileId, numOfChunkReceived).writeChunkFile(receivedData);
	}*/

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
		if(!ProgramDefinitions.db.getDatabase().isChunkStored(chunkFileId, numOfChunkToStore)){
			//Saves on data and registers storing
			ProgramDefinitions.db.getDatabase().addStoredChunkFile(chunkFileId, numOfChunkToStore, chunkReplicationDegree,chunkData);
		}

		MessageHeader headMessage = new MessageHeader(MessageHeader.MessageType.stored, backupSenderId, chunkFileId, numOfChunkToStore);
		MessagePacket n = new MessagePacket(headMessage, null);
		sendMessage(n);
		if(DEBUG) System.out.println(numOfChunkToStore+"!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

	/**
	 * Process a delete message. If the message is valid it
	 * tries to delete all the chunks of the file with a 
	 * certain fileId contained in the message body. Sends
	 * a confirmation message back to the control so it can
	 * ragister the occurence
	 * @param message The message packet
	 */
	private void processDelete(MessagePacket message){
		String sender = message.header.getSenderId();
		byte[] senderKey = ProgramDefinitions.mydata.priv_key;
		byte[] unencryptBody = SymmetricKey.decryptData(senderKey, message.body);
		DeleteBody msgBody = (DeleteBody) SerialU.deserialize(unencryptBody);
		//TODO: Check if peerId matches the peerId of the file
		deleteFile(msgBody.getFileId());
	}
	
	/**
	 * Deletes a file folder aswell as the chunk files
	 * associated with it and removes this file entry
	 * in the database
	 * @param fileId The file identifier to delete
	 */
	private void deleteFile(String fileId){
		DatabaseManager dbm = ProgramDefinitions.db;
		dbm.getDatabase().deleteFile(fileId);
	}
	
}