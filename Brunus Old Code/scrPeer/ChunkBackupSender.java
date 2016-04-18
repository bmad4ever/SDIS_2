import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class ChunkBackupSender extends CommunicationThread{

	static final boolean DEBUG = false;

	//MessageHeader hwci;//header with chunk info
	String fileid;
	String filechunknumS;
	String repdegS;
	int repdeg;
	byte[] chunk;
	private DatagramSocket mdb_socket;
	private InetAddress mdb_address;
	private boolean sendStore = false;
	private boolean count_delf=true;
	
	int mc_port;
	int mdb_port;

	public ChunkBackupSender(String fileid, String filechunknum,int repdeg, byte[] chunk, boolean send_store, boolean dontcountself) throws IOException 
	{
		this(fileid, filechunknum, repdeg, chunk);
		this.sendStore = send_store;
		this.count_delf = dontcountself;
	}
	
	public ChunkBackupSender(String fileid, String filechunknum,int repdeg, byte[] chunk, boolean send_store) throws IOException 
	{
		this(fileid, filechunknum, repdeg, chunk);
		this.sendStore = send_store;
	}

	public ChunkBackupSender(String fileid, int filechunknum,int repdeg, byte[] chunk) throws IOException 
	{
		this(fileid, Integer.toString(filechunknum), repdeg, chunk);
	}
	
	public ChunkBackupSender(String fileid, String filechunknum,int repdeg, byte[] chunk) throws IOException 
	{
		//this.hwci = mh;
		//this.chunk = mb;
		this.fileid=fileid;
		this.filechunknumS=filechunknum;
		this.repdegS=Integer.toString(repdeg);
		this.repdeg = repdeg;
		this.chunk = chunk;

		try{
			this.mdb_socket = new DatagramSocket();
			this.mdb_address = InetAddress.getByName(ProgramData.getMDBadd());
			mdb_port = ProgramData.getMDBport();//base_port + port_aux;
		}catch(Exception e){e.printStackTrace();}

		//mc_socket.setTimeToLive(1);
		//mdb_socket.setTimeToLive(1);
	}

	//extra stuff. useful 2 know the result of the operation outside of this thread
	RefBoolean stored_received , replication_degree_achieved;
	public void passQuestions(RefBoolean stored_received, RefBoolean replication_degree_achieved)
	{
		this.stored_received			 = stored_received;
		this.replication_degree_achieved = replication_degree_achieved;
	}

	public void run(){

		if(DEBUG) System.out.println("ChunkBackupSender(started)");

		long wait_time=1000;
		long wait_time_send = 200+1*100;

		//in case this is not the origin of the chunk file (ex.: after processing removed) check if we got  copy
		Boolean has_chunk_aux = ProgramData.chunkslog.owns_chunk(fileid, filechunknumS);
		boolean has_chunk = has_chunk_aux == null ? false: has_chunk_aux.booleanValue();
		int expected_number_of_copies=0;

		//clean previous stored of the chunk 2 avoid possible mistakes
		ProgramData.mc_listener.clear_answers_with(MessageType.stored,fileid,filechunknumS);

		//cycle send putchunk N receive stores
		int num_iterations = count_delf? 5:2;//use less iterations if its a "space reclaim" putchunk
		for(int i = 1; i<=num_iterations;++i)
		{
			if(DEBUG) System.out.println("ChunkBackupSender iteration "+i);

			//send chunk
			try {
				sendDatagramPacket(mdb_socket, 
						build_header(MessageType.putchunk,fileid,filechunknumS,repdegS),
						chunk,mdb_address,mdb_port);
			} catch (IOException e) {
				e.printStackTrace();
			}

			long time_send = 0; // this is the time we deduct to the time interval between iterations
			if (sendStore) {

				try {
					sleep(wait_time_send);
					time_send = wait_time_send;
					DatagramSocket mc_socket = new DatagramSocket();
					int mc_port = ProgramData.getMCport();
					InetAddress mc_address = InetAddress.getByName(ProgramData.getMCadd());
					sendDatagramPacket(mc_socket,
							build_header(MessageType.stored,fileid,filechunknumS,repdegS)
							,null,mc_address,mc_port,2,100);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("ChunkRestoreSend: failed to send stored");
				} catch (Exception a) {
					// TODO Auto-generated catch block
					a.printStackTrace();
				} 
			}


			expected_number_of_copies=0;
			//w8 4 answers
			if(stored_received!=null) 
			{
				stored_received.setValue(false);//extra stuff
				replication_degree_achieved.setValue(false);//extra stuff
			}
			expected_number_of_copies=0;
			try{	
				sleep(wait_time - time_send);
				wait_time = wait_time*2;
				expected_number_of_copies = ProgramData.mc_listener.process_answers_with(MessageType.stored,fileid,filechunknumS);
				if(has_chunk && count_delf) expected_number_of_copies++;//count myself if I got a copy of the chunk (in case I'm not the file origin peer)
			} catch (Exception e){e.printStackTrace();}

			//if chunk has enough copies then leave, if not repeat
			if(stored_received!=null && expected_number_of_copies>0) stored_received.setValue(true);//extra stuff
			if(expected_number_of_copies>=repdeg) {
				if(stored_received!=null) replication_degree_achieved.setValue(true);//extra stuff
				break;
			}
		}

		//in case this is not the origin of the chunk file (ex.: after processing removed) update num of copies 
		if(has_chunk) ProgramData.chunkslog.update_chunk(fileid, filechunknumS, ChunksLog.keep_rep_degree, expected_number_of_copies, null);

		mdb_socket.close();
		if(DEBUG) System.out.println("ChunkBackupSender(ended)");
	}

}

