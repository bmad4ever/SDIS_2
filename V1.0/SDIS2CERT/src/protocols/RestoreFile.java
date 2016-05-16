package protocols;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import FileSystem.Chunk;
import FileSystem.DatabaseManager;
import Utilities.Misc;
import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Client;
import funtionalities.PeerMetadata;

public class RestoreFile extends TCP_Client{

	private final static boolean DEBUG = true;

	public static int _CHUNK_SIZE = 64000;

	private static int _MAX_NUMBER_OF_RETRIES = 2;
	private static int _INITIAL_REPLY_WAIT_TIME = 1; // seconds

	private DatabaseManager db;

	private String fileName;

	public RestoreFile(int port, String address, DatabaseManager db, String fileName,RefValue<Boolean> accept){
		super(port, address,accept);

		this.db = db;

		this.fileName = fileName;
	}

	private boolean restoreChunk(final String fileId, final int chunkNum,RefValue<byte[]> received_chunk ){

		Chunk chunk2request = db.getDatabase().getStoredChunkData(fileId,chunkNum);
		if(chunk2request==null) {
			if(DEBUG) System.out.println("restoreChunk("+fileId+","+chunkNum+")File Chunk not in DB");
			return false;
		}

		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;

		//MessageHeader headerToSend = new MessageHeader(MessageHeader.MessageType.getchunk, ProgramDefinitions.mydata.peerID);
		//MessagePacket packetToSend = new MessagePacket(headerToSend, null);

		/*ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				while(!db.getDatabase().getStoredChunkData(fileId, chunkNum).hasData()){
				}

				return true;
			}
		};*/

		RefValue<Boolean> completed = new RefValue<Boolean>();
		//RefValue<byte[]> received_chunk = new RefValue<byte[]>();

		boolean restoreComplete = false;
		while(( numOfTries <= _MAX_NUMBER_OF_RETRIES ) && !restoreComplete){

			List<String> peersIDs = chunk2request.getPeersSaved();
			for(int i=0; i<peersIDs.size() && !restoreComplete;++i)//PeerData peer : peers)
			{
				PeerData temp_peerdata = PeerMetadata.getPeerData(peersIDs.get(i));

				if(temp_peerdata==null) continue;
				//do not count own data
				if (temp_peerdata.peerID==ProgramDefinitions.mydata.peerID) continue;

				//completed.add(new RefValue<Boolean>());
				//executor.execute(
				Thread t_getchunk = new GETCHUNK(temp_peerdata.addr
						,ProgramDefinitions.mydata.peerID
						,fileId
						,chunkNum
						,completed
						,received_chunk);//completed.get(i));
				//);
				try{
					t_getchunk.join();
				}catch(Exception e){e.printStackTrace();}

				restoreComplete = completed.value;	
			}

			try {
				Thread.sleep(waitInterval);
				numOfTries++;
				waitInterval = waitInterval * 2;
				/*	restoreComplete = executor.submit(callable).get(waitInterval, TimeUnit.SECONDS).booleanValue();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
				e.getCause();
			} catch (TimeoutException e) {
				numOfTries++;
				waitInterval = waitInterval * 2;*/
			} catch(Exception e){e.printStackTrace();}
		}

		return restoreComplete;
	}

	public boolean doRestore(){

		String fileId = db.getDatabase().getFileId(fileName);
		if(fileId == null) return false;

		RefValue<byte[]> received_chunk=null;
		int chunkNum = 0;
		do{
			received_chunk = new RefValue<byte[]>();
			if(!restoreChunk(fileId, chunkNum,received_chunk)) return false;

			// write temp chunk file
			db.getDatabase().getStoredChunkData(fileId, chunkNum).writeChunkFile(received_chunk.value);

			chunkNum++;
		}while(
				db.getDatabase().getStoredChunkData(fileId, chunkNum)!=null
				//ou received_chunk!=null && received_chunk.value.length == _CHUNK_SIZE
				);

		if(!uniteFile()) return false;

		return true;
	}

	private boolean uniteFile(){

		String fileId = db.getDatabase().getFileId(fileName);
		if(fileId == null) return false;

		String filesDir = ProgramDefinitions.mydata.peerID + File.separator + fileId;
		String outputDir = ProgramDefinitions.mydata.peerID + File.separator + fileName;

		File[] files = new File(filesDir).listFiles();
		ArrayList<String> chunkNameHolder = new ArrayList<>();
		for(File file : files){
			String chunkName = file.getName();

			if(chunkName.length() >= fileId.length() &&
					chunkName.substring(0, fileId.length()).equals(fileId)){
				chunkNameHolder.add(chunkName);
			}
		}

		if(chunkNameHolder.size() == 0) return false;
		PriorityQueue<String> namesOrdered = new PriorityQueue<>(chunkNameHolder);

		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputDir));
			while(!namesOrdered.isEmpty()){
				File tempFile = new File(filesDir + File.separator + namesOrdered.remove());
				Files.copy(tempFile.toPath(), bos);
			}

			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
			e.getCause();
		}

		if(!Misc.deleteFolder(fileId)) return false;

		return true;
	}
}