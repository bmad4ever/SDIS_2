import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TestAppServer extends Thread {
	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out;
	BufferedReader in;
	volatile protected boolean stop = false;
	public void STOP() { 
		stop=true; 
		try {
			this.serverSocket.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		}
	String message;
	String returnMessage;

	public TestAppServer(int serverSocket) {
		try {
			this.serverSocket = new ServerSocket(serverSocket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void restartConection(ServerSocket serverSocket2) {
		message = "";
		returnMessage = "";
		try {
			this.clientSocket = this.serverSocket.accept();			
			this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);                   
			this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

	public static boolean testPath(String path) {
		File f = new File(path);
		if(f.exists() && !f.isDirectory()) { 
			return true;
		}
		return false;
	}

	public String processMessage(String msg) {
		String rtn_msg = "";
		String file_path = "";
		int replication_deg = -1, space_2_reclaim = -1;
		String[] arr = msg.split(" ");

		switch (arr[0]) {
		case "BACKUP":
			if (!testPath(arr[1])) {
				rtn_msg = "RESPONSE: path not valid";
				break;
			} else file_path = arr[1];
			replication_deg = Integer.parseInt(arr[2]);
			rtn_msg = FileUtilities.backUpFile(file_path, replication_deg);
			
			break;
		case "RESTORE":
			rtn_msg = FileUtilities.restoreFile(arr[1]);
			break;
		case "DELETE":
			try {
				new SendDelete(arr[1]).start();
				ProgramData.myfileslog.delete_file_info(arr[1]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				rtn_msg = "RESPONSE: DELETE exception!";
				break;
			}
			rtn_msg = "RESPONSE: success";
			break;
		case "RECLAIM":
			space_2_reclaim = Integer.parseInt(arr[1]);
			if (FileUtilities.spaceReclaim(space_2_reclaim))
				rtn_msg = "RESPONSE: success " + "Total space: " + ProgramData.max_storage_space_allowed + " chunks(64KB) Total used: " + ProgramData.storage_space_used + " chunks(<=64KB each)";
			else
				rtn_msg = "RESPONSE: RECLAIM fail!";
			break;

		default:
			rtn_msg = "error_process_message";
			break;
		}
		return rtn_msg;
	}

	public void run() {

		while(!stop) {
			restartConection(this.serverSocket);
			try {
				message = in.readLine();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			if(!stop){
				returnMessage = processMessage(message);
				out.println(returnMessage);
			}
		}
		//this.serverSocket.close();
	}

	/*public static void main(String[] args) {
		TestAppServer test = new TestAppServer(12000);
		test.start();
	}*/


}