package funtionalities;

public class PeerRenewService implements Runnable{

	static final private boolean DEBUG=true;
	
	static boolean stop=false;
	void STOP(){stop=true;}
	static final int wait_time = 5000;
	
	@Override
	public void run() {
				while(!stop)
				{
					try {
						Thread.sleep(wait_time);
						if(DEBUG) System.out.println("Renew Service: Sending hello...");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		
	}
	
}
