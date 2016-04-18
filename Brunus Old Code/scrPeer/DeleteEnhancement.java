import java.util.Map;

public class DeleteEnhancement implements Runnable {

	static final boolean DEBUG = false;
	
	static final int MAX_NUMBER_OFGETCHUNK_TRIES_PERCHUNK = 2;
	
	public DeleteEnhancement() 
	{

	}

	void check_deletes_for(Map<String,String> filechunks){
		
	
		for(String file:filechunks.keySet())
		{
			try{
				
			for(int i = 0; i<MAX_NUMBER_OFGETCHUNK_TRIES_PERCHUNK; ++i){
			RefBoolean received_confirmation = new RefBoolean(null);
			ChunkRestoreGet crg = new ChunkRestoreGet(file,Integer.parseInt( filechunks.get(file) ),false);
			crg.passQuestions(received_confirmation);
			crg.start();
			crg.join();
			if(received_confirmation.getValue()) return;
			}

			//no confirmation? -> assume file was deleted
			//if(!received_confirmation.getValue())
			//{
				new FileDeletion(file).start();
			//}
			
			} catch (Exception e) {}
			
		}
		
	}
	
	public void run()
	{
		if (DEBUG) System.out.println("DeleteEnhancement started");
		/*ONLY WORKS IF ALL PEERS ONLINE AND ABLE 2 COMMUNICATE*/
		/*ALSO ASSUMES SAVES ESTIMATE OF NUM OF COPIES WAS CORRECT*/
		
		/*process case1
		a chunk (or more) is missing in between our peer stored chunks
		if no one can confirm it then it was either removed	or, if not, lost forever
		in either case is safe to delete the file chunk
		*/
		
		if (DEBUG) System.out.println("DeleteEnhancement doing 1st step");
		check_deletes_for(ProgramData.chunkslog.find_1missingChunk_forAllFiles());
		
		/*process case2
		if a chunk as rep degree greater than one
		(assuming rep degree was not overwritten when offline)
		then if no one can confirm it was deleted
		riskier than previous case
		*/
		
		if (DEBUG) System.out.println("DeleteEnhancement doing 2nd step");
		check_deletes_for(ProgramData.chunkslog.find_1Chunk_withRepDegHigherThan1_forAllFiles());
		
		if (DEBUG) System.out.println("DeleteEnhancement ended");
	}
	
}
