package protocols;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import FileSystem.DatabaseManager;
import Utilities.Misc;
import Utilities.ProgramDefinitions;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;

public class RestoreFile extends TCP_Client{
	
	public static int _CHUNK_SIZE = 64000;

	private static int _MAX_NUMBER_OF_RETRIES = 5;
	private static int _INITIAL_REPLY_WAIT_TIME = 1; // seconds

	private DatabaseManager db;

	private String fileName;

	public RestoreFile(int port, String address, DatabaseManager db, String fileName){
		super(port, address);

		this.db = db;

		this.fileName = fileName;
	}

	private boolean restoreChunk(final String fileId, final int chunkNum){

		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;
		
		MessageHeader headerToSend = new MessageHeader(MessageHeader.MessageType.getchunk, ProgramDefinitions.mydata.peerID);
		MessagePacket packetToSend = new MessagePacket(headerToSend, null);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				while(!db.getDatabase().getStoredChunkData(fileId, chunkNum).hasData()){
				}
				
				return true;
			}
		};

		boolean restoreComplete = false;
		while(( numOfTries <= _MAX_NUMBER_OF_RETRIES ) && !restoreComplete){

			sendMessage(packetToSend);

			try {
				restoreComplete = executor.submit(callable).get(waitInterval, TimeUnit.SECONDS).booleanValue();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
				e.getCause();
			} catch (TimeoutException e) {
				numOfTries++;
				waitInterval = waitInterval * 2;
			}
		}

		return restoreComplete;
	}

	public boolean doRestore(){

		String fileId = db.getDatabase().getFileId(fileName);
		if(fileId == null) return false;

		int chunkNum = 0;
		do{
			if(!restoreChunk(fileId, chunkNum)) return false;
			
			// write temp chunk file
			db.getDatabase().getStoredChunkData(fileId, chunkNum).writeChunkFile();
			
			chunkNum++;
		}while(db.getDatabase().getStoredChunkData(fileId, chunkNum).getData().length == _CHUNK_SIZE);

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