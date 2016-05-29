package protocols;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import FileSystem.Chunk;
import FileSystem.DatabaseManager;
import FileSystem.PeerFile;
import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import funtionalities.PeerMetadata;
import funtionalities.SymmetricKey;

public class BackupFile{

	private final static boolean DEBUG = true;
	
	public static int _CHUNK_SIZE = 64000;
	private static int _MAX_NUMBER_OF_RETRIES = 2;
	private static int _INITIAL_REPLY_WAIT_TIME = 1; // seconds
	private DatabaseManager db;
	private String filePath;
	private int replicationDegree;
	private PeerFile peerFile;
	RefValue<String> answer;

	public BackupFile(/*int port, String address*/
			DatabaseManager db,
			String filePath, int replicationDegree,RefValue<String> answer) {
		//super(port, address,accept);

		this.db = db;

		this.filePath = filePath;
		this.replicationDegree = replicationDegree;

		this.answer = answer;
		this.answer.value = "";
	}

	private boolean sendPutChunck(final String fileId, final int chunkNum, byte[] chunkData) {
		if(chunkData == null) 
		{
			return false;
		}

		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;
		boolean backupComplete = false;

		//Chunk db_chunk_info = db.getDatabase().addStoredChunkFile(fileId, chunkNum, replicationDegree);
		//when saving own chunks metadata there is no need to save the fileid in all chunks, 
		//it will be available on hashmap myorgiginalfiles
		Chunk db_chunk_info = peerFile.addChunk(null, chunkNum, replicationDegree);
		
		/*ExecutorService receiveExecutor = Executors.newFixedThreadPool(1);
		Runnable receiveRunnable = new Runnable() {
			@Override
			public void run() {
				while(!db.getDatabase().getStoredChunkData(fileId, chunkNum).isAboveReplicationDegree()){

				}
			}
		};*/

		while( numOfTries <= _MAX_NUMBER_OF_RETRIES ){

			//sendMessage(packetToSend);
			/*List<PeerData> peers = PeerMetadata.getMetadata2send2peer();
			long seed = System.nanoTime();
			Collections.shuffle(peers, new Random(seed));*/
			List<PeerData> peers = PeerMetadata.getPeersListRandomlySorted();

			//only use list if mutithreading
			//List<RefValue<Boolean>> completed = new ArrayList<RefValue<Boolean>>(replicationDegree);
			RefValue<Boolean> completed = new RefValue<Boolean>();

			//List<Thread> putchunks = new Lis
			//ExecutorService executor = Executors.newFixedThreadPool(5); //could try to trun multiple at the same time later

			for(int i=0; i<peers.size() && !backupComplete ;++i)//PeerData peer : peers)
			{	
				PeerData temp_peerdata = peers.get(i);
				//do not count own data
				if (temp_peerdata.peerID.equals(ProgramDefinitions.mydata.peerID)) continue;
				if(DEBUG) System.out.println("Sending putchunk<" +fileId +","+chunkNum +"> to " + 
				temp_peerdata.peerID+ "______"
				+ProgramDefinitions.mydata.peerID
				+temp_peerdata.peerID.equals(ProgramDefinitions.mydata.peerID)
				);
				
				//completed.add(new RefValue<Boolean>());
				//executor.execute(
				Thread t_putchunk = new PUTCHUNK(temp_peerdata.addr
						,ProgramDefinitions.mydata.peerID
						,fileId
						,chunkNum
						,replicationDegree
						,chunkData
						,completed);//completed.get(i));
				//);
				t_putchunk.start();
				try{
					t_putchunk.join();
				}catch(Exception e){e.printStackTrace();}

				if(completed.value) db_chunk_info.addPeerSaved(temp_peerdata.peerID);
				if(db_chunk_info.isAboveReplicationDegree())
					backupComplete = true;
			}
			if(backupComplete) break;
			
			try {
				Thread.sleep(waitInterval);//receiveExecutor.submit(receiveRunnable).get(waitInterval, TimeUnit.SECONDS);
				/*} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
				e.getCause();
			} catch (TimeoutException e) {
				numOfTries++;

				 */
				} catch(Exception e){e.printStackTrace();}
			
			waitInterval = waitInterval * 2;
			numOfTries++;
			
			//if(db.getDatabase().getStoredChunkData(fileId, chunkNum).isAboveReplicationDegree())
		}

		//receiveExecutor.shutdown();

		return backupComplete;
	}

	public boolean doBackup(){

		File fileTemp = new File(filePath);
		if(! (fileTemp.exists() && !fileTemp.isDirectory())) 
		{
			answer.value = "No such file found";
			return false;
		}
		
		if(PeerMetadata.getPeerDataLength()<replicationDegree)
		{
			answer.value = "Not enough connected peers to ensure given replication degree";
			return false;
		}
		
		String fileName = fileTemp.getName();
		String modDate = Long.toString(fileTemp.lastModified());
		
		ArrayList<byte[]> data = splitFileAndEncryptData(filePath);
		if(data == null) {
			answer.value = "Failed to divide in chunk or to encrypt";
			return false;
		}

		peerFile = db.getDatabase().addOriginalFile(fileName,modDate,replicationDegree);
		if(peerFile==null) peerFile = db.getDatabase().getFileMetadata(fileName);
		String fileId = db.getDatabase().getFileId(fileName);

		for(int i = 0; i < data.size(); i++){
			if(!sendPutChunck(fileId, i, data.get(i))) 
			{
				this.answer.value = "Chunk " + i +" failed to backup";
				return false;
			}
		}
		this.answer.value = "Backup completed";
		return true;
	}

	public ArrayList<byte[]> splitFileAndEncryptData(String fileDir){
		ArrayList<byte[]> result = new ArrayList<>();

		File fileToSplit = new File(fileDir);
		if(!fileToSplit.exists() || !fileToSplit.isFile()) return null;

		try {

			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSplit));

			byte[] buffer = new byte[_CHUNK_SIZE];
			long numberOfChuncks = fileToSplit.length() / _CHUNK_SIZE;
			for(int i = 0; i < numberOfChuncks; i++){
				bis.read(buffer);				
				result.add(SymmetricKey.encryptData(SymmetricKey.key, buffer));
				buffer = new byte[_CHUNK_SIZE];
			}

			int bytesRead = bis.read(buffer);
			//if(bytesRead == -1) bytesRead = 0;
			if(bytesRead>0){
			byte[] smallBuffer = new byte[bytesRead];
			System.arraycopy(buffer, 0, smallBuffer, 0, bytesRead);
			result.add(SymmetricKey.encryptData(SymmetricKey.key, smallBuffer));
			}

			bis.close();

		} catch (IOException e) {
			e.printStackTrace();
			e.getCause();
		}

		return result;
	}
}