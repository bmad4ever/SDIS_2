package protocols;

import Utilities.RefValue;
import communication.TCP_Client;

public class DELETE_protocol extends TCP_Client{

	public DELETE_protocol(int p, String a,RefValue<Boolean> accept) {
		super(p,a,accept);
	}

	@Override
	public void run(){
		super.baserun();
		if(failed_init)
			return;
		
			
			
			
	}
	
}
