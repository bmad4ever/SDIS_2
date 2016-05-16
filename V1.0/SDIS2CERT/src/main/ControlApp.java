package main;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import Utilities.ProgramDefinitions;
import communication.SSLServer;
import communication.TCP_Server;
import funtionalities.AsymmetricKey;
import funtionalities.PeerMetadata;
import funtionalities.SymmetricKey;

public class ControlApp {
	public static void main(String[] args) {
		
		//initialize stuff 
		System.out.println("Server setting up...");
		
		System.setProperty("Djavax.net.ssl.keyStore","server.keys");
		System.setProperty("Djavax.net.ssl.keyStorePassword","123456");
		System.setProperty("Djavax.net.ssl.trustStore","truststore");
		System.setProperty("Djavax.net.ssl.trustStorePassword","123456");
		
		try{
			PeerMetadata.setDatabaseNames(
					"CONTROL" + File.separator +ProgramDefinitions.peerInfoDatabaseName , 
					"CONTROL" + File.separator +ProgramDefinitions.timestampsDatabaseName);
			PeerMetadata.INIT();
			
			
			AsymmetricKey.generate_key();
			SymmetricKey.generate_cipher();
			ProgramDefinitions.is_control = true;
			if(PeerMetadata.DEBUG)
				PeerMetadata.printData();
		} catch (Exception e) {e.printStackTrace(); return;}
		
		//start SSL server
		SSLServer server_ssl = new SSLServer();
		server_ssl.start();
		

		//start server
		TCP_Server server = new TCP_Server(ProgramDefinitions.CONTROL_PORT);
		try {
			System.out.println("Control Server running on " + InetAddress.getLocalHost().getHostAddress() + ":" + ProgramDefinitions.CONTROL_PORT);
		} catch (UnknownHostException e1) {e1.printStackTrace();}
		server.start();
		(new Thread(new PeerMetadata())).start();//will save metadata on nonvolatile memory from time to time
		
		try {System.in.read();} 
		catch (IOException e) {e.printStackTrace();}
		System.out.println("Closing down.");
		System.exit(0);
	}
}