package communication.service;

import java.io.File;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import Utilities.BinaryFile;
import Utilities.MessageStamp;
import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Thread;
import communication.messages.DeleteBody;
import communication.messages.DeleteRequestBody;
import communication.messages.MessageHeader;
import communication.messages.MessageHeader.MessageType;
import communication.messages.MessagePacket;
import funtionalities.AsymmetricKey;
import funtionalities.PeerMetadata;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;
import protocols.DELETE;

/**
 * Performs the server-side actions in a Protocol.
 * Received messages are handled by state_machine(message)
 */
public class ControlServiceThread extends TCP_Thread{
	private static final int SOCKET_TIMEOUT = 4000;

	public ControlServiceThread(Socket clientSocket){
		socket = clientSocket;
	}	

	@Override
	public void run() {
		MessagePacket receivedMSG = (MessagePacket) receiveMessage();			
		if(DEBUG)
			receivedMSG.print();

		state_machine(receivedMSG);
	}


	public void sendDeny()
	{
		MessageHeader h = new MessageHeader(
				MessageHeader.MessageType.deny,"CRED");
		MessagePacket m = new MessagePacket(h, null);
		sendMessage(m);
	}

	public void sendConfirm()
	{
		MessageHeader h = new MessageHeader(
				MessageHeader.MessageType.confirm,"CRED");
		MessagePacket m = new MessagePacket(h, null);
		sendMessage(m);
	}

	/**
	 * State Machine to decide how to process a message
	 * @param receivedMSG the message to process
	 */
	public void state_machine(MessagePacket receivedMSG){
		switch (receivedMSG.header.getMessageType()) {
		case hello:
			if(DEBUG)
				System.out.println("Service type: HELLO");
			process_hello(receivedMSG);
			break;
		case requestdelete:
			if(DEBUG)
				System.out.println("Service type: REQUESTDEL");
			processDeleteRequest(receivedMSG);
			break;
		case peer_privkey:
			if(DEBUG)
				System.out.println("Service type: Peer_Privkey");
			process_privatekey(receivedMSG);
			break;
		case peer_backup_metadata:
			process_peer_metadata_backup(receivedMSG);
			break;
		case peer_restore_metadata:
			process_peer_metadata_recover(receivedMSG);
			break;
		case getpeeraddr:
			process_Who(receivedMSG);
			break;
		case revive:
			processRevive(receivedMSG);
			break;
		default:
			break;
		}
		closeSocket();
	}

	public void process_hello(MessagePacket receivedMSG){
		//deny por agora
		sendDeny();
		return;
	}

	public void process_privatekey(MessagePacket receivedMSG){

		if(DEBUG)
			receivedMSG.print();
		
		Object[] receivedbody = (Object[]) SerialU.deserialize(receivedMSG.body);
		
		PeerData new_pd = (PeerData) SerialU.deserialize(  AsymmetricKey.decrypt(AsymmetricKey.prvk, (byte[]) receivedbody[0])  );
		System.out.println("Sender ID is:" + new String(AsymmetricKey.decrypt(AsymmetricKey.prvk, (byte[])receivedbody[1])) );
		
		PeerData existingData = PeerMetadata.getPeerData(receivedMSG.header.getSenderId());

		//deny - no peer data sent
		if(new_pd==null){
			sendDeny();
			return;
		}

		if(DEBUG)System.out.println("<"+new_pd.peerID+">");

		//deny - private keys are disparate on new and old data
		if (existingData != null){
			if(Arrays.equals(new_pd.priv_key,existingData.priv_key))
			{				
				PeerMetadata.updatePeerData(existingData, new_pd);
			}
			else
			{
				MessageHeader h = new MessageHeader(
						MessageHeader.MessageType.deny,"CRED");
				MessagePacket m = new MessagePacket(h, null);
				sendMessage(m);

				return;
			}
		}
		//accept, store data
		else{
			PeerMetadata.addNewPeerData(new_pd);
		}

		// and send full peer metadata with no private keys.
		MessageHeader h = new MessageHeader(MessageHeader.MessageType.confirm,"CRED");

		HashSet<PeerData> peerMetadata = PeerMetadata.getActivePeersData4peers();
		byte[] tmp =  SerialU.serialize(peerMetadata);	
		byte[] peerMbody = SymmetricKey.encryptData(new_pd.priv_key, tmp);
		MessagePacket m = new MessagePacket(h,peerMbody);
		PeerMetadata.updateActivePeer(new_pd.peerID); 
		sendMessage(m);
	}

	/**
	 * Processes the delete request message and processes it.
	 * If there are any errors it should send a message to the
	 * peer that requested the delete with the deny type
	 * @param receivedMSG The delete request message packet to be processed
	 */
	public void processDeleteRequest(MessagePacket receivedMSG){
		String sender = receivedMSG.header.getSenderId();
		byte[] senderKey = PeerMetadata.getPeerData(sender).priv_key;
		byte[] unencryptBody = SymmetricKey.decryptData(senderKey, receivedMSG.body);
		DeleteRequestBody msgBody = (DeleteRequestBody) SerialU.deserialize(unencryptBody);
		MessageHeader respheader;
		if(msgBody == null) {
			respheader = new MessageHeader(MessageHeader.MessageType.deny,"CRED");
			MessagePacket m = new MessagePacket(respheader, null);
			sendMessage(m);
		}else {
			PeerMetadata.processStamping(receivedMSG.header.getSenderId(), new MessageStamp(receivedMSG.header,msgBody));
			respheader = new MessageHeader(MessageHeader.MessageType.confirm,"CRED");
			MessagePacket m = new MessagePacket(respheader, null);
			sendMessage(m);
			/* 
			 * Gets the peer information of each peer in the list
			 * and sends a DELETE protocol to each one of them with
			 * a peerData object containing peer information
			 */
			
			//send message using request delete timestamp
			MessageHeader header = new MessageHeader(MessageHeader.MessageType.delete, "control",receivedMSG.header.getTimeStamp());
			for(int i = 0; i < msgBody.PeerIDs.size(); i++){
				System.out.println("Sent DELETE to peer: " + msgBody.PeerIDs.get(i));
				if (!PeerMetadata.isPeerActive(msgBody.PeerIDs.get(i))) continue; //peer is not active
				PeerData pd = PeerMetadata.getPeerData(msgBody.PeerIDs.get(i));
				if(pd != null){
					DeleteBody deleteBody = new DeleteBody(msgBody.FileID, sender);//msgBody.PeerIDs.get(i));
					byte[] body = SerialU.serialize(deleteBody);
					byte[] encryptBody = SymmetricKey.encryptData(pd.priv_key, body);
					MessagePacket deleteMessage = new MessagePacket(header, encryptBody);
					DELETE dp = new DELETE(pd, deleteMessage, new RefValue<Boolean>());
					dp.start();
					try {
						dp.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void process_peer_metadata_backup(MessagePacket receivedMSG)
	{
		if(PeerMetadata.getPeerData(receivedMSG.header.getSenderId()) == null)
		{
			sendDeny();
			return; //failed to backup data or not a known peer
		}
		
		File dir_rec = new File(ProgramDefinitions.controlPeersBackupFolderName);
		dir_rec.mkdir();
		if(!BinaryFile.saveBinaryFile(
				ProgramDefinitions.controlPeersBackupFolderName + 
				receivedMSG.header.getFileId()
				,receivedMSG.body))
		{
			sendDeny();
			return; //failed to backup data or not a known peer
		}
		sendConfirm();
	}

	public void process_peer_metadata_recover(MessagePacket receivedMSG)
	{
		if(PeerMetadata.getPeerData(receivedMSG.header.getSenderId()) == null)
		{
			sendDeny();
			return; //not a known peer
		}
		
		byte[] data = null;
		try{ data = BinaryFile.readBinaryFile(
				ProgramDefinitions.controlPeersBackupFolderName + 
				receivedMSG.header.getFileId());
		} catch (Exception e) {return;}
		if(data == null){
			sendDeny();
			return; //failed to read data
		}
		
		MessageHeader h = new MessageHeader(
				MessageHeader.MessageType.peer_medatada,"CRED");
		MessagePacket m = new MessagePacket(h, data);
		sendMessage(m);
	}

	public void process_Who(MessagePacket receivedMSG){
		if(PeerMetadata.getPeerData(receivedMSG.header.getSenderId()) == null)
		{
			sendDeny();
			return; //not a known peer
		}
		
		PeerMetadata.renewPeerService(receivedMSG.header.getSenderId());
		
		HashSet<PeerData> peerMetadata = PeerMetadata.getActivePeersData4peers();
		byte[] tmp =  SerialU.serialize(peerMetadata);	
		byte[] data = SymmetricKey.encryptData(PeerMetadata.getPeerData(receivedMSG.header.getSenderId()).priv_key, tmp);
		
		
		MessageHeader h = new MessageHeader(
				MessageHeader.MessageType.peeraddr,"CRED");
		MessagePacket m = new MessagePacket(h, data);
		sendMessage(m);
	
	}
	
	public void processRevive(MessagePacket receivedMSG){
		System.out.println("REVIVE!!");
		String senderId = receivedMSG.header.getSenderId();
		byte[] senderKey = PeerMetadata.getPeerData(senderId).priv_key;
		byte[] body = SymmetricKey.decryptData(senderKey, receivedMSG.body);
		Hashtable<String, Long> receivedPeers = (Hashtable<String, Long>)SerialU.deserialize(body);
		List<MessageStamp> deleteList = new ArrayList<MessageStamp>();
		for (Entry<String,Long> e : receivedPeers.entrySet() ) {
			List<MessageStamp> stamps = PeerMetadata.message_stamps.get(e.getKey());
			if(stamps == null){
				continue;
			}
			for (int i = stamps.size()-1; i >= 0; i--) {
				MessageStamp messageStamp = stamps.get(i);
				if(messageStamp.timestamp <= e.getValue()){
					break;
				}else{
					deleteList.add(messageStamp);
				}
			}
		}
		
		MessageHeader sendHeader = new MessageHeader(MessageType.superdelete,senderId);
		byte[] sendBody = SerialU.serialize(deleteList);
		byte[] encpSendBody = SymmetricKey.encryptData(PeerMetadata.getPeerData(senderId).priv_key, sendBody);
		MessagePacket messageToSend = new MessagePacket(sendHeader, encpSendBody);
		sendMessage(messageToSend);
	}
	
}