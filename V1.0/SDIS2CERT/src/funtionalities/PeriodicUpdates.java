package funtionalities;

import java.util.Random;

import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import protocols.WHO;

public class PeriodicUpdates implements Runnable{

	static final boolean DEBUG=false;
	
	static boolean stop=false;
	public static void STOP(){stop=true;}
	/**time between nonvolatile metadata updates in milliseconds*/
	static final int wait_time_control = 5000;
	static final int wait_time = 4000;
	static final int max_rand_delay = 2000;
	@Override
	public void run() {
		while(!stop)
		{
			try {
				
				if (!ProgramDefinitions.is_control) Thread.sleep(wait_time+(new Random()).nextInt(max_rand_delay));
				else Thread.sleep(wait_time_control); 
					
				if(DEBUG) System.out.println("Saving Data");
				PeerMetadata.save_timestamps();
				if (ProgramDefinitions.is_control) PeerMetadata.update_active_peers();
				PeerMetadata.save_peers();
				if (!ProgramDefinitions.is_control)
					{
						ProgramDefinitions.db.save();
						RefValue<Boolean> completed = new RefValue<Boolean>();
						WHO who = new WHO(ProgramDefinitions.CONTROL_PORT,ProgramDefinitions.CONTROL_ADDRESS, completed);
						who.start();
					}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	
}
