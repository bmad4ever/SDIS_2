package FileSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/* EXAMPLE: 
   DatabaseManager dbm = new DatabaseManager("teste.ser");
	dbm.getDatabase().printFiles();
	dbm.getDatabase().addPeerFile(new PeerFile("kik", 3));
	dbm.getDatabase().addPeerFile(new PeerFile("leel", 2));
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
			load();
		}
	}
	
	
	public void save(){
		try {
			fos = new FileOutputStream(filename);
			oos = new ObjectOutputStream(fos);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			oos.writeObject(database);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void load(){
		try {
			fis = new FileInputStream(filename);
			ois = new ObjectInputStream(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
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
