package funtionalities;

import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import protocols.HELLO;

public class PeerRenewService implements Runnable{

	static final private boolean DEBUG=true;

	static boolean stop=false;
	void STOP(){stop=true;}
	static final int wait_time = 5000;
	
	
	
	@Override
	public void run() {
		RefValue<Boolean> accept = new RefValue<Boolean>();
		accept.value = false;
		while(!stop)
		{
			try {
				Thread.sleep(wait_time);
				if(DEBUG) System.out.println("Renew Service: Sending hello...");

				
				HELLO client = new HELLO(ProgramDefinitions.CONTROL_PORT, ProgramDefinitions.CONTROL_ADDRESS, accept);
		    	client.start();
		    	try {client.join();	} 
		    	catch (InterruptedException e1) {	e1.printStackTrace();}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
