package communication.service;

import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;

import protocols.DELETE;
import Utilities.PeerData;
import Utilities.RefValue;
import communication.TCP_Thread;
import communication.messages.DeleteRequestBody;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.AsymmetricKey;
import funtionalities.PeerMetadata;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;

/**
 * Performs the server-side actions in a Protocol.
 * Received messages are handled by state_machine(message)
 *
 */
public class ControlServiceThread extends TCP_Thread{

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
		case peer_privkey:
			if(DEBUG)
				System.out.println("Service type: Peer_Privkey");
			process_privatekey(receivedMSG);
			break;
		default:
			break;
		}
	}
	
	public void process_hello(MessagePacket receivedMSG){
		
		//deny por agora
		MessageHeader h = new MessageHeader(
				MessageHeader.MessageType.deny,"CRED");
		MessagePacket m = new MessagePacket(h, null);
		sendMessage(m);
		return;
	}

	public void process_privatekey(MessagePacket receivedMSG){
		
		if(DEBUG)
			receivedMSG.print();
		byte[] msgContent = AsymmetricKey.decrypt(AsymmetricKey.prvk, receivedMSG.body);
		PeerData new_pd = (PeerData) SerialU.deserialize(msgContent);

		PeerData existingData = PeerMetadata.getPeerData(receivedMSG.header.getSenderId());
		
		//deny - no peer data sent
		if(new_pd==null){
			MessageHeader h = new MessageHeader(
					MessageHeader.MessageType.deny,"CRED");
			MessagePacket m = new MessagePacket(h, null);
			sendMessage(m);

			return;
		}
		
		if(DEBUG)System.out.println("PPPPDDDDD<"+new_pd.peerID+">");
		
		//deny - private keys are disparate on new and old data
		if (existingData != null){
			if(Arrays.equals(new_pd.priv_key,existingData.priv_key)){
				PeerMetadata.updatePeerData(existingData, new_pd);
			}else{
				MessageHeader h = new MessageHeader(
						MessageHeader.MessageType.deny,"CRED");
				MessagePacket m = new MessagePacket(h, null);
				sendMessage(m);

				return;
			}
		}
		//accept, store data
		else
			PeerMetadata.addNewPeerData(new_pd);

		// and send full peer metadata with no private keys.
		MessageHeader h = new MessageHeader(MessageHeader.MessageType.confirm,"CRED");

		HashSet<PeerData> peerMetadata = PeerMetadata.getMetadata2send2peer();
		byte[] tmp =  SerialU.serialize(peerMetadata);
		byte[] peerMbody = SymmetricKey.encryptData(new_pd.priv_key, tmp);
		MessagePacket m = new MessagePacket(h,peerMbody);
		PeerMetadata.updateActivePeer(new_pd.peerID); 
		sendMessage(m);
	}

	public void process_delete(MessagePacket receivedMSG){
		String sender = receivedMSG.header.getSenderId();
		byte[] senderKey = PeerMetadata.getPeerData(sender).priv_key;
		byte[] unencryptBody = SymmetricKey.decryptData(senderKey, receivedMSG.body);
		DeleteRequestBody msgBody = (DeleteRequestBody) SerialU.deserialize(unencryptBody);
		MessageHeader responseheader;
		if(msgBody == null) {
			responseheader = new MessageHeader(MessageHeader.MessageType.deny,"CRED");
			MessagePacket m = new MessagePacket(responseheader,null);
			sendMessage(m);
			return;
		}
		responseheader = new MessageHeader(MessageHeader.MessageType.confirm,"CRED");
		//byte[] tmp = SerialU.serialize(msgBody.PeerIDs.size());
		//byte[] responsebody = SymmetricKey.encryptData(senderKey, tmp);
		MessagePacket m = new MessagePacket(responseheader,null);
		sendMessage(m);

		byte[] deleteBody = SerialU.serialize(msgBody.FileID);
		MessagePacket deleteMessage = new MessagePacket(receivedMSG.header , deleteBody);

		for(int i = 0; i < msgBody.PeerIDs.size(); i++){
			System.out.println(msgBody.PeerIDs.get(i));
			PeerData pd = PeerMetadata.getPeerData(msgBody.PeerIDs.get(i));
			if(pd == null){
				continue;
			}
			DELETE dp = new DELETE(pd, new RefValue<Boolean>());
			dp.start();
			try {
				dp.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//O control deve, aqui, para cada peer identificado na lista:
			// - Verificar se existe peer com esse nome na metadata
			// - Se existir, fazer uma nova thread DELETE_protocol a cada um, na qual o corpo da mensagem a enviar � a variavel deleteMessage
		}
	}
}