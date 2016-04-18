
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyFilesLog {//extends Thread {

	/*
	 * responsible for storing and checking the client files
	 * used to avoid saving client chunks when restore protocol is initiated by some peer
	 * logline : filename>numchunks>fileid
	 * */
	
	private static final boolean DEBUG = false;
	
	String log_file_name;
	final static String separator=">";
	final static String find_SomeFile_pattern = "[^>]+"+separator+"[^>]+"+separator;
	final static String get_SomeFileID_pattern = separator+"([^>]+)"+separator+"([^>]+)";


	MyFilesLog (String logname)
	{
		log_file_name = logname;
	}

	synchronized public void save_new_file(String filename, String numChunks,String file_id )
	{
		try {
			Files.write(Paths.get(log_file_name),
					(filename
							+separator+numChunks
							+separator+file_id+"\r\n").getBytes()
					, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("log_save_new_file_info() failed");
		}
	}

	synchronized public boolean has_fileid(String fileid) //not yet tested
	{
		Pattern pattern = Pattern.compile( find_SomeFile_pattern+fileid);
			
		try {
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;String input = "";
			while ((line = file.readLine()) != null) 
			{
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) return true;
			}
			//input += line + '\n';
			file.close();

			//Pattern pattern = Pattern.compile(fileid);
			//matcher = pattern.matcher(input);
		}
		catch(Exception e) {e.printStackTrace(); return false;}
		
			return false;//matcher.find();
	}

	//return [fileid,num of chunks]
	synchronized public String[] get_file_info(String filename)
	{
		Pattern pattern = Pattern.compile( filename + get_SomeFileID_pattern);
		
		try {
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;String input = "";
			while ((line = file.readLine()) != null) 
			{
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) return 
						new String[]{matcher.group(2).toString(),matcher.group(1).toString()};
			}
			//input += line + '\n';
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("log_save_new_file_info() failed");
		}
		return null;
	}

	
	synchronized public void delete_file_info(String filename)
	{
		Pattern pattern = Pattern.compile( filename + get_SomeFileID_pattern);

		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;String input = "";
			while ((line = file.readLine()) != null) 
				{
				//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
				Matcher matcher = pattern.matcher(line);
				if (!matcher.find()) input += line + '\n';
				}
			file.close();

			// write the new String with the replaced line OVER the same file
			FileOutputStream fileOut = new FileOutputStream(log_file_name,false);
			fileOut.write(input.getBytes());
			fileOut.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem reading file.");
		}
	}
		

	
}
