package main;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;

import FileSystem.DatabaseManager;
import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Server;
import funtionalities.PeerMetadata;
import funtionalities.SymmetricKey;
import protocols.HELLO;
import userInterface.PeerUI;

public class PeerApp {

	public static void main(String[] args) {

		if(args.length!=4) {
			System.out.println("cred <ID> <password> <port number> <Control IP Address> ");
			return;
		}

		ProgramDefinitions.is_control = false;
		ProgramDefinitions.myID = new String(args[0]);

		File chunkFolder = new File(ProgramDefinitions.myID);
		if(!chunkFolder.exists())
			chunkFolder.mkdir();

		System.setProperty("Djavax.net.ssl.keyStore","client.keys");
		System.setProperty("Djavax.net.ssl.keyStorePassword","123456");
		System.setProperty("Djavax.net.ssl.trustStore","truststore");
		System.setProperty("Djavax.net.ssl.trustStorePassword","123456");

		SymmetricKey.generate_key(args[0]+args[1]);

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			ProgramDefinitions.mydata = new PeerData(
					SymmetricKey.key, 
					InetAddress.getLocalHost().getHostAddress(),
					Integer.parseInt(args[2]),
					new String(md.digest(args[0].getBytes("UTF-8"))) );
		} catch (Exception e) {	e.printStackTrace();}

		//System.out.println(ProgramDefinitions.myID); if(true) return;

		ProgramDefinitions.db = new DatabaseManager(ProgramDefinitions.myID + File.separator + ProgramDefinitions.chunkDatabaseFileName);

		PeerMetadata.setDatabaseNames(
				ProgramDefinitions.myID + File.separator +ProgramDefinitions.peerInfoDatabaseName , 
				ProgramDefinitions.myID + File.separator +ProgramDefinitions.timestampsDatabaseName);
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

		try {
			client.join();	
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if(!accept.value) {
			System.out.println("Control Unavailable or Service denied");
			server.STOP();
			return;
		}

		//START PASSIVE PERIODIC STUFF -------------------
		//(new Thread(new PeerMetadata())).start();//will save metadata on nonvolatile memory from time to time
		//(new Thread(new PeerRenewService())).start();

		//UI RELATED
		PeerUI.UI();

		//QUIT -------------------
		server.STOP();
		System.out.println("Closing down.");
		System.exit(0);
	}
}