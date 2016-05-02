package Test;
	import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
	import java.net.ServerSocket;
	import java.net.Socket;
	import java.net.UnknownHostException;

import protocols.PUTCHUNK;

	public class Test_Client {
		 
		PUTCHUNK test;
		
	    public static void main(String[] args) throws UnknownHostException, InterruptedException {
	    	
	    	System.out.println("TEST CLIENT");
	    	 	
	    	PUTCHUNK client = new PUTCHUNK(50001, args[0]);
	    	client.start();
	    	
	    	
	    	try {System.in.read();} 
			catch (IOException e) {e.printStackTrace();}
			System.out.println("Closing down.");
			System.exit(0);
	    }

	}