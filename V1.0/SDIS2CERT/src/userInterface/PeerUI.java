package userInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import Utilities.ProgramDefinitions;
import Utilities.RefValue;
import protocols.BackupFile;
import protocols.REQUESTDEL;
import protocols.RestoreFile;

public class PeerUI {

	private static final boolean DEBUG = true;
	
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
		if(DEBUG) System.out.println("Processing user input " + choice);
		switch (choice) {
		case 1: 
			RefValue<String> backup_answer = new RefValue<String>();
			BackupFile bf = new BackupFile(ProgramDefinitions.db, "test.jpg",1,backup_answer);
			bf.doBackup();
			System.out.println(backup_answer.value);
			break;
		case 2:
			RefValue<String> restore_answer = new RefValue<String>();
			RestoreFile rf = new RestoreFile(ProgramDefinitions.db, "test.jpg",restore_answer);
			rf.doRestore();
			System.out.println(restore_answer.value);
			break;
		case 3:
			List<String> PeerIDs = new ArrayList<String>();
			PeerIDs.add("Peer1");
			PeerIDs.add("Peer2");
			PeerIDs.add("Peer3");
			REQUESTDEL deleteclient = new REQUESTDEL(ProgramDefinitions.CONTROL_PORT, ProgramDefinitions.CONTROL_ADDRESS, "FileID", PeerIDs,null);
			deleteclient.start();
			try {
				deleteclient.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				}
			break;
		case 5: return true ;
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
