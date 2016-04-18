import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProgramData {

	private static final boolean DEBUG = false;
	public static final boolean IS_WINDOWS = System.getProperty( "os.name" ).contains( "indow" );

	//this storage is measured in number of chunks
	//note
	//some chunks could be smaller than 64KB but having to check the precise space when freeing/storing a chunk
	//souldn't be so simple and would be harder to test i this phase

	static public int max_storage_space_allowed = 10; 
	static public boolean set_storage_max_space(int num) {
		if (num>=0 ) {max_storage_space_allowed = num; return true;}
		return false;
		}
	static public int storage_space_used = 0;
	public static synchronized void update_storage(int val2add){ 
		storage_space_used += val2add; 
		if (storage_space_used<0)storage_space_used=0;
	}

	//static private String log_file_name;
	//static public String getLogFileName(){return log_file_name;}
	static ChunksLog chunkslog;
	static MyFilesLog myfileslog;
	static int peer_id;			//should be a nice integer in the peer network
	//static int storage_size; 	//the max number of chuncks allowed

	private static  String MCaddress;
	public static String getMCadd(){return MCaddress;}
	private static  String MDBaddress;
	public static String getMDBadd(){return MDBaddress;}
	private static  String MDRaddress;
	public static String getMDRadd(){return MDRaddress;}
	private static  int MCport;
	public static int getMCport(){return MCport;}
	private static  int MDBport;
	public static int getMDBport(){return MDBport;}
	private static  int MDRport;
	public static int getMDRport(){return MDRport;}

	static MC_Listener  mc_listener;
	static MDB_Listener mdb_listener;
	static MDR_Listener mdr_listener;

	private static String chunk_folder_Path;
	public static String getChunkFolderPath(){return chunk_folder_Path;}
	private static String temp_folder_Path;
	public static String getTempFolderPath(){return temp_folder_Path;}
	private static String restore_Path;
	public static String getRestoreFolderPath(){return restore_Path;}
	private static String restored_Files;
	public static String getRestoredFiles(){return restored_Files;}
	
	private static boolean loaded_stuff=false;
	
	public static void setProgramData(int peerid,int maxstoragespace_allowed,String MCadd,String MDBadd,String MDRadd,int MCp,int MDBp,int MDRp)
	{
		peer_id=peerid;
		max_storage_space_allowed=maxstoragespace_allowed;
		chunkslog = new ChunksLog("chunkslog_"+peerid+".txt");
		myfileslog = new MyFilesLog("myfileslog_"+peerid+".txt");
		MCaddress=MCadd;
		MDBaddress=MDBadd;
		MDRaddress=MDRadd;
		MCport  = MCp;
		MDBport = MDBp;
		MDRport = MDRp;

		try{

			File dir_rec = new File("chunk_files_server_" + Integer.toString(peerid));
			dir_rec.mkdir();
			dir_rec = new File("tmp_chunks_" + Integer.toString(peerid));
			dir_rec.mkdir();
			dir_rec = new File("restore_chunks_" + Integer.toString(peerid));
			dir_rec.mkdir();
			dir_rec = new File("restored_Files_" + Integer.toString(peerid));
			dir_rec.mkdir();
			chunk_folder_Path = "chunk_files_server_" + Integer.toString(peerid);
			temp_folder_Path = "tmp_chunks_" + Integer.toString(peerid);		
			restore_Path = "restore_chunks_" + Integer.toString(peerid);
			restored_Files = "restored_Files_" + Integer.toString(peerid);
			
			File f = new File("chunkslog_"+peerid+".txt");
			File f2 = new File("myfileslog_"+peerid+".txt");
			if(!f2.exists() && !f2.isDirectory())
			{
			    f2.createNewFile();
			} 
			if(!f.exists() && !f.isDirectory())
			{
			    f.createNewFile();
			} else 
				{
					update_storage( chunkslog.get_available_storage_capacity() );
					loaded_stuff=true;
				}

			/*URL location = ProgramData.class.getProtectionDomain().getCodeSource().getLocation();
			chunk_folder_Path = location.getFile();
			if(IS_WINDOWS) chunk_folder_Path = chunk_folder_Path.substring(1);
	        if(DEBUG) System.out.println(chunk_folder_Path);

	        Path path_rec = Paths.get(chunk_folder_Path + "chunk_files_server_" + Integer.toString(peerid));
			Path path_snd = Paths.get(chunk_folder_Path+ "shared_files_server_" + Integer.toString(peerid));

			File dir_rec = path_rec.toFile();
			File dir_snd = path_snd.toFile();

			 if(DEBUG) System.out.println(path_rec.toString());*/
			//if(dir_rec.mkdir())System.out.println("created");//
			/*if (!dir_rec.exists()) {
			 if(DEBUG) System.out.println(path_rec.toString());
			dir_rec.mkdir();//Files.createDirectory(path_rec);
		}*/
			/*if (!dir_snd.exists()) {
			Files.createDirectory(path_snd);
		}*/

			mc_listener = new MC_Listener(MCaddress, MCport);
			mdb_listener = new MDB_Listener(MDBaddress, MDBport);
			mdr_listener = new MDR_Listener(MDRaddress, MDRport);

		} catch(Exception e) { e.printStackTrace();}
		
		if (DEBUG) {
			System.out.println("Storage used: " + storage_space_used);
		}

	}

	public static void start_listeners()
	{
		mc_listener.start();
		mdb_listener.start();
		mdr_listener.start();
		if(loaded_stuff && 	MulticastListener.version == CommunicationThread.Version.two)
			(new Thread(new DeleteEnhancement())).run();
	}

	public static void stop_listeners()
	{
		mc_listener.STOP();
		mdb_listener.STOP();
		mdr_listener.STOP();
	}

	synchronized public static void saveChunkFile(byte[] filedata,String fileid,String chunkNum)
	{
		try{
			FileOutputStream fos = new FileOutputStream(chunk_folder_Path+"/"+fileid+"."+chunkNum);
			fos.write(filedata);
			fos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	synchronized public static void saveRestoredChunkFile(byte[] filedata,String fileid,String chunkNum)
	{
		try{
			FileOutputStream fos = new FileOutputStream(restore_Path+"/"+fileid+"."+chunkNum);
			fos.write(filedata);
			fos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	synchronized public static byte[] getFileChunk(String fileid, String chunkno)
	{
		byte[] data ;
		try{
			Path path = Paths.get(chunk_folder_Path +"/"+fileid+"."+chunkno);
			data = Files.readAllBytes(path);
		}catch(Exception e){e.printStackTrace(); return null;}
		return data;
	}

	synchronized public static void deleteFileChunks(String fileid, String[] chunks)
	{
		for(String chunk:chunks){
			try {
				(new File(chunk_folder_Path+"/"+fileid+"."+chunk)).delete();
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	synchronized public static void deleteFileChunk(String fileid, String chunk)
	{
		try {
			(new File(chunk_folder_Path+"/"+fileid+"."+chunk)).delete();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}


}
