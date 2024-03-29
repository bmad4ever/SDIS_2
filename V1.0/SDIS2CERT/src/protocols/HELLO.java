package protocols;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.SSLClient;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.AsymmetricKey;
import funtionalities.PeerMetadata;
import funtionalities.SerialU;
import funtionalities.SymmetricKey;


/**
 * <description of HELLO protocol>
 *
 */
public class HELLO extends TCP_Client{
		
	public HELLO(int p, String a, RefValue<Boolean> taskCompleted) {
		super(p,a,taskCompleted);
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;
		
		//send SSL
		MessageHeader nHeader = new MessageHeader(MessageHeader.MessageType.hello, ProgramDefinitions.mydata.peerID);
		MessagePacket n = new MessagePacket(nHeader, null);
		
		MessagePacket response = null;
		try {
			response = (MessagePacket) SSLClient.SendAndReceiveOne(ProgramDefinitions.CONTROL_ADDRESS, ProgramDefinitions.CONTROL_PORT_SSL, n);
		} catch (IOException e1) {	e1.printStackTrace();	};
		
		if(response==null)
		{
			return;
		}
		
		if(DEBUG) response.print();
		
		//get public assym key from response
		try {
			AsymmetricKey.pubk = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(response.body));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		//send our data to control
		byte[] raw = SerialU.serialize(ProgramDefinitions.mydata);
		byte[] raw2 = ProgramDefinitions.mydata.peerID.getBytes();
		Object[] tosend = {
				AsymmetricKey.encrypt(AsymmetricKey.pubk, raw),
				AsymmetricKey.encrypt(AsymmetricKey.pubk, raw2)
		}; 
		MessageHeader mHeader = new MessageHeader(MessageHeader.MessageType.peer_privkey, null);
		MessagePacket m = new MessagePacket(mHeader, SerialU.serialize(tosend));
		sendMessage(m);
		
		response = (MessagePacket) receiveMessage();
		if(response==null)
		{
			return;
		}
		
		if(DEBUG) response.print();
		this.taskCompleted.value = (response.header.getMessageType() == MessageHeader.MessageType.confirm);
		if(this.taskCompleted.value){
			byte[] tmp = SymmetricKey.decryptData(ProgramDefinitions.mydata.priv_key, response.body);
			HashSet<PeerData> tmpPD = (HashSet<PeerData>) SerialU.deserialize(tmp);
			PeerMetadata.setPeerMetadataList(tmpPD);
		}
		
		if(DEBUG)
			PeerMetadata.printData();

	}
}
