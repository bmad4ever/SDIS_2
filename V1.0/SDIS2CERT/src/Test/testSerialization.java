package Test;

import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import Utilities.PeerData;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import communication.messages.MessageHeader.MessageType;
import funtionalities.AsymmetricKey;
import funtionalities.SerialU;

public class testSerialization {

    public static void main(String[] args) throws UnknownHostException, InterruptedException {
    	
    	System.out.println("TEST SERIAL");
    	
    	//testing wth public key on prvate key field (not default behaviour, done jus 2 test stuff)
    	try {
			AsymmetricKey.generate_key();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	MessageHeader head = new MessageHeader(
    			MessageType.hello,
    			"","","",0,1
    			);
    	
    	System.out.println(AsymmetricKey.pubk);
    	PeerData pd = new PeerData(AsymmetricKey.pubk.getEncoded(), "192.12.1.1", 32544, "monkey XP");
    	MessagePacket pck = new MessagePacket(head,SerialU.serialize(pd));

    	byte[] msg = SerialU.serialize(pck); 
    	
    	
    	
    	//deserealize
    	MessagePacket pckD = (MessagePacket) SerialU.deserialize( msg );
    	PeerData dataD = (PeerData) SerialU.deserialize(pckD.body);
    	System.out.println(dataD.peerID);
    	System.out.println(dataD.addr.ip);
    	try {
			System.out.println( KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(dataD.priv_key)) );
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	/*try {System.in.read();} 
		catch (IOException e) {e.printStackTrace();}
		System.out.println("Closing down.");
		System.exit(0);*/
    }
	
}
