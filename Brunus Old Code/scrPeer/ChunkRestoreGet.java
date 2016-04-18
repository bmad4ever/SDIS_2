import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class ChunkRestoreGet extends CommunicationThread{

		static final boolean DEBUG = false;
		
		String fileid;
		String filechunknumS;

		private DatagramSocket mc_socket;
		private InetAddress mc_address;
		int mc_port;
		
		final static int base_wait_time=400;
		final static int extra_wait_time=200;
	
		private boolean store_file = true;
		
		public ChunkRestoreGet(String fileid, int filechunknum,boolean store_file) throws IOException 
		{
			this( fileid,  filechunknum);
			this.store_file = store_file;
		}
		
		public ChunkRestoreGet(String fileid, int filechunknum/*,int repdeg, byte[] chunk*/) throws IOException 
		{
			this.fileid=fileid;
			this.filechunknumS=Integer.toString(filechunknum);

			try{
			this.mc_socket = new DatagramSocket();
			this.mc_address = InetAddress.getByName(ProgramData.getMCadd());
			mc_port = ProgramData.getMCport();
			}catch(Exception e){e.printStackTrace();}
		}
		
		//extra stuff. useful 2 know the result of the operation outside of this thread
		RefBoolean chunk_received;
		public void passQuestions(RefBoolean chunk_received)
		{
			this.chunk_received	= chunk_received;
		}
		
		public void run(){

		if(DEBUG) System.out.println("ChunkRestoreGet(started)");
		
				//send GETCHUNK
				try {
					sendDatagramPacket(mc_socket, 
							build_header(MessageType.getchunk,fileid,filechunknumS,"0"),
							null,mc_address,mc_port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//w8 4 answers
				try {
					sleep(base_wait_time+extra_wait_time);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				//get answer if any
				Message chunkmsg = ProgramData.mdr_listener.getNremove_msg(MessageType.chunk,fileid,filechunknumS);
				
				//maybe should remove other answers if any just 2 be safe???
				//-- now done before requesting! bcause one mag might arrive a little later-- ProgramData.mdr_listener.process_answers_with(MessageType.chunk,fileid,filechunknumS);
				
				if(chunkmsg==null)
				{
					if(DEBUG) System.out.println("ChunkRestoreGet:Did not receive chunk");
					if(chunk_received!=null) chunk_received.setValue(false);
					return;
				}
				
				//save chunk
				if(store_file) ProgramData.saveRestoredChunkFile(chunkmsg.body,chunkmsg.header.FileId() , chunkmsg.header.ChunkNo());//ProgramData.saveChunkFile(chunkmsg.body,chunkmsg.header.FileId() , chunkmsg.header.ChunkNo());
				if(chunk_received!=null) chunk_received.setValue(true);
				//if save is successful update used space
				//ProgramData.update_storage(true);
				//update LOG
				//ProgramData.chunkslog.update_chunk(chunkmsg.header.FileId(), chunkmsg.header.ChunkNo(), 
				//		chunkmsg.header.ReplicationDegree(),ChunksLog.increase_number_of_copies ,true);

			//message should not be kept after processing is done
			ProgramData.mdr_listener.clear_answers_with(MessageType.chunk,fileid,filechunknumS);
			
			mc_socket.close();
			if(DEBUG) System.out.println("ChunkRestoreGet(ended)");
		}
		
	}
