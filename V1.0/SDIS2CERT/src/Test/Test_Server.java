package Test;
	import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
	import java.net.Socket;
	import java.net.UnknownHostException;

import communication.TCP_ServerCentral;
import protocols.PUTCHUNK;

	public class Test_Server {
		 
		PUTCHUNK test;
		
	    public static void main(String[] args) throws UnknownHostException, InterruptedException {
	    		
	    	System.out.println("TEST SERVER: " + InetAddress.getLocalHost().getHostName() + " : " + InetAddress.getLocalHost().getHostAddress());
	    	
	    	TCP_ServerCentral server = new TCP_ServerCentral(50001);
	    	server.start();
	    	
	    	
	    	try {System.in.read();} 
			catch (IOException e) {e.printStackTrace();}
			System.out.println("Closing down.");
			System.exit(0);
	    	
	    }

	}