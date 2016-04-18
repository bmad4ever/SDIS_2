import java.io.IOException;


public class SpaceReclaimProcessRemove extends CommunicationThread{

	static final boolean DEBUG = false;

	//MessageHeader hwci;//header with chunk info
	Message msg;
	final static int wait_time_b4closing=0;

	public SpaceReclaimProcessRemove(Message msg) throws IOException 
	{
		this.msg = msg;
	}

	public void run(){

		if(DEBUG) System.out.println("ChunkRestoreSend(started)");

		//check if the file origin is not from this server
		if(ProgramData.myfileslog.has_fileid(msg.header.FileId()))
		{
			if(DEBUG) System.out.println("ChunkBackupReceive():reeived putchunk of a client's file");
			ProgramData.mdr_listener.clear_answers_with(MessageType.removed, msg.header.FileId(), msg.header.ChunkNo());
			return;
		}

		//has chunk?
		Boolean already_had_chunk_aux = ProgramData.chunkslog.owns_chunk(msg.header.FileId(),  msg.header.ChunkNo());
		//no need 2 save the same chunk twice, 
		//also useful to avoid cleaning list so it can used by processRemove thread
		boolean already_had_chunk = already_had_chunk_aux == null ? false: already_had_chunk_aux.booleanValue();
		if(!already_had_chunk) {
			ProgramData.mdr_listener.clear_answers_with(MessageType.removed, msg.header.FileId(), msg.header.ChunkNo());
			return;//cant update or retrieve what it aint got
		}


		//update chunk num of copies
		ProgramData.chunkslog.update_chunk(msg.header.FileId(), msg.header.ChunkNo(), ChunksLog.keep_rep_degree, ChunksLog.decrease_number_of_copies , null);

		//check if num copies >= rep degree, if not, no need to startbackup protocol
		//repdegree will have value=0 if num of copies is >= rep degree
		int repdegree = ProgramData.chunkslog.enough_copies_ofchunk(msg.header.FileId(),  msg.header.ChunkNo());
		if( repdegree<=0 ) {
			ProgramData.mdr_listener.clear_answers_with(MessageType.removed, msg.header.FileId(), msg.header.ChunkNo());
			return;//no need to start putchunk
		}


		//w8 b4 sending 
		//clean b4 waiting 2 avoid getting duplicate putchunks/stored received long ago b4 this call
		ProgramData.mdb_listener.clear_answers_with(MessageType.putchunk, msg.header.FileId(), 
				msg.header.ChunkNo());
		ProgramData.mdr_listener.clear_answers_with(MessageType.stored, msg.header.FileId(), 
				msg.header.ChunkNo());
		int sleeptime = WaitRandomDelay(0,400);
		
		
		//check if peers already started protocol
		if(ProgramData.mdb_listener.process_answers_with(MessageType.putchunk, msg.header.FileId(), 
				msg.header.ChunkNo())>0	) //already retrieved by some other peer
			//START DONOT PUTCHUNK
		{
			//get stored messages received and update number of copies n log
			int copies = ProgramData.mc_listener.process_answers_with(MessageType.stored,msg.header.FileId(),msg.header.ChunkNo());
			ProgramData.chunkslog.update_chunk(msg.header.FileId(), msg.header.ChunkNo(), 
					ChunksLog.keep_rep_degree	,copies + 1,true);

		}//END DONOT PUTCHUNK
		else{//start DO PUTCHUNK PROTOCOL
			//start backupprotocol
			try {
				new ChunkBackupSender(msg.header.FileId(),msg.header.ChunkNo(),repdegree,
						ProgramData.getFileChunk(msg.header.FileId(), msg.header.ChunkNo()), true ).start();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			////w8 to complete 400ms (+few extra just 2 b safe)
			//and clean possible messages caught after starting backupprotocol
			try {
				sleep(400-sleeptime+wait_time_b4closing);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ProgramData.mdb_listener.clear_answers_with(MessageType.putchunk, msg.header.FileId(), msg.header.ChunkNo());
			ProgramData.mdr_listener.clear_answers_with(MessageType.removed, msg.header.FileId(), msg.header.ChunkNo());

			//send stored 4 other peers to count number of copies ok
			/*try {
			DatagramSocket mc_socket = new DatagramSocket();
			int mc_port = ProgramData.getMCport();
			InetAddress mc_address = InetAddress.getByName(ProgramData.getMCadd());
			sendDatagramPacket(mc_socket,
			build_header(MessageType.stored,msg.header.FileId(),msg.header.ChunkNo(),Integer.toString(msg.header.ReplicationDegree()))
			,null,mc_address,mc_port );
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ChunkRestoreSend: failed to send stored");
		}*/


		}//end DO PUTCHUNK PROTOCOL

		if(DEBUG) System.out.println("ChunkRestoreSend(end)");
	}

}
