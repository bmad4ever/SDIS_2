import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChunkBackupSpamming extends Thread{

	private static boolean DEBUG = false;
	
	volatile protected boolean stop = false;
	public void STOP() { stop=true;}
	
	static final int min_delay=10000;
	static final int max_delay=40000;
	
	static float elitist_bias = 0.5f;//should not be lower than 50% and not higher than 1, will tilt the result towards the end indexes
	static float base_space = 1000000000.0f;
	static int  base_space_descret = (int) base_space;
	
	public void run()
	{
		while(!stop)
		{
			//if (DEBUG) System.out.println("SPAMMER: WAITING");
			//sleep some interval to avoid 2 much spamm the network
			CommunicationThread.WaitRandomDelay(min_delay,max_delay);
			
			//start preparing the spamm - - - - - - - - - - - - - - - - - - - -
			//if (DEBUG) System.out.println("SPAMMER: PREPARING SPAM");
			
			//get owned chunk ordered from   ( rep deg -num copies )
			RefInteger numfilesref = new RefInteger(null);
			Map<Integer,Map<String,List<String>>> allchunks = ProgramData.chunkslog.find_safer2_remove(true,numfilesref);

			if(allchunks==null ) continue;
			if(allchunks.size()==0) continue;
			
			int numfiles;
			if(numfilesref==null || numfilesref.getValue()==null) {System.out.println("SPAMMING > SOME BUG!");  continue;} 
			numfiles = numfilesref.getValue();
			//float numFilesF = (float)numfiles;
			
			//if(DEBUG) System.out.println("SPAMMER: numF="+numfiles);
			
			//prioritize higher indexes but choose half randomly
			//a chunk may not achieve desired rep degree due to the number of peers
			//randomizing avoids this kind of situations

			//sum(1,n) i <==> (n)(1+n)/(2) also = to sum(1,n) i/n <==> (1+n)/(2) and so on...
			//but with less num error while summing
			float possibleCasesSpace =  ((float)numfiles)*(1.0f+ (float)numfiles) / 2.0f ;
			float index_chooser = (float) (new Random()).nextInt(base_space_descret+1);
			index_chooser*=(1*(.5f+elitist_bias));
			index_chooser/=base_space;
			float value = 0;
			for(int i = 1; i<=numfiles; ++i)
			{
				value += ((float) i);
					/*if (DEBUG) System.out.println("SPAMMER:"
							+ index_chooser + " | " + (value/onePlusNDivNsquared)
							);*/
					
				if (index_chooser<=value/possibleCasesSpace)  
				{
					int index=0;
					boolean found = false;
					for(Integer ci:allchunks.keySet())
					{
						Map<String,List<String>> files = allchunks.get(ci);
						for(String fileid:files.keySet())
						{
							List<String> chunks = files.get(fileid); 
							for(String chunk:chunks)
							{
								if(index==(i-1)) {
									//file chosen now spam it
									if (DEBUG) System.out.println("SPAMMER ("+(i-1)+"): " + fileid + "." + chunk);
									found=true;
									
									try {
										(new ChunkBackupSender(
												fileid,chunk
												,ProgramData.chunkslog.get_ChunkRepDegree(fileid, chunk)
												,ProgramData.getFileChunk(fileid, chunk)
												,true)
												).start();
									} catch (IOException e) {
										e.printStackTrace();
									}
									
									break;
								}
								index++;
							}
							if(found) break;
						}
						if(found) break;
					}
					break;
				}
			}
			
		}//while end...
		
	}
	
}
