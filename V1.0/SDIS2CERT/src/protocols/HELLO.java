package protocols;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import Utilities.AsymmetricKey;
import Utilities.ProgramDefinitions;
import Utilities.SerialU;
import communication.MessageHeader;
import communication.MessagePacket;
import communication.TCP_Client;


/**
 * <description of HELLO protocol>
 *
 */
public class HELLO extends TCP_Client{

	public HELLO(int p, String a) {
		super(p,a);
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;
		
		MessagePacket n = new MessagePacket(MessageHeader.MessageType.hello,null,null,null,0,0,null);
		sendMessage(n);
		
		MessagePacket response = (MessagePacket) receiveMessage();
		response.print();
				
		try {
			AsymmetricKey.pubk = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(response.body));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		byte[] raw = SerialU.serialize(ProgramDefinitions.mydata);		
		MessagePacket m = new MessagePacket(MessageHeader.MessageType.peer_privkey,null,null,null,0,0, AsymmetricKey.encrypt(AsymmetricKey.pubk, raw));
		sendMessage(m);
		
		
	}
	
	
}
