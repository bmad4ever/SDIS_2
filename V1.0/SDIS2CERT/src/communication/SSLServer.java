package communication;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.AsymmetricKey;

public class SSLServer extends Thread{

	static final boolean DEBUG = true;

	volatile protected boolean stop = false;
	public void STOP() {stop=true;};
	
	public void run(int port)  {

		SSLServerSocket sslServerSocket=null;
		ObjectInputStream socketRead=null;
		ObjectOutputStream socketWrite=null;
		
		try {
			sslServerSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		//sslServerSocket.setNeedClientAuth(true);
		//sslSocket.getSoTimeout();
		
		while (!stop) {
			SSLSocket sslSocket=null;

			try {
				sslSocket = (SSLSocket) sslServerSocket.accept();

			socketRead = new ObjectInputStream(sslSocket.getInputStream());
			socketWrite = new ObjectOutputStream(sslSocket.getOutputStream());

			MessagePacket msg = (MessagePacket) socketRead.readObject() ;
			
			socketWrite.writeObject(getResponse(msg));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
			socketRead.close();
			socketWrite.close();
			sslSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	protected static Object getResponse(MessagePacket receivedMSG)
	{
		switch (receivedMSG.header.getMessageType()) {
		case hello:
			if(DEBUG) System.out.println("Service type: HELLO");
			MessageHeader header = new MessageHeader(
					MessageHeader.MessageType.cred_pubkey
					,"CONTROL");
			byte[] body = AsymmetricKey.pubk.getEncoded();
			MessagePacket msg = new MessagePacket(header, body);
			return msg;
		default:
			break;
		}
		return null;
	}

}