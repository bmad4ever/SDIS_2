package Utilities;

import FileSystem.DatabaseManager;

public class ProgramDefinitions {

	static final public int MAX_SERVICE_THREADS = 10;
	static final public int NUMBER_OF_PUTCHUNK_TRIES = 5;
	static final public String delete_message="b76r5e4wx8YVTsrsEYBYTFUBY7t8b96r96bBFIBTF66c6VUbYTFCfvuy";
	static final public String SYMM_KEY_ALGORITHM = "AES";
	
	static public long timestamp = 0;
	static public boolean is_control = false;
	static public PeerData mydata;
	static public String myID;
	static public int CONTROL_PORT = 50123;
	static public int CONTROL_PORT_SSL = 50124;
	static public String CONTROL_ADDRESS;
	
	static public String chunkDatabaseFileName = "chunkMetadata.ser";
	static public String peerInfoDatabaseName = "peerMetadata.ser"; 
	static public String timestampsDatabaseName = "timestampsMetadata.ser"; 
	
	static public final String recoveredFilesFolderName = "RECOVERED FILES";
	
	static public DatabaseManager db;
}
