package protocols;

import communication.MessagePacket;
import communication.TCP_Client;

public class PUTCHUNK extends TCP_Client{

	public PUTCHUNK(int p, String a) {
		super(p,a);
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;
			MessagePacket n = new MessagePacket(null, null);
			sendMessage(n);
	}
	
}
