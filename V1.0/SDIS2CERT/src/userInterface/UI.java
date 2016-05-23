package userInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.JFrame;

import Utilities.MessageStamp;
import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import funtionalities.PeerMetadata;
import protocols.BackupFile;
import protocols.PEER_BACKUP_METADATA;
import protocols.REQUESTDEL;
import protocols.PEER_RESTORE_METADATA;
import protocols.RestoreFile;

public class UI {

	public static RefValue<String> backup(String filePath, int replicationDegree){
		RefValue<String> backup_answer = new RefValue<String>();
		BackupFile bf = new BackupFile(ProgramDefinitions.db, filePath, replicationDegree, backup_answer);
		bf.doBackup();
		ProgramDefinitions.db.save();
		return backup_answer;
	}

	public static void quit(JFrame mainFrame){
		mainFrame.dispose();
	}



	public static void showMessageStamps(String peerId){
		if(PeerMetadata.message_stamps.containsKey(peerId)){
			System.out.println("\n\n");
			
			List<MessageStamp> mStamps = PeerMetadata.message_stamps.get(peerId);
			
			for(MessageStamp ms : mStamps)
				System.out.println("-> " + peerId + "\t: " + ms.fileid + "\t: " + ms.msg.toString() + "\t: " + ms.timestamp);
		}
	}
	
	public static void showMessageStamps(){
		
	}







	static BufferedReader br;

	public static void UI(){

		br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Enter String");

		int input=0;
		while(input!=6){
			System.out.println();
			System.out.println("--------------");
			System.out.println("1-Backup File");
			System.out.println("2-Recover File");
			System.out.println("3-Delete File");
			System.out.println("4-Backup Client Data");
			System.out.println("5-Recover Client Data");
			System.out.println("6-Quit");

			while(true){//get user input
				try{
					input = Integer.parseInt(br.readLine());
					break;
				} catch(Exception e){System.out.println("Digit a valid answer please");}
			}

			if(run_choice(input)) break;			
		}
	}

	private static boolean run_choice(int choice)
	{
		RefValue<String> answers;
		RefValue<Boolean> completed;
		BackupFile bf;
		RestoreFile rf;
		PEER_BACKUP_METADATA pbm;
		PEER_RESTORE_METADATA prd;

		switch (choice) {
		case 1: 
			RefValue<String> backup_answer = new RefValue<String>();
			bf = new BackupFile(ProgramDefinitions.db, "test.jpg",1,backup_answer);
			bf.doBackup();
			System.out.println(backup_answer.value);
			ProgramDefinitions.db.save();
			break;
		case 2:
			RefValue<String> restore_answer = new RefValue<String>();
			rf = new RestoreFile(ProgramDefinitions.db, "test.jpg",restore_answer);
			rf.doRestore();
			System.out.println(restore_answer.value);
			break;
		case 3:
			HashSet<String> set = ProgramDefinitions.db.getDatabase().getFileMetadata("test.jpg").getPeersWithChunks();
			List<String> PeerIDs = new ArrayList<String>(set);
			REQUESTDEL requestDelete = new REQUESTDEL(ProgramDefinitions.CONTROL_PORT, ProgramDefinitions.CONTROL_ADDRESS, "d407ee6406a1216f2366674a1a9ff71361d5bef4721f8eb8b51f95e319dd56", PeerIDs,null);
			requestDelete.start();
			try {
				requestDelete.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			break;
		case 4: 
			completed = new RefValue<Boolean>();
			pbm = new PEER_BACKUP_METADATA(
					ProgramDefinitions.CONTROL_PORT,
					ProgramDefinitions.CONTROL_ADDRESS,
					ProgramDefinitions.mydata.peerID,
					ProgramDefinitions.mydata.peerID,
					ProgramDefinitions.db,
					completed
					);
			pbm.run();
			try{
				pbm.join();
			} catch (Exception e){e.printStackTrace();}
			if(completed.value) System.out.println("Metadata Backup was successfull");
			else System.out.println("Metadata Backup failed");
			break;
		case 5: 
			completed = new RefValue<Boolean>();
			prd = new PEER_RESTORE_METADATA(
					ProgramDefinitions.CONTROL_PORT,
					ProgramDefinitions.CONTROL_ADDRESS,
					ProgramDefinitions.mydata.peerID,
					ProgramDefinitions.mydata.peerID,
					completed,
					ProgramDefinitions.db				
					);
			prd.run();
			try{
				prd.join();
			} catch (Exception e){e.printStackTrace();}
			if(completed.value) {
				System.out.println("Metadata Backup was successfull");
				ProgramDefinitions.db.save();
			}
			else System.out.println("Metadata Backup failed");

			break;
		case 6: return true;
		case 7: //if(DEBUG) PeerMetadata.printMaxTimeStamps();
			break;
		default:
			break;
		}
		System.out.println("Press Enter to continue...");
		try {
			br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}