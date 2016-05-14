package protocols;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import FileSystem.Chunk;
import FileSystem.DatabaseManager;
import Utilities.PeerData;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;
import funtionalities.Metadata;

public class BackupFile{

	public static int _CHUNK_SIZE = 64000;

	private static int _MAX_NUMBER_OF_RETRIES = 2;
	private static int _INITIAL_REPLY_WAIT_TIME = 1; // seconds

	private DatabaseManager db;

	private String filePath;
	private int replicationDegree;

	public BackupFile(/*int port, String address*/
			DatabaseManager db,
			String filePath, int replicationDegree,RefValue<Boolean> accept) {
		//super(port, address,accept);

		this.db = db;

		this.filePath = filePath;
		this.replicationDegree = replicationDegree;
	}

	private boolean sendPutChunck(final String fileId, final int chunkNum, byte[] chunkData) {
		if(chunkData == null) return false;

		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;
		boolean backupComplete = false;

		Chunk db_chunk_info = db.getDatabase().addStoredChunkFile(fileId, chunkNum, replicationDegree);

		/*ExecutorService receiveExecutor = Executors.newFixedThreadPool(1);
		Runnable receiveRunnable = new Runnable() {
			@Override
			public void run() {
				while(!db.getDatabase().getStoredChunkData(fileId, chunkNum).isAboveReplicationDegree()){

				}
			}
		};*/

		MessageHeader headerToSend = new MessageHeader(MessageHeader.MessageType.putchunk, ProgramDefinitions.mydata.peerID);
		MessagePacket packetToSend = new MessagePacket(headerToSend, chunkData);

		while(( numOfTries <= _MAX_NUMBER_OF_RETRIES ) && !backupComplete){

			//sendMessage(packetToSend);
			List<PeerData> peers = Metadata.getMetadata2send2peer();
			long seed = System.nanoTime();
			Collections.shuffle(peers, new Random(seed));
			
			//only use list if mutithreading
			//List<RefValue<Boolean>> completed = new ArrayList<RefValue<Boolean>>(replicationDegree);
			RefValue<Boolean> completed = new RefValue<Boolean>();
			
			//List<Thread> putchunks = new Lis
			//ExecutorService executor = Executors.newFixedThreadPool(5); //could try to trun multiple at the same time later
			
			for(int i=0; i<peers.size();++i)//PeerData peer : peers)
			{
				PeerData temp_peerdata = peers.get(i);
				//do not count own data
				if (temp_peerdata.peerID==ProgramDefinitions.mydata.peerID) continue;
				
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
				try{
				t_putchunk.join();
				}catch(Exception e){e.printStackTrace();}
				
				if(completed.value) db_chunk_info.addPeerSaved(temp_peerdata.peerID);
			}
				
			try {
				Thread.sleep(waitInterval);//receiveExecutor.submit(receiveRunnable).get(waitInterval, TimeUnit.SECONDS);
			/*} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
				e.getCause();
			} catch (TimeoutException e) {
				numOfTries++;
				
			*/} catch(Exception e){e.printStackTrace();}
			waitInterval = waitInterval * 2;
			
			//if(db.getDatabase().getStoredChunkData(fileId, chunkNum).isAboveReplicationDegree())
			if(db_chunk_info.isAboveReplicationDegree())
				backupComplete = true;
		}

		//receiveExecutor.shutdown();
		
		return backupComplete;
	}

	public boolean doBackup(){
		ArrayList<byte[]> data = splitFile(filePath);
		if(data == null) return false;

		File fileTemp = new File(filePath);
		String fileName = fileTemp.getName();

		db.getDatabase().addOriginalFile(fileName);
		String fileId = db.getDatabase().getFileId(fileName);

		for(int i = 0; i < data.size(); i++){
			if(!sendPutChunck(fileId, i, data.get(i))) return false;
		}

		return true;
	}

	public ArrayList<byte[]> splitFile(String fileDir){
		ArrayList<byte[]> result = new ArrayList<>();

		File fileToSplit = new File(fileDir);
		if(!fileToSplit.exists() || !fileToSplit.isFile()) return null;

		try {

			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSplit));

			byte[] buffer = new byte[_CHUNK_SIZE];
			long numberOfChuncks = fileToSplit.length() / _CHUNK_SIZE;
			for(int i = 0; i < numberOfChuncks; i++){
				bis.read(buffer);
				result.add(buffer);
				buffer = new byte[_CHUNK_SIZE];
			}

			int bytesRead = bis.read(buffer);
			if(bytesRead == -1) bytesRead = 0;
			byte[] smallBuffer = new byte[bytesRead];
			System.arraycopy(buffer, 0, smallBuffer, 0, bytesRead);
			result.add(smallBuffer);

			bis.close();

		} catch (IOException e) {
			e.printStackTrace();
			e.getCause();
		}

		return result;
	}
}