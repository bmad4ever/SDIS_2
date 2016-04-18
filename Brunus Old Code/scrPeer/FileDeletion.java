
public class FileDeletion extends Thread {

	//Message msg;
	String file;
	
	FileDeletion(Message msg)
	{
		this.file = msg.header.FileId();
	}
	
	FileDeletion(String file)
	{
		this.file = file;
	}
	
	public void run()
	{
		//get chunks nums owned and remove them from log
		String[] owned = ProgramData.chunkslog.delete_file(file);
		//delete chunks
		ProgramData.deleteFileChunks(file,owned);
		//update storage
		ProgramData.update_storage(-owned.length);
	}
	
}
