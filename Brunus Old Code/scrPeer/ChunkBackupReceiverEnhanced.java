import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class ChunkBackupReceiverEnhanced extends CommunicationThread{

	static final boolean DEBUG = false;

	//MessageHeader hwci;//header with chunk info
	Message msg;
	private DatagramSocket mc_socket;
	private InetAddress mc_address;
	private int mc_port;

	final static int wait_time_b4closing=200;
	
	final static int chunk_not_owned_extra_wait_time=250;
	final static int default_max_total_wait_time=500;//a bit more than max 400
	final static int max_random_wait_time=default_max_total_wait_time-chunk_not_owned_extra_wait_time;
	
	static boolean removing = false;
	
	public ChunkBackupReceiverEnhanced(Message msg) throws IOException 
	{
		this.msg = msg;
		this.mc_socket = new DatagramSocket(null);
		this.mc_socket.setReuseAddress(true);
		this.mc_port = ProgramData.getMCport();
		this.mc_address = InetAddress.getByName(ProgramData.getMCadd());
	}

	public void run(){

		if(removing)
		{ 
			mc_socket.close(); 
			return;
		} 
		if(DEBUG) System.out.println("ChunkBackupReceive(started)");

		//check if the file origin is not from this server
		if(ProgramData.myfileslog.has_fileid(msg.header.FileId()))
		{
			if(DEBUG) System.out.println("ChunkBackupReceive():reeived putchunk of a client's file");
			mc_socket.close();
			return;
		}

		Boolean already_had_chunk_aux = ProgramData.chunkslog.owns_chunk(msg.header.FileId(),  msg.header.ChunkNo());
		//no need 2 save the same chunk twice, 
		//also useful to avoid cleaning list so it can used by processRemove thread

		boolean already_had_chunk = already_had_chunk_aux == null ? false: already_had_chunk_aux.booleanValue();

		if(!already_had_chunk){
		try {
		//extra sleep time if doesn't have chunk
		sleep(chunk_not_owned_extra_wait_time);
		}catch (Exception e){e.printStackTrace();}
		}

		//w8 random delay
		int sleeptime = WaitRandomDelay(0,max_random_wait_time);
		
		//check if number of stores received satisfies rep degree
		boolean num_of_stores_satisfy_rep_degree = ProgramData.mc_listener.get_answers_with(MessageType.stored,msg.header.FileId(),msg.header.ChunkNo()) >= msg.header.ReplicationDegree();
		
		//if num stored received < rep degreed then save file and send stored
		if(!num_of_stores_satisfy_rep_degree||already_had_chunk)
		{
			try {
				sendDatagramPacket(mc_socket,
						build_header(MessageType.stored,msg.header.FileId(),msg.header.ChunkNo(),Integer.toString(msg.header.ReplicationDegree()))
						,null,mc_address,mc_port, 3, 100);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ChunkBackupReceive:failed to send");
			}
			
			if(!already_had_chunk){

				//check available space
				if(ProgramData.storage_space_used>=ProgramData.max_storage_space_allowed)
				{
					removing=true;
					WaitRandomDelay(0,500);
					//START SPACE RECLAIMING PROTOCOL
					String remove[] = ProgramData.chunkslog.find_best_2remove();
					if(remove==null)
					{
						System.out.println("ChunkBackupReceive:Storage is full and can't reclaim any space! Received putchunk will be discarded.");

					} else{

						if(DEBUG) {
							String chunkname = remove[0] + "." + remove[1];
							System.out.println("ChunkBackupReceive:storage is full and chunk <"+ chunkname +"> will be removed");
						}
						new SpaceReclaimRemove(remove[0], remove[1]).start();

						try {sleep(1000);} 
						catch (InterruptedException e) {removing=false;}
					}
					mc_socket.close();
					removing=false;
					return;
				} else //got enough space
				{
					//save chunk
					ProgramData.saveChunkFile(msg.body,msg.header.FileId() , msg.header.ChunkNo());
					//if save is successful update used space
					ProgramData.update_storage(1);
					//update LOG
					ProgramData.chunkslog.update_chunk(msg.header.FileId(), msg.header.ChunkNo(), msg.header.ReplicationDegree(), 1,true);
				}
			}	
		}
			
		//w8 for the end of the 400ms interval and update number of copies of the chunk with the "real" value 
		//w8 to complete 400ms (+few extra just 2 b safe)
		//to get all STORED answers
		try {
			sleep(max_random_wait_time-sleeptime+wait_time_b4closing);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//get stored messages received and update number of copies n log
		int copies = ProgramData.mc_listener.process_answers_with(MessageType.stored,msg.header.FileId(),msg.header.ChunkNo());
		ProgramData.chunkslog.update_chunk(msg.header.FileId(), msg.header.ChunkNo(), msg.header.ReplicationDegree(),copies + 1,null);

		//clear "proof" (avoid some rare occasional conflict with reclaim protocol)
		if(!already_had_chunk)
			ProgramData.mdb_listener.clear_answers_with(MessageType.putchunk,msg.header.FileId(), msg.header.ChunkNo());


		mc_socket.close();
		if(DEBUG) System.out.println("ChunkBackupReceive(end)");
	}

}
