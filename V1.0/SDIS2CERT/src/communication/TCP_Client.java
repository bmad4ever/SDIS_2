package communication;

import java.net.Socket;
import java.net.UnknownHostException;

import Utilities.RefValue;

import java.net.InetAddress;

public class TCP_Client extends TCP_Thread{
	
	static final int SOCKET_TIMEOUT=4000;
	
	/**<p>informs if the thread completed it's job successfully (not necessarily to catch errors but t know if it completed the given task)/p>
	 * <p>will be initialized with false in constructor if != null</p>
	 * */
	protected RefValue<Boolean> taskCompleted;
	
	public TCP_Client(int p, String a,RefValue<Boolean> taskCompleted) {
		if (taskCompleted != null){
			this.taskCompleted = taskCompleted;
			this.taskCompleted.value = false;
		}
		port = p;
		try {
			address = InetAddress.getByName(a);
		} catch (UnknownHostException e) {e.printStackTrace();}
	}
	
	/*@Override
	public void run() {
		baserun();
	}*/
	
	public void baserun() {
		try {
			socket = new Socket(address, port);
			socket.setSoTimeout(SOCKET_TIMEOUT);
		} catch (Exception e) {
			//e.printStackTrace();
			failed_init = true;
		}
	}
}