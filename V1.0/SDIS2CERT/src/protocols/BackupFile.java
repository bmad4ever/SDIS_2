package protocols;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import FileSystem.DatabaseManager;
import Utilities.ProgramDefinitions;
import communication.TCP_Client;
import communication.messages.MessageHeader;
import communication.messages.MessagePacket;

public class BackupFile extends TCP_Client{

	public static int _CHUNK_SIZE = 64000;

	private static int _MAX_NUMBER_OF_RETRIES = 5;
	private static int _INITIAL_REPLY_WAIT_TIME = 1; // seconds

	private DatabaseManager db;

	private String filePath;
	private int replicationDegree;

	public BackupFile(int port, String address, DatabaseManager db, String filePath, int replicationDegree) {
		super(port, address);

		this.db = db;

		this.filePath = filePath;
		this.replicationDegree = replicationDegree;
	}

	private boolean sendPutChunck(final String fileId, final int chunkNum, byte[] chunkData) {
		if(chunkData == null) return false;

		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;
		boolean backupComplete = false;

		db.getDatabase().addStoredChunkFile(fileId, chunkNum, replicationDegree);

		ExecutorService receiveExecutor = Executors.newFixedThreadPool(1);
		Runnable receiveRunnable = new Runnable() {
			@Override
			public void run() {
				while(!db.getDatabase().getStoredChunkData(fileId, chunkNum).isAboveReplicationDegree()){

				}
			}
		};

		MessageHeader headerToSend = new MessageHeader(MessageHeader.MessageType.putchunk, ProgramDefinitions.mydata.peerID);
		MessagePacket packetToSend = new MessagePacket(headerToSend, chunkData);

		while(( numOfTries <= _MAX_NUMBER_OF_RETRIES ) && !backupComplete){

			sendMessage(packetToSend);

			try {
				receiveExecutor.submit(receiveRunnable).get(waitInterval, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
				e.getCause();
			} catch (TimeoutException e) {
				numOfTries++;
				waitInterval = waitInterval * 2;
			}

			if(db.getDatabase().getStoredChunkData(fileId, chunkNum).isAboveReplicationDegree())
				backupComplete = true;
		}

		receiveExecutor.shutdown();
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