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
	private FileOutputStream fos;
	private ObjectOutputStream oos;
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
			save();
		}
	}	
	
	public void save(){
		try {
			fos = new FileOutputStream(filename);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(database);
		} catch (IOException e) {
			e.printStackTrace();
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