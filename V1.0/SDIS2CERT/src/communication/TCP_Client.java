package communication;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import java.net.InetAddress;

public class TCP_Client extends TCP_Thread{
	
	public TCP_Client(int p, String a) {
		port = p;
		try {
			adress = InetAddress.getByName(a);
		} catch (UnknownHostException e) {e.printStackTrace();}
	}
	
	@Override
	public void run() {
		baserun();
	}
	
	public void baserun() {
		try {		
			socket = new Socket(adress, port);
		} catch (IOException e) {
			e.printStackTrace();
			failed_init = true;
		}
	}
}