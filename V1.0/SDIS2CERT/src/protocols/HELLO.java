package protocols;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.AsymmetricKey;
import funtionalities.Metadata;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;


/**
 * <description of HELLO protocol>
 *
 */
public class HELLO extends TCP_Client{
	
	RefValue<Boolean> accept;
	
	public HELLO(int p, String a, RefValue<Boolean> accept) {
		super(p,a);
		this.accept = accept;
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;
		
		MessageHeader nHeader = new MessageHeader(MessageHeader.MessageType.hello, ProgramDefinitions.mydata.peerID, null, null, 0);
		MessagePacket n = new MessagePacket(nHeader, null);
		sendMessage(n);
		
		MessagePacket response = (MessagePacket) receiveMessage();
		if(DEBUG)
			response.print();
				
		try {
			AsymmetricKey.pubk = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(response.body));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		byte[] raw = SerialU.serialize(ProgramDefinitions.mydata);
		MessageHeader mHeader = new MessageHeader(MessageHeader.MessageType.peer_privkey, null, null, null, 0);
		MessagePacket m = new MessagePacket(mHeader, AsymmetricKey.encrypt(AsymmetricKey.pubk, raw));
		sendMessage(m);
		
		
		
		response = (MessagePacket) receiveMessage();
		if(DEBUG)
			response.print();
		this.accept.value = (response.header.getMessageType() == MessageHeader.MessageType.confirm);
		if(this.accept.value)
		{
			byte[] tmp = SymmetricKey.decryptData(ProgramDefinitions.mydata.priv_key, response.body);
			List<PeerData> tmpPD = (List<PeerData>) SerialU.deserialize(tmp);
			Metadata.setPeerMetadataList(tmpPD);
		}
		
		if(DEBUG)
			Metadata.printData();
		
		
	}
}
