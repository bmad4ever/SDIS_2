package userInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import protocols.BackupFile;
import protocols.PEER_BACKUP_METADATA;
import protocols.REQUESTDEL;
import protocols.PEER_RESTORE_METADATA;
import protocols.REVIVE;
import protocols.RestoreFile;

public class UI {

	//dev note: most classes are not running on a new thread (apparently no need to do so)
	//using run instead of starting a thread. the extends thread only causes overhead
	//please remove later if continued ...
	
	public static RefValue<String> backup(String filePath, int replicationDegree){
		RefValue<String> backup_answer = new RefValue<String>();
		BackupFile bf = new BackupFile(ProgramDefinitions.db, filePath, replicationDegree, backup_answer);
		bf.doBackup();
		ProgramDefinitions.db.save();
		return backup_answer;
	}

	public static RefValue<String> recover(String fileName){
		RefValue<String> restore_answer = new RefValue<String>();
		RestoreFile rf = new RestoreFile(ProgramDefinitions.db, fileName, restore_answer);
		rf.doRestore();
		return restore_answer;
	}

	public static void delete(String fileName, String fileId){
		HashSet<String> set = ProgramDefinitions.db.getDatabase().getFileMetadata(fileName).getPeersWithChunks();
		List<String> PeerIDs = new ArrayList<String>(set);
		REQUESTDEL requestDelete = new REQUESTDEL(ProgramDefinitions.CONTROL_PORT, ProgramDefinitions.CONTROL_ADDRESS,
				fileId,fileName, PeerIDs, null);
		requestDelete.run();
		/*try {
			requestDelete.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}*/
		return;
	}

	public static RefValue<String> backupClientData(){
		RefValue<String> answer = new RefValue<>();
		RefValue<Boolean> completed = new RefValue<Boolean>();
		PEER_BACKUP_METADATA pbm = new PEER_BACKUP_METADATA(
				ProgramDefinitions.CONTROL_PORT,
				ProgramDefinitions.CONTROL_ADDRESS,
				ProgramDefinitions.mydata.peerID,
				ProgramDefinitions.mydata.peerID,
				ProgramDefinitions.db,
				completed
				);
		pbm.run();
		/*try{
			pbm.join();
		} catch (Exception e){e.printStackTrace();}*/
		if(completed.value) answer.value = "Metadata Backup was successful";
		else answer.value = "Metadata Backup failed";
		return answer;
	}
	
	public static RefValue<String> recoverClientData(){
		RefValue<String> answer = new RefValue<>();		
		RefValue<Boolean> completed = new RefValue<Boolean>();
		PEER_RESTORE_METADATA prd = new PEER_RESTORE_METADATA(
				ProgramDefinitions.CONTROL_PORT,
				ProgramDefinitions.CONTROL_ADDRESS,
				ProgramDefinitions.mydata.peerID,
				ProgramDefinitions.mydata.peerID,
				completed,
				ProgramDefinitions.db				
				);
		prd.run();
		/*try{
			prd.join();
		} catch (Exception e){e.printStackTrace();}*/
		if(completed.value) {
			answer.value = "Metadata Backup was successfull";
			ProgramDefinitions.db.save();
		}
		else answer.value = "Metadata Backup failed";
		return answer;
	}
	
	public static void revive(){
		RefValue<Boolean> ref = new RefValue<Boolean>();
		REVIVE revive = new REVIVE(ProgramDefinitions.CONTROL_PORT, ProgramDefinitions.CONTROL_ADDRESS, ref);
		revive.run();
	}

	public static void quit(GUI mainFrame){
		mainFrame.dispose();
	}
}