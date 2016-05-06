package protocols;

import communication.TCP_Client;

public class DELETE_protocol extends TCP_Client{

	public DELETE_protocol(int p, String a) {
		super(p,a);
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;
		
			
			
			
	}
	
}
