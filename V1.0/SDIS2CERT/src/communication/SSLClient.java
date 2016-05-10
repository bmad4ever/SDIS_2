package communication;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLClient {

	/** sends and receives a single message via SSL socket*/
	public static Object SendAndReceiveOne(String host, int port, Object content) throws IOException {

		SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
		//sslSocket.setNeedClientAuth(true);
		//sslSocket.getSoTimeout();
		
		ObjectOutputStream socketWrite = new ObjectOutputStream(sslSocket.getOutputStream());
		ObjectInputStream  socketRead = new ObjectInputStream(sslSocket.getInputStream());

		socketWrite.writeObject(content);

		Object response=null;
		try {
			response = socketRead.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		socketWrite.close();
		socketRead.close();
		sslSocket.close();
		
		return response;
	}
}