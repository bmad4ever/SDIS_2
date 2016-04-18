import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class ChunkBackupReceiver extends CommunicationThread{

	static final boolean DEBUG = false;

	//MessageHeader hwci;//header with chunk info
	Message msg;
	private DatagramSocket mc_socket;
	private InetAddress mc_address;
	private int mc_port;

	final static int wait_time_b4closing=200;

	static boolean removing = false;
	
	public ChunkBackupReceiver(Message msg) throws IOException 
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
		if(DEBUG) System.out.println("ChunkBackupReceive(started)" );

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
			//check available space
			if(ProgramData.storage_space_used>=ProgramData.max_storage_space_allowed)
			{
				removing=true;
				WaitRandomDelay(0,500);
				//START SPACE RECLAIMING PROTOCOL
				String remove[] = ProgramData.chunkslog.find_best_2remove();
				if(remove==null)
				{
					if(DEBUG) System.out.println("ChunkBackupReceive:Storage is full and can't reclaim any space! Received putchunk will be discarded.");
					removing=false;
					mc_socket.close();
					return;
				}
				
				if(DEBUG) {
					String chunkname = remove[0] + "." + remove[1];
					System.out.println("ChunkBackupReceive:storage is full and chunk <"+ chunkname +"> will be removed");
				}
				new SpaceReclaimRemove(remove[0], remove[1]).start();
				try {sleep(1000);} 
				catch (InterruptedException e) {removing=false;}
				removing=false;
				return;
			}

			//save chunk
			if (DEBUG) System.out.println("-----ChunkBackupReceive: bodysize:"+msg.body.length);
			ProgramData.saveChunkFile(msg.body,msg.header.FileId() , msg.header.ChunkNo());
			//if save is successful update used space
			ProgramData.update_storage(1);
			//update LOG
			ProgramData.chunkslog.update_chunk(msg.header.FileId(), msg.header.ChunkNo(), msg.header.ReplicationDegree(), 1,true);
		}

		//send STORED via MC
		int sleeptime = WaitRandomDelay(0,400);
		try {
			sendDatagramPacket(mc_socket,
					build_header(MessageType.stored,msg.header.FileId(),msg.header.ChunkNo(),Integer.toString(msg.header.ReplicationDegree()))
					,null,mc_address,mc_port, 3, 100);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ChunkBackupReceive:failed to send");
		}

		//assuming it might be coming due 2 a remove... 
		//can a putchunk with a new replication degree appear? 
		//if so then this might need 2 be changed
		if(already_had_chunk) return;


		//w8 to complete 400ms (+few extra just 2 b safe)
		//to get all STORED answers
		try {
			sleep(400-sleeptime+wait_time_b4closing);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//get stored messages received and update number of copies n log
		int copies = ProgramData.mc_listener.process_answers_with(MessageType.stored,msg.header.FileId(),msg.header.ChunkNo());
		ProgramData.chunkslog.update_chunk(msg.header.FileId(), msg.header.ChunkNo(), msg.header.ReplicationDegree(),copies + 1,null);

		//clear "proof" (avoid some rare occasional conflict with reclaim protocol)
		ProgramData.mdb_listener.clear_answers_with(MessageType.putchunk,msg.header.FileId(), msg.header.ChunkNo());

		mc_socket.close();
		if(DEBUG) System.out.println("ChunkBackupReceive(end)");
	}

}
