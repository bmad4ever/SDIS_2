package FileSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/* EXAMPLE: 
   DatabaseManager dbm = new DatabaseManager("teste.ser");
	dbm.getDatabase().addOriginalFile("image.png"); creates and stores the original name and its digested id
	dbm.getDatabase().addStoredChunkFile("fs8464ga8wrgg8a6ga8wa4g86sa46d6a48gf68awga", 3); stores a chunk id with its minimal replication value
	dbm.save();
 */
public class DatabaseManager {
	private FileInputStream fis;
	private ObjectInputStream ois;
	private Database database;
	private String filename;
	 
	public DatabaseManager(String name){
		filename = name;
		File f = new File(filename);
		if(f.exists()){
			load();
		}else{
			database = new Database();
		}
	}	
	
	public boolean save(){

		//serialize data to a new file (keeps old data so far)
		try (
				FileOutputStream file = new FileOutputStream(filename+"_new");
				ObjectOutputStream output = new ObjectOutputStream(file);
				){
			try {
				output.writeObject(database);
			} catch (Exception e) {e.printStackTrace();}
		}  
		catch(IOException ex){
			ex.printStackTrace();
			return false;
		}

		//if saved ok, then replace old data with new data
		try {
			File peerFile = new File(filename+"_new");
			File peerFile2 = new File(filename);
			peerFile2.delete();
			// Rename file
			boolean success = peerFile.renameTo(peerFile2);
			return success;
		}  		catch(Exception ex){
			return false;
		}

	}
	
	private void load(){
		try {
			fis = new FileInputStream(filename);
			ois = new ObjectInputStream(fis);
			database = (Database) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Database getDatabase(){
		return database;
	}
}