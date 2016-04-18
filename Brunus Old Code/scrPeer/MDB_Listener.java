import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class MDB_Listener extends MulticastListener{

	private static final boolean DEBUG = false;

	public MDB_Listener(String address, int port) throws IOException {
		super(address,port);
	}



	public void run()
	{
		while(!stop)
		{
			try {
				DatagramPacket packet = getDatagramPacket(socket);
				byte[] received = new byte[packet.getLength()];
				byte[] temp = packet.getData();
				received = Arrays.copyOfRange(temp, 0, packet.getLength());
				Message msg = getMSG(received,true);

				if(DEBUG) System.out.println("-----MDB rec: bogysize="+msg.body.length);
				if(!valid_msghd(msg.header)) continue;
				if(DEBUG) System.out.println("MDB: "+msg.toString());
				
				if(msg.header.MessageType()==CommunicationThread.MessageType.putchunk)
				{
					Message proof = new Message(msg.header,null);
					add2_msg_list(proof);
					//to be used by the reclaim protocol,
					//proof should be cleaned after chunknackupreceiver or processRemove
					//chunknackupreceiver will close if chunk is owned without cleaning
					//so the verification by processRemove should succeed
					
					if(version==Version.one){new ChunkBackupReceiver(msg).start();}
					else if(version==Version.two)
					{new ChunkBackupReceiverEnhanced(msg).start();}
				}
				else {
					if(DEBUG) {
						System.out.println("MDB: Unexpected msg received");
						System.out.println("MDB: "+msg.toString());
					}
				}
			} catch(SocketTimeoutException se){}
			catch (Exception e) {				
				e.printStackTrace();
			}

			if(DEBUG) System.out.println("endingloop");
		}//--- --- LOOP END
		
		try {
			exit_multicast();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
