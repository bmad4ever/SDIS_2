import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ChunkRestoreSend extends CommunicationThread{

	static final boolean DEBUG = false;
	
	//MessageHeader hwci;//header with chunk info
	Message msg;
	private DatagramSocket mdr_socket;
	private InetAddress mdr_address;
	private int mdr_port;
	
	final static int wait_time_b4closing=200;
	
	public ChunkRestoreSend(Message msg) throws IOException 
	{
		this.msg = msg;
		this.mdr_socket = new DatagramSocket();
		this.mdr_port = ProgramData.getMDRport();
		this.mdr_address = InetAddress.getByName(ProgramData.getMDRadd());
	}
	
	public void run(){
		
		if(DEBUG) System.out.println("ChunkRestoreSend(started)");
		
		//check if the file origin is not from this server
		if(ProgramData.myfileslog.has_fileid(msg.header.FileId()))
			{
				if(DEBUG) System.out.println("ChunkBackupReceive():reeived putchunk of a client's file");
						return;
			}
		
		//has chunk?
		Boolean already_had_chunk_aux = ProgramData.chunkslog.owns_chunk(msg.header.FileId(),  msg.header.ChunkNo());
		boolean already_had_chunk = already_had_chunk_aux == null ? false: already_had_chunk_aux.booleanValue();
		if(!already_had_chunk)
			return;//cant retrieve what it aint got
		
		//w8 b4 sending
		int sleeptime = WaitRandomDelay(0,400);
		if(ProgramData.mdr_listener.process_answers_with(MessageType.chunk, msg.header.FileId(), 
				msg.header.ChunkNo())>0	) return; //already retrieved by some other peer
		
		//get chunk 2 b sent
		byte[] chunk = ProgramData.getFileChunk(msg.header.FileId(), msg.header.ChunkNo());

		try {
			sendDatagramPacket(mdr_socket,
			build_header(MessageType.chunk,msg.header.FileId(),msg.header.ChunkNo(),Integer.toString(msg.header.ReplicationDegree()))
			,chunk,mdr_address,mdr_port );
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ChunkRestoreSend to send");
		}

		//w8 to complete 400ms (+few extra just 2 b safe)
		try {
			sleep(400-sleeptime+wait_time_b4closing);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//clean received to avoid possible problems later
		ProgramData.mdr_listener.clear_answers_with(MessageType.chunk, msg.header.FileId(), msg.header.ChunkNo());
		
		mdr_socket.close();
		if(DEBUG) System.out.println("ChunkRestoreSend(end)");
	}
	
}
