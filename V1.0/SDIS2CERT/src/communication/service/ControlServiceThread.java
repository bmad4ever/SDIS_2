package communication.service;

import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import FileSystem.DatabaseManager;
import Utilities.PeerData;
import communication.TCP_Thread;
import communication.messages.DeleteRequestBody;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.AsymmetricKey;
import funtionalities.Metadata;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;

/**
 * Performs the server-side actions in a Protocol.
 * Received messages are handled by state_machine(message)
 *
 */
public class ControlServiceThread extends TCP_Thread{

	private DatabaseManager db; // stores system information

	public ControlServiceThread(Socket clientSocket, DatabaseManager database){
		socket = clientSocket;
		db = database;
	}	

	public void run() {
		MessagePacket receivedMSG = (MessagePacket) receiveMessage();			
		if(DEBUG)
			receivedMSG.print();

		state_machine(receivedMSG);
	}

	public void state_machine(MessagePacket receivedMSG){
		switch (receivedMSG.header.getMessageType()) {
		case hello:
			if(DEBUG)
				System.out.println("Service type: HELLO");
			process_hello(receivedMSG);
			break;
		case requestdelete:
			if(DEBUG)
				System.out.println("Service type: DELETE");
			process_delete(receivedMSG);
			break;
		default:
			break;
		}
	}

	public void process_hello(MessagePacket receivedMSG){
		MessageHeader header = new MessageHeader(
				MessageHeader.MessageType.cred_pubkey,"CRED");
		byte[] body = AsymmetricKey.pubk.getEncoded();
		MessagePacket msg = new MessagePacket(header, body);
		sendMessage(msg);

		MessagePacket msgPack = (MessagePacket) receiveMessage();
		if(DEBUG)
			msgPack.print();
		byte[] msgContent = AsymmetricKey.decrypt(AsymmetricKey.prvk, msgPack.body);
		PeerData new_pd = (PeerData) SerialU.deserialize(msgContent);

		PeerData existingData = Metadata.getPeerData(receivedMSG.header.getSenderId());

		if(new_pd==null){
			MessageHeader h = new MessageHeader(
					MessageHeader.MessageType.deny,"CRED");
			MessagePacket m = new MessagePacket(h, null);
			sendMessage(m);

			return;
		}

		if (existingData != null){
			if(Arrays.equals(new_pd.priv_key,existingData.priv_key)){
				Metadata.updatePeerData(existingData, new_pd);
			}else{
				MessageHeader h = new MessageHeader(
						MessageHeader.MessageType.deny,"CRED");
				MessagePacket m = new MessagePacket(h, null);
				sendMessage(m);

				return;
			}
		}else
			Metadata.addNewPeerData(new_pd);

		MessageHeader h = new MessageHeader(MessageHeader.MessageType.confirm,"CRED");

		List<PeerData> peerMetadata = Metadata.getMetadata2send2peer();
		byte[] tmp =  SerialU.serialize(peerMetadata);
		byte[] peerMbody = SymmetricKey.encryptData(new_pd.priv_key, tmp);
		MessagePacket m = new MessagePacket(h,peerMbody);
		Metadata.updateActivePeer(new_pd.peerID); 
		sendMessage(m);
	}

	public void process_delete(MessagePacket receivedMSG){
		String sender = receivedMSG.header.getSenderId();
		byte[] senderKey = Metadata.getPeerData(sender).priv_key;
		byte[] unencryptBody = SymmetricKey.decryptData(senderKey, receivedMSG.body);
		DeleteRequestBody msgBody = (DeleteRequestBody) SerialU.deserialize(unencryptBody);

		MessageHeader responseheader = new MessageHeader(
				MessageHeader.MessageType.confirm,"CRED");
		byte[] tmp =  SerialU.serialize(msgBody.PeerIDs.size());
		byte[] responsebody = SymmetricKey.encryptData(senderKey, tmp);
		MessagePacket m = new MessagePacket(responseheader,responsebody);
		sendMessage(m);

		byte[] deleteBody = SerialU.serialize(msgBody.FileID);
		MessagePacket deleteMessage = new MessagePacket(receivedMSG.header , deleteBody);

		for(int i = 0; i < msgBody.PeerIDs.size(); i++){
			System.out.println(msgBody.PeerIDs.get(i));

			//O control deve, aqui, para cada peer identificado na lista:
			// - Verificar se existe peer com esse nome na metadata
			// - Se existir, fazer uma nova thread DELETE_protocol a cada um, na qual o corpo da mensagem a enviar é a variavel deleteMessage
		}
	}
}