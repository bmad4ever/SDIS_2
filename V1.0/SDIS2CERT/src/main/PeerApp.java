package main;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import FileSystem.DatabaseManager;
import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Server;
import funtionalities.PeerMetadata;
import funtionalities.PeerRenewService;
import funtionalities.SymmetricKey;
import protocols.HELLO;

public class PeerApp {
	
	public static void main(String[] args) {

		if(args.length!=4){
			System.out.println("cred <ID> <password> <port number> <Control IP Address> ");
			return;
		}
		
		System.setProperty("Djavax.net.ssl.keyStore","client.keys");
		System.setProperty("Djavax.net.ssl.keyStorePassword","123456");
		System.setProperty("Djavax.net.ssl.trustStore","truststore");
		System.setProperty("Djavax.net.ssl.trustStorePassword","123456");

		SymmetricKey.generate_key(args[0]+args[1]);

		try {
			ProgramDefinitions.mydata = new PeerData(SymmetricKey.key, InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(args[2]), args[0]);
		} catch (NumberFormatException | UnknownHostException e) {	e.printStackTrace();}
		
		ProgramDefinitions.db = new DatabaseManager(ProgramDefinitions.mydata.peerID + File.separator + ProgramDefinitions.chunkDatabaseFileName);
		
		PeerMetadata.setDatabaseNames(
				ProgramDefinitions.mydata.peerID + File.separator +ProgramDefinitions.peerInfoDatabaseName , 
				ProgramDefinitions.mydata.peerID + File.separator +ProgramDefinitions.timestampsDatabaseName);
		PeerMetadata.INIT();
		
		//start server
		TCP_Server server = new TCP_Server(Integer.parseInt(args[2]));
		server.start();

		//Send HELLO to Control
		RefValue<Boolean> accept = new RefValue<Boolean>();
		accept.value = false;
		ProgramDefinitions.CONTROL_ADDRESS = args[3];
		HELLO client = new HELLO(ProgramDefinitions.CONTROL_PORT, ProgramDefinitions.CONTROL_ADDRESS, accept);
		client.start();
		try {client.join();	} 
		catch (InterruptedException e1) {	e1.printStackTrace();}

		if(!accept.value)
		{
			System.out.println("Service denied by control");
			return;
		}

		(new Thread(new PeerMetadata())).start();//will save metadata on nonvolatile memory from time to time
		(new Thread(new PeerRenewService())).start();


		try {System.in.read();} 
		catch (IOException e) {e.printStackTrace();}

		System.out.println("Closing down.");
		System.exit(0);
	}
}