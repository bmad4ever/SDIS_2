package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCP_Thread extends Thread {

	protected ServerSocket testSer;
	protected Socket testSocket;
	protected ObjectOutputStream socketWrite;
	protected ObjectInputStream socketRead;

	protected InetAddress adress;
	protected int port;
	
	protected boolean failed_init = false;
	
	public void sendMessage(Object obj){
		try{
		if (socketWrite == null) socketWrite = new ObjectOutputStream(testSocket.getOutputStream());
		if(!failed_init)
			{
				socketWrite.writeObject(obj);
			}
		}catch (Exception e) {e.printStackTrace();}

	}

	public Object receiveMessage() {
		try{
		if (socketRead==null) socketRead = new ObjectInputStream(testSocket.getInputStream());
		if(!failed_init)
			{
			return socketRead.readObject();
			}
		else
			return null;
		} catch (Exception e) {e.printStackTrace(); return null;}
	}
	
	
}
