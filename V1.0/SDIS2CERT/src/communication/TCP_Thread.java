package communication;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCP_Thread extends Thread {

	protected ServerSocket serverSocket;
	protected Socket socket;
	protected ObjectOutputStream socketWrite;
	protected ObjectInputStream socketRead;

	protected InetAddress address;
	public int port;

	protected boolean failed_init = false;

	protected final boolean DEBUG = false;

	/**returns true if message was sent successfully*/
	public boolean sendMessage(Object obj){
		try{
			if (socketWrite == null) socketWrite = new ObjectOutputStream(socket.getOutputStream());
			if(!failed_init){
				socketWrite.writeObject(obj);
			}
		}catch (Exception e) {e.printStackTrace(); return false;}
		return true;
	}

	public Object receiveMessage() {
		try {
			if (socketRead==null) socketRead = new ObjectInputStream(socket.getInputStream());
			if(!failed_init) {
				return socketRead.readObject();
			}else
				return null;
		} catch (Exception e) {
			e.printStackTrace(); 
			return null;
		}
	}

	public void closeSocket()
	{
		try{
		socket.close();
		}catch(Exception e){}
	}
}