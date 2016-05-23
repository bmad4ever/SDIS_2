package funtionalities;

import Utilities.ProgramDefinitions;

public class PeriodicUpdates implements Runnable{

	static final boolean DEBUG=false;
	
	static boolean stop=false;
	public static void STOP(){stop=true;}
	/**time between nonvolatile metadata updates in milliseconds*/
	static final int wait_time = 5000;
	@Override
	public void run() {
		while(!stop)
		{
			try {
				Thread.sleep(wait_time);
				if(DEBUG) System.out.println("Saving Data");
				PeerMetadata.save_timestamps();
				if (ProgramDefinitions.is_control) PeerMetadata.update_active_peers();
				PeerMetadata.save_peers();
				if (!ProgramDefinitions.is_control) ProgramDefinitions.db.save();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	
}
