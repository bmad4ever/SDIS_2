package main;

import java.io.File;
import java.net.InetAddress;
import java.security.MessageDigest;

import FileSystem.DatabaseManager;
import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Server;
import funtionalities.PeerMetadata;
import funtionalities.PeriodicUpdates;
import funtionalities.SymmetricKey;
import protocols.HELLO;
import protocols.PEER_RESTORE_METADATA;
import userInterface.PeerUI;

public class PeerApp {

	public static void main(String[] args) {

		if(args.length!=4){
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
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(args[0].getBytes("UTF-8"));
			ProgramDefinitions.mydata = new PeerData(
					SymmetricKey.key, 
					InetAddress.getLocalHost().getHostAddress(),
					Integer.parseInt(args[2]),
					String.format("%064x",new java.math.BigInteger(1, md.digest())
							) );
		} catch (Exception e) {	e.printStackTrace();}

		//System.out.println(ProgramDefinitions.myID); if(true) return;

		String db_path=ProgramDefinitions.myID + File.separator + ProgramDefinitions.chunkDatabaseFileName;
		ProgramDefinitions.db = new DatabaseManager(db_path);

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
		(new Thread(new PeriodicUpdates())).start();//will save metadata on nonvolatile memory from time to time
		//(new Thread(new PeerRenewService())).start();

		//UI RELATED
		PeerUI.UI();

		//QUIT -------------------
		server.STOP();
		PeriodicUpdates.STOP();
		System.out.println("Closing down.");
		ProgramDefinitions.db.save();
		System.exit(0);
	}
}