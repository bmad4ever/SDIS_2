
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

	//static boolean quit_program = false;

	//[start] test stuff ====================================================

	final static boolean TEST_STUFF = false;
	final static boolean TEST_file_read_write=false;
	final static boolean TEST_header_parsing=false;
	final static boolean TEST_file_writer=false;
	final static boolean TEST_file_store=false;
	final static boolean TEST_listener_funcionalities=false;
	final static boolean TEST_putchunk_timeout=false;
	final static boolean TEST_delete=false;
	final static boolean TEST_sendRequestRestore = false;
	final static boolean TEST_remove = false;
	final static boolean TEST_en1 = false;



	final static boolean HACKED_START = false;//jumps params parsing and forces a test startup

	final static byte[] test_small_chunk = "THIS IS MY TEXT SAMPLE 101".getBytes();

	static void test_file_read_write() {
		if(!TEST_file_read_write)return;
		System.out.println("-----test_file_read_write-----");

		//ChunksLog la = new ChunksLog("peer123.txt");

		//la.log_save_new_chunk_info("mymac1.mypathandfile1.moddate1", 1, ReplicationDeg);


		//la.put_chunk("myidexampleDD112365", 0, 6, 8);

		//System.out.println("---" + Integer.toString( la.get_available_storage_capacity() ));

		//la.update_chunk("myidexampleDD112365", Integer.toString(0), 6, ChunksLog.decrease_number_of_copies,false);
		//System.out.println("---" + Integer.toString( la.get_available_storage_capacity() ));

		ProgramData.setProgramData(1,12,"225.0.0.1", "225.0.0.2", "225.0.0.3",30000,30001,30002);
		/*Testthread[] t1 = new Testthread[100];
		for(int i = 0; i<100; ++i)
			t1[i] = new Testthread(Integer.toString(i), i);
		for(int i = 0; i<100; ++i) t1[i].start();*/

		MyFilesLog mfl = new MyFilesLog("myfileslog_1.txt");

		/*mfl.save_new_file("supa12");
		mfl.save_new_file("supa1");
		mfl.save_new_file("supa2");
		mfl.save_new_file("supa3");
		mfl.save_new_file("supa4");
		mfl.save_new_file("LLLP");*/

		System.out.println(mfl.has_fileid("supa"));//false
		System.out.println(mfl.has_fileid("supa12"));//true
		System.out.println(mfl.has_fileid("LLLP"));//true
	}

	static void test_header_parsing() {
		if(!TEST_header_parsing)return;
		System.out.println("-----test_header_parsing-----");
		//REMOVED
	}

	static void test_file_writer()
	{
		if(!TEST_file_writer)return;
		System.out.println("-----test_file_writer-----");
		ProgramData.setProgramData(7,12, "225.0.0.1", "225.0.0.2", "225.0.0.3",30000,30001,30002);
		ProgramData.saveChunkFile("frgetrwefgenergew342231rw".getBytes(), "fidexample", "1");
	}

	static void test_chunk_store()
	{
		if(!TEST_file_store)return;
		System.out.println("-----test_file_store-----");


		ProgramData.setProgramData(7,12,"225.0.0.1", "225.0.0.2", "225.0.0.3",30000,30001,30002);

		try {
			MDB_Listener mdb = new MDB_Listener("225.0.0.2",30001);
			mdb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			new ChunkBackupSender("hhh",1,1,test_small_chunk).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*try {
				 byte[] buf = new byte[254];
				MulticastSocket socket = new MulticastSocket(30000);
				InetAddress address = InetAddress.getByName("225.0.0.2");
				socket.joinGroup(address);
				DatagramPacket dg =  new DatagramPacket(buf, buf.length);;
				socket.receive(dg);
				System.out.println("-----received-----");
				socket.close();
			 }catch (IOException e) {
					e.printStackTrace();
				}*/

	}

	static void test_listener_funcionalities()
	{
		if(!TEST_listener_funcionalities)return;
		System.out.println("-----test_listener_funcionalities-----");
		MC_Listener mc=null;
		try {
			mc = new MC_Listener("225.0.0.1", 30000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		/*mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.stored, "1.0", "7", "F1", "0",3));//1
		mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.stored, "1.0", "7", "F1", "0",3));//1

		mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.stored, "1.0", "4", "F1", "0",3));//2
		mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.stored, "1.0", "3", "F1", "0",3));//3

		mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.delete, "1.0", "4", "F1", "4",3));
		mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.delete, "1.0", "3", "F1", "3",3));

		mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.stored, "1.0", "7", "Faa2", "0",1));
		mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.stored, "1.0", "3", "FSS3", "0",2));
		mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.stored, "1.0", "7", "FasSSd1", "0",3));

		mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.stored, "1.0", "9", "CC", "0",3));
		mc.add2_msghd(new MessageHeader(CommunicationThread.MessageType.stored, "1.0", "9", "DD", "0",3));
		 */
		System.out.println(
				mc.process_answers_with(CommunicationThread.MessageType.stored, "F1", "0")//expected 3
				);
	}

	static void test_putchunk()
	{
		if(!TEST_putchunk_timeout)return;
		System.out.println("-----test_putchunk_timeout-----");

		ProgramData.setProgramData(8,12, "230.0.0.3", "230.0.0.4", "230.0.0.5",8888,8889,8890);
		ProgramData.start_listeners();

		//ChunkBackupSender sender=null;
		//ConcatFileChunk.backUpFile("/Users/hacker/Downloads/ferrari.jpg", 1);



		/*try {
			//mc = new MC_Listener("225.0.0.1", 30000);
			sender= new ChunkBackupSender("fid111", 6, 2, test_small_chunk);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		/*try {
			DatagramPacket msgPacket = new DatagramPacket(test_small_chunk, test_small_chunk.length,InetAddress.getByName("230.0.0.4"),8889);
			DatagramSocket socket = new DatagramSocket();
			socket.send(msgPacket);
			System.out.println("sent");
			socket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		//sender.start();



	}

	static void test_sendRequestRestore()
	{
		if(!TEST_sendRequestRestore)return;
		System.out.println("-----TEST_sendretores-----");
		ProgramData.setProgramData(8,12,"230.0.0.3", "230.0.0.4", "230.0.0.5",8888,8889,8890);
		ProgramData.start_listeners();	
		try {
			new ChunkRestoreGet("cafe91c17ac78cc0a97faf32ae0bc2a223799165f7ea7f77f474bc4d0c0823ca",2).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void test_delete() throws SocketException {

		if(!TEST_delete)return;

		ProgramData.setProgramData(8,12, "230.0.0.3", "230.0.0.4", "230.0.0.5",8888,8889,8890);
		ProgramData.start_listeners();

		DatagramSocket socket = new DatagramSocket();
		int port = ProgramData.getMCport();
		InetAddress inetaddress;
		try {
			inetaddress = InetAddress.getByName(ProgramData.getMCadd());
			CommunicationThread.sendDatagramPacket(socket, CommunicationThread.build_header(CommunicationThread.MessageType.delete, "cafe91c17ac78cc0a97faf32ae0bc2a223799165f7ea7f77f474bc4d0c0823ca", "2", "1"), null, inetaddress, port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void test_remove() {
		if(!TEST_remove)return;

		ProgramData.setProgramData(8,12, "230.0.0.3", "230.0.0.4", "230.0.0.5",8888,8889,8890);
		ProgramData.start_listeners();
		SpaceReclaimRemove rmv = new SpaceReclaimRemove("cafe91c17ac78cc0a97faf32ae0bc2a223799165f7ea7f77f474bc4d0c0823ca", "1");
		rmv.start();
	}

	public static void test_newstuff()
	{
		if(!TEST_en1) return;
		System.out.println("-----TEST_en1-----");
		ProgramData.setProgramData(12000,12, "239.0.0.3", "239.0.0.4", "239.0.0.5",8888,8889,8890);
		
		Map<Integer,Map<String,List<String>>> toRem= ProgramData.chunkslog.find_safer2_remove();
		
		System.out.println("- - - 1st test - - -"); 
		for(Integer i:toRem.keySet())
		{
			Map<String,List<String>> files = toRem.get(i);
			for(String fileid:files.keySet())
			{
				List<String> chunks = files.get(fileid); 
				for(String chunk:chunks)
				{
					System.out.println(">> " + i + " >> " + fileid + "|" + chunk );
				}
			}
		}
		
		System.out.println("- - - 2nd test - - -"); 
		Map<String,List<String>> fileschunks = ProgramData.chunkslog.findAllChunks_with1Copy();
		for(String fileid:fileschunks.keySet())
		{
			List<String> chunks = fileschunks.get(fileid); 
			for(String chunk:chunks)
			{
				System.out.println(">> " + fileid + "|" + chunk );
			}
		}
		

		System.out.println("- - - 3rd test - - -"); 
		Map<String, String> missing1 = ProgramData.chunkslog.find_1missingChunk_forAllFiles();
		for(String fileid:missing1.keySet())
		{
			String chunk = missing1.get(fileid); 
			System.out.println(">> " + fileid + "|" + chunk );				
		}

		System.out.println("- - - 4th test - - -"); 
		Map<String, String> higherthan1 = ProgramData.chunkslog.find_1Chunk_withRepDegHigherThan1_forAllFiles();		
		for(String fileid:higherthan1.keySet())
		{
			String chunk = higherthan1.get(fileid); 
			System.out.println(">> " + fileid + "|" + chunk );				
		}
		
		System.out.println("- - - 5th test - - -"); 

		(new ChunkBackupSpamming()).start();
		
		System.out.println("- - - ended - - -"); 
		
	}

	//[end] test stuff ====================================================

	private static Pattern pattern;
	private static Matcher matcher;

	private static final String IPADDRESS_PATTERN = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	public static boolean testInteger(String s) {
		try { 
			Integer.parseInt(s); 
		} catch(NumberFormatException e) { 
			return false; 
		} catch(NullPointerException e) {
			return false;
		}
		return true;
	}

	public static boolean IPAddressValidator(final String ip) {	
		pattern = Pattern.compile(IPADDRESS_PATTERN);
		matcher = pattern.matcher(ip);
		return matcher.matches();	    	    
	}
	
	public static void printUsage() {
		System.out.println("Usage: java AppServer <peer id> <version> <initial storage size> <MC ip> <MC port> <MDB ip> <MDB port> <MDR ip> <MDR port>\n");
	
		System.out.println();
		System.out.println("- - - - - - - - - - - - - - - - - - - - - -");
		System.out.println("<peer id> will be the identifier for this peer and port used to communicate with the Interface.");
		System.out.println();
		System.out.println("Any given port should be available and in between 1024 and 65535");
		System.out.println();
		System.out.println("Storage Size is measured in number of chunks (64KB)");
		System.out.println();
		System.out.println("MC, MDB and MDR must have a valid multicast address in between 224.0.0.0 and 239.255.255.255");
		System.out.println("We advise following the RFC2365 : \"The administratively scoped IPv4 multicast address space is defined to be the range 239.0.0.0 to 239.255.255.255.\"");
		System.out.println();
		System.out.println("To close the peer write stop on the console and press Enter. The peer might take some time before closing.");
	}

	public static void wait4StopCommand()
	{
		try {
			String input = "";

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			do{
				input=br.readLine();
			} while(!input.toUpperCase().equals("STOP"));
		}
		catch (Exception e){e.printStackTrace();}
	}
	
	public static class Arg {
		   public static final int PEER_ID=0,ADDRESS=0
				   ,VERSION=1,STORAGE_INITIAL_SIZE=2
				   ,PORT=1,MC=3,MDB=5,MDR=7;
		}
	
	public static void main(String[] args) throws IOException {

		//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		if(TEST_STUFF) //run tests here if needed
		{
			test_file_read_write();
			test_header_parsing();
			test_file_writer();
			test_chunk_store();
			test_listener_funcionalities();
			test_putchunk();
			test_delete();
			test_sendRequestRestore();
			test_remove();
			test_newstuff();
			return;
		}//--- END TEST SECTION ------------------------------

		if(!HACKED_START)
		{
			if(args.length != 9)
			{
				printUsage();
				return;
			}

			int peer_id = -1, mc_port = -1, mdb_port = -1, mdr_port = -1,storageinitialsize=-1;
			
			if (!testInteger(args[Arg.PEER_ID])) {
				System.out.println("\nDEBUG: <peer id> not valid!");
				printUsage();
				return;
			} else {
				peer_id = Integer.parseInt(args[Arg.PEER_ID]);
			}
			if (!IPAddressValidator(args[Arg.MC+Arg.ADDRESS])) {
				System.out.println("\nDEBUG: <MC ip> not valid!");
				printUsage();
				return;
			}
			if (!testInteger(args[Arg.MC+Arg.PORT])) {
				System.out.println("\nDEBUG: <MC port> not valid!");
				printUsage();
				return;
			} else {
				mc_port = Integer.parseInt(args[Arg.MC+Arg.PORT]);
			}
			if (!IPAddressValidator(args[Arg.MDB+Arg.ADDRESS])) {
				System.out.println("\nDEBUG: <MDB ip> not valid!");
				printUsage();
				return;
			}
			if (!testInteger(args[Arg.MDB+Arg.PORT])) {
				System.out.println("\nDEBUG: <MDB port> not valid!");
				printUsage();
				return;
			} else {
				mdb_port = Integer.parseInt(args[Arg.MDB+Arg.PORT]);
			}
			if (!IPAddressValidator(args[Arg.MDR+Arg.ADDRESS])) {
				System.out.println("\nDEBUG: <MDR ip> not valid!");
				printUsage();
				return;
			}
			if (!testInteger(args[Arg.MDR+Arg.PORT])) {
				System.out.println("\nDEBUG: <MDR port> not valid!");
				printUsage();
				return;
			} else {
				mdr_port = Integer.parseInt(args[Arg.MDR+Arg.PORT]);
			}
			
			if(!(args[Arg.VERSION].equals("1.0")||args[Arg.VERSION].equals("2.0")))
			{
				System.out.println("Only 1.0 and 2.0 versions allowed!");
				printUsage();
				return;
			}
			
			storageinitialsize = Integer.parseInt(args[Arg.STORAGE_INITIAL_SIZE]);
			if(storageinitialsize<=0)
			{
				System.out.println("Storage size must be greater than 0");
				printUsage();
				return;
			}
			
			if(args[Arg.VERSION].equals("2.0"))
				MulticastListener.version = CommunicationThread.Version.two;
			
			/*System.out.println("\nDEBUG: " + peer_id);
			System.out.println("DEBUG: " + args[1]);
			System.out.println("DEBUG: " + mc_port);
			System.out.println("DEBUG: " + args[3]);
			System.out.println("DEBUG: " + mdb_port);
			System.out.println("DEBUG: " + args[5]);
			System.out.println("DEBUG: " + mdr_port);*/
			ProgramData.setProgramData(peer_id,storageinitialsize,args[Arg.MC+Arg.ADDRESS],args[Arg.MDB+Arg.ADDRESS],args[Arg.MDR+Arg.ADDRESS],mc_port,mdb_port,mdr_port);
			ProgramData.start_listeners();
			TestAppServer testAppServer = new  TestAppServer(peer_id);
			testAppServer.start();
			
			ChunkBackupSpamming spammer=null;
			if(args[Arg.VERSION].equals("2.0"))
			{
				MulticastListener.version = CommunicationThread.Version.two;
				spammer = new ChunkBackupSpamming(); 
				spammer.start();
			}
			
			//terminate SERVER
			wait4StopCommand();
			testAppServer.STOP();
			ProgramData.stop_listeners();
			if(spammer!=null) spammer.STOP();
		}
		else {
			int i = 444;
			ProgramData.setProgramData(12000+i,12, "239.168.1.3", "239.168.1.4", "239.168.1.5",8888,8889,8890);
			ProgramData.start_listeners();
			TestAppServer testAppServer = new  TestAppServer(12000+i);
			testAppServer.start();
			
			//terminate SERVER
			wait4StopCommand();
			testAppServer.STOP();
			ProgramData.stop_listeners();
		}
	 
	}

}

