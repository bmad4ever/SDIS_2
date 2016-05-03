package Test;
	import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import communication.TCP_ServerPeer;
import protocols.PUTCHUNK;

	public class Test_Server {
		 
		PUTCHUNK test;
		
	    public static void main(String[] args) throws UnknownHostException, InterruptedException {
	    		
	    	System.out.println("TEST SERVER: " + InetAddress.getLocalHost().getHostName() + " : " + InetAddress.getLocalHost().getHostAddress());
	    	
	    	TCP_ServerPeer server = new TCP_ServerPeer(50001);
	    	server.start();
	    	
	    	
	    	try {System.in.read();} 
			catch (IOException e) {e.printStackTrace();}
			System.out.println("Closing down.");
			System.exit(0);
	    	
	    }

	}