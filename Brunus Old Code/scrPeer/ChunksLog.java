
import java.io.*;
//import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunksLog {//extends Thread {

	/*
	 * Responsible for checking currently or previously stored chunks
	 * */
	
	private static final boolean DEBUG = false;
	
	/*log line
	 * fileid=mac_address.filepath.file_name.last_modification_date
	 * <file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
	 * */

	public enum LogOperation{increase_chunk,decrease_chunck,get_storage_space,has_chunk}
	final static String separator = ">"; //character used to separate infos in log file. can't be a character used in file name
	String log_file_name;
	static final String find_file_pattern = "([0-9]+)"+separator+"[0-9]"+separator+"[0-9]+"+separator+"[0-9]";
	static final String get_file_data_pattern = "([0-9])"+separator+"([0-9]+)"+separator+"([0-9])";
	static final String get_chunk_pattern = "([^>]+)"+separator+"([0-9]+)"+separator+"([0-9])"+separator+"([0-9]+)"+separator+"([0-9])";
	static final String find_owned_pattern = "[^>]+"+separator+"[0-9]+"+separator+"[0-9]"+separator+"[0-9]+"+separator+"([0-9])";
	static private String[] escape_strings=null;

	//private Semaphore log_file_lock = null;

	public ChunksLog(String logname)
	{
		//super(name);
		//operation=op;
		log_file_name=logname;

		//if (log_file_lock == null) log_file_lock= new Semaphore(1);

		escape_strings = new String[4];
		escape_strings[0] = "(";
		escape_strings[1] = ")";
		escape_strings[2] = "{";
		escape_strings[3] = "}";
	}

	//creates new if not found
	//replaces chunk info if found
	public final static int increase_number_of_copies = -1;
	public final static int decrease_number_of_copies = -2;
	public final static int keep_rep_degree = -1;
	public void put_chunk(String fileid,String filechunk,int rep_degree,int count)
	{
		update_chunk(fileid, filechunk, rep_degree, count, true );
	}

	//set owned usage. null->keeps original (0 if doesn't exist and is created)
	//true->set to owned        false-> set to not owned
	//count => 0 sets number of copies to that number
	//count = -1 inc number of copies by 1
	//count < -1 dec number of copies by 1
	//rep_degree <=0 will keep the original rep degree
	synchronized public void update_chunk(String fileid,String filechunk,int rep_degree,int count,Boolean set_owned)
	{
		//file id + chunk number
		String chunk_id = fileid + separator + filechunk/*Integer.toString(filechunk)*/ + separator;
		//escape special characters (- not working right now, if file as parenthesis = trouble)

		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;String input = "";
			while ((line = file.readLine()) != null) input += line + '\n';
			file.close();

			if (DEBUG) System.out.println("rewrite_chunk(read):\n"+input); // check that it's inputted right

			//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
			Pattern pattern = Pattern.compile(chunk_id+get_file_data_pattern);
			Matcher matcher = pattern.matcher(input);
			if (!matcher.find())
			{
				if(DEBUG) System.out.println("chunck_degree_update(not found)");
				log_save_new_chunk_info( fileid, filechunk, rep_degree>0?rep_degree:1, count>=0?count:0,set_owned);
				return;

			}

			int num_copies = Integer.parseInt(matcher.group(2).toString());
			String new_num_copies;
			if (count>=0) new_num_copies = Integer.toString(count);
			else if (count==increase_number_of_copies)  new_num_copies = Integer.toString(num_copies+1);
			else new_num_copies = Integer.toString((num_copies-1>=0? num_copies-1:0));

			String new_rep_degree = rep_degree==keep_rep_degree?matcher.group(1) :Integer.toString(rep_degree);
			
			input = input.replaceFirst(chunk_id + get_file_data_pattern, 
					chunk_id + new_rep_degree +separator+ new_num_copies
					+separator+(set_owned==null? matcher.group(3).toString():
						(set_owned.booleanValue()?"1":"0")
							)	
					);

			// check if the new input is right
			if (DEBUG) System.out.println("chunck_degree_update(after replacing)\n----------------------------------"  + '\n' + input);

			// write the new String with the replaced line OVER the same file
			FileOutputStream fileOut = new FileOutputStream(log_file_name);
			fileOut.write(input.getBytes());
			fileOut.close();

		} catch (Exception e) {
			System.out.println("Problem reading file.");
			return;
		}
	}

	private void log_save_new_chunk_info(String file_id,String chunk_number,int rep_degree,int count,Boolean set_owned )
	{
		StringBuilder msg = new StringBuilder(file_id);
		msg.append(separator);
		msg.append( /*Long.toString(*/chunk_number );
		msg.append(separator);
		msg.append(Integer.toString(rep_degree));
		msg.append(separator);
		msg.append(Integer.toString(count));
		msg.append(separator);
		if(set_owned==null) msg.append("0");
		else msg.append( set_owned.booleanValue()? "1":"0");
		msg.append("\r\n");

		try {
			Files.write(Paths.get(log_file_name),msg.toString().getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("log_save_new_file_info() failed");
		}
	}

	synchronized public Boolean owns_chunk(String fileid,String filechunk) 
	{

		//file id + chunk number
		String chunk_id = fileid + separator + filechunk/*Integer.toString(filechunk)*/ + separator;


		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;String input = "";
			while ((line = file.readLine()) != null) input += line + '\n';
			file.close();

			if (DEBUG) System.out.println("owns_chunk(read):\n"+input); // check that it's inputted right

			//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
			Pattern pattern = Pattern.compile(chunk_id+get_file_data_pattern);
			Matcher matcher = pattern.matcher(input);
			if (!matcher.find())
			{
				if(DEBUG) System.out.println("owns_chunk(not found)");
				return null;

			}
			
			return matcher.group(3).toString().equals("1");
			
		} catch (Exception e) {}
		
			return null;
	}

	//if repdegree>numofcopies returns repdegree else return 0
	synchronized public int enough_copies_ofchunk(String fileid,String filechunk) 
	{
		//file id + chunk number
		String chunk_id = fileid + separator + filechunk/*Integer.toString(filechunk)*/ + separator;

		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;String input = "";
			while ((line = file.readLine()) != null) input += line + '\n';
			file.close();

			if (DEBUG) System.out.println("enough_copies_ofchunk(read):\n"+input); // check that it's inputted right

			//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
			Pattern pattern = Pattern.compile(chunk_id+get_file_data_pattern);
			Matcher matcher = pattern.matcher(input);
			if (!matcher.find())
			{
				if(DEBUG) System.out.println("enough_copies_ofchunk(not found)");
				return 0;

			}
			
			int repdegree=Integer.parseInt(matcher.group(1).toString());
			int numofcopies=Integer.parseInt(matcher.group(2).toString());
			if (repdegree>numofcopies) return repdegree;
			return -1;
			
		} catch (Exception e) {}
		
			return 0;
	}
	
	synchronized public int get_ChunkRepDegree(String fileid,String filechunk) 
	{
		//file id + chunk number
		String chunk_id = fileid + separator + filechunk/*Integer.toString(filechunk)*/ + separator;

		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;String input = "";
			while ((line = file.readLine()) != null) input += line + '\n';
			file.close();

			if (DEBUG) System.out.println("get_ChunkRepDegree(read):\n"+input); // check that it's inputted right

			//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
			Pattern pattern = Pattern.compile(chunk_id+get_file_data_pattern);
			Matcher matcher = pattern.matcher(input);
			if (!matcher.find())
			{
				if(DEBUG) System.out.println("get_ChunkRepDegree(not found)");
				return 0;
			}
			
			return Integer.parseInt(matcher.group(1).toString());
			
		} catch (Exception e) {}
		
			return 0;
	}
	
	synchronized public int get_available_storage_capacity()
	{

		int capacity=0;
		//long used_chunks;
		String read_line = null;
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(log_file_name));
		} catch (FileNotFoundException e) {
			System.err.println("Could not open " + log_file_name );
			return -1;
		}

		do{
			try {
				read_line=in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}

			if (read_line != null) {
				//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
				Pattern pattern = Pattern.compile(find_owned_pattern);
				Matcher matcher = pattern.matcher(read_line);

				if (matcher.find()) 
				{
					if(DEBUG) System.out.println(matcher.group(0).toString());
					if(matcher.group(1).toString().equals("1")) capacity++;
				}
			}
			else break;

		}while(true);

		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}

		return capacity;

	}

	synchronized public String[] delete_file(String fileid)
	{
		if (DEBUG) System.out.println("ChunkLog:delete_file");
		List<String> owned = new ArrayList<>();
		//file id + chunk number
				String filefinder = fileid + separator + find_file_pattern;
				//escape special characters (- not working right now, if file as parenthesis = trouble)
			//	for (String s:escape_strings )
			//		filefinder = filefinder.replace(s,"\\\\"+s);
				//System.out.println(filefinder);

				try {
					// input the file content to the String "input"
					BufferedReader file = new BufferedReader(new FileReader(log_file_name));
					String line;String input = "";
					while ((line = file.readLine()) != null) 
						{
						//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
						Pattern pattern = Pattern.compile(filefinder);
						Matcher matcher = pattern.matcher(line);
						if (!matcher.find()) input += line + '\n';
						else owned.add(matcher.group(1).toString());
						}
					file.close();

					// write the new String with the replaced line OVER the same file
					FileOutputStream fileOut = new FileOutputStream(log_file_name,false);
					fileOut.write(input.getBytes());
					fileOut.close();

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Problem reading file.");
					return null;
				}
				String[] ret = new String[owned.size()];
				ret = owned.toArray(ret);
				return ret;
	}

	//finds chunk that has bigger numcopies-repdegree (returns null if none can be removed)
	//returns fileid in result[0] and chunkno in result[1]
	//return null if none found
	synchronized public String[] find_best_2remove()
	{
		int best_dif = 0;
		String[] best_found = null;

		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;
			while ((line = file.readLine()) != null) 
			{
				//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
				Pattern pattern = Pattern.compile(get_chunk_pattern);
				Matcher matcher = pattern.matcher(line);
				
				if (!matcher.find()){
					//this should never happen
					if (DEBUG) System.out.println("select_best_2remove:unexpected result 001");
				}
				else if (matcher.group(5).toString().equals("1"))//if owned
				{
					int numofpossibleremoves = 
							Integer.parseInt(matcher.group(4).toString()) -//num copies
						Integer.parseInt(matcher.group(3).toString()); //rep degree
					if ( numofpossibleremoves>best_dif)
					{
						if(best_found==null) best_found = new String[2];
						best_dif = numofpossibleremoves;
						best_found[0] = matcher.group(1).toString();//fileid
						best_found[1] = matcher.group(2).toString();//chunkno
					}
					
				}
			}
			file.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem reading file.");
			return null;
		}
		return best_found;
	}

	//returns list of chunk files 2b removed that have num copies > rep degree  --->  map<fileid,filechunks>
	//compare .keyset().size == 0 to know if none was found (if needed)
	synchronized public Map<String, List<String>> findAll_safe_2b_removed()
	{
		if(DEBUG) System.out.println("findAll_safe_2b_removed");
		
		Map<String, List<String>> all_found = new HashMap<>();

		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;
			while ((line = file.readLine()) != null) 
			{
				//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
				Pattern pattern = Pattern.compile(get_chunk_pattern);
				Matcher matcher = pattern.matcher(line);
				
				if (!matcher.find()){
					//this should never happen
					if (DEBUG) System.out.println("findAll_2b_removed:unexpected result 003");
				}
				else if (matcher.group(5).toString().equals("1"))//if owned
				{
					int numofpossibleremoves = 
							Integer.parseInt(matcher.group(4).toString()) -
						Integer.parseInt(matcher.group(3).toString());
					if ( numofpossibleremoves>0)
					{
						String fileid = matcher.group(1).toString();
						if(!all_found.containsKey(fileid)) all_found.put(fileid, new ArrayList<>());
						
						all_found.get(fileid).add(matcher.group(2).toString()); //add chunk
					}
					
				}
			}
			file.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem reading file.");
			return null;
		}
		return all_found;
	}
	
	//returns list of chunk files 2b removed that have 1 < num copies <= rep degree  --->  map<fileid,filechunks>
	//the fisrt map key indictes the diference between rep degree and num of copies
	//so that the space reclaim may be able 2 prioritize the deletion chunks with smaller difference
	synchronized public Map<Integer,Map<String,List<String>>> find_safer2_remove()
	{ return find_safer2_remove(false,null);  }
	synchronized public Map<Integer,Map<String,List<String>>> find_safer2_remove(boolean also_get_with_numCopies_1,RefInteger count )
	{
		if(DEBUG) System.out.println("find_safer2_remove");
		int count_aux = 0;
		
		Map<Integer,Map<String,List<String>>> ascSortedMap = new TreeMap(); 

		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;
			while ((line = file.readLine()) != null) 
			{
				//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
				Pattern pattern = Pattern.compile(get_chunk_pattern);
				Matcher matcher = pattern.matcher(line);
				
				if (!matcher.find()){
					//this should never happen
					if (DEBUG) System.out.println("findAll_lessSafe2b_removed:unexpected result 123");
				}
				else if (matcher.group(5).toString().equals("1"))//if owned
				{
					int num_copies=Integer.parseInt(matcher.group(4).toString());
					int repMinusCopies = 
							Integer.parseInt(matcher.group(3).toString())-num_copies ;
					
					if (num_copies>1||also_get_with_numCopies_1)
					{
						if (!ascSortedMap.containsKey(repMinusCopies))
							ascSortedMap.put(repMinusCopies,new HashMap<>());
						Map<String,List<String>> filechunk = ascSortedMap.get(repMinusCopies);
						
						String fileid = matcher.group(1).toString();
						if(!filechunk.containsKey(fileid)) filechunk.put(fileid, new ArrayList<>());
						
						filechunk.get(fileid).add(matcher.group(2).toString()); //add chunk
						count_aux++;
					}
					
				}
			}
			file.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem reading file.");
			return null;
		}
		if(count!=null) count.setValue(count_aux);
		return ascSortedMap;
	}
	
	//returns the list of chunk files with number of copies equal to 1
	synchronized public Map<String, List<String>> findAllChunks_with1Copy()
	{
		if(DEBUG) System.out.println("findAllChunks_with1Copy");
		
		Map<String, List<String>> all_found = new HashMap<>();

		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;
			while ((line = file.readLine()) != null) 
			{
				//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
				Pattern pattern = Pattern.compile(get_chunk_pattern);
				Matcher matcher = pattern.matcher(line);
				
				if (!matcher.find()){
					//this should never happen
					if (DEBUG) System.out.println("findAllChunks_with1Copy:unexpected result 846");
				}
				else if (matcher.group(5).toString().equals("1"))//if owned
				{
					if ( Integer.parseInt(matcher.group(4).toString())==1)
					{
						String fileid = matcher.group(1).toString();
						if(!all_found.containsKey(fileid)) all_found.put(fileid, new ArrayList<>());
						
						all_found.get(fileid).add(matcher.group(2).toString()); //add chunk
					}
					
				}
			}
			file.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem reading file.");
			return null;
		}
		return all_found;
	}
	
	synchronized public Map<String, String> find_1missingChunk_forAllFiles()
	{
		if(DEBUG) System.out.println("find_1missingChunk_forAllFiles");
		
		Map<String, List<Integer>> all_files = new HashMap<>();

		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;
			while ((line = file.readLine()) != null) 
			{
				//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
				Pattern pattern = Pattern.compile(get_chunk_pattern);
				Matcher matcher = pattern.matcher(line);
				
				if (!matcher.find()){
					//this should never happen
					if (DEBUG) System.out.println("find_1missingChunk_forAllFiles:unexpected result 005");
				}
				else if (matcher.group(5).toString().equals("1"))//if owned
				{
						String fileid = matcher.group(1).toString();
						if(!all_files.containsKey(fileid)) all_files.put(fileid, new ArrayList<>());
						
						all_files.get(fileid).add( Integer.parseInt(matcher.group(2).toString())); //add chunk
				}
			}
			file.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("find_1missingChunk_forAllFiles:Problem reading file.");
			return null;
		}
		
		Map<String, String> result = new HashMap<>();
		for(String file: all_files.keySet())
		{
			List<Integer> chunks = all_files.get(file); 
			Collections.sort(chunks);
			if(chunks.size()>0){
				if(chunks.get(0) > 0) result.put(file, Integer.toBinaryString(0) );
				else for(int i = 1; i < chunks.size() ; ++i)
				{
					int aux = chunks.get(i-1)+1;
					if(aux!=chunks.get(i)) //at least missing chunk num chunks.get(i-1)+1
					{
						result.put(file, Integer.toBinaryString(aux));
						break;
					}
				}
			}
		}

		return result;
	}
	
	synchronized public Map<String, String> find_1Chunk_withRepDegHigherThan1_forAllFiles()
	{
		if(DEBUG) System.out.println("find_1Chunk_withRepDegHigherThan1_forAllFiles");
			
		Map<String, String> result = new HashMap<>();
		try {
			// input the file content to the String "input"
			BufferedReader file = new BufferedReader(new FileReader(log_file_name));
			String line;
			while ((line = file.readLine()) != null) 
			{
				//<file_id> <chunck_number> <repliation_degree> <number_of_copies> <owned>
				Pattern pattern = Pattern.compile(get_chunk_pattern);
				Matcher matcher = pattern.matcher(line);
				
				if (!matcher.find()){
					//this should never happen
					if (DEBUG) System.out.println("find_1Chunk_withRepDegHigherThan1_forAllFiles:unexpected result 006");
				}
				else if (matcher.group(5).toString().equals("1")
						&&Integer.parseInt(matcher.group(3).toString())>1
								)//if owned and rep degree higher than 1
				{
					result.put(matcher.group(1).toString(), matcher.group(2).toString());//filename and chunk num
				}
			}
			file.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("find_1missingChunk_forAllFiles:Problem reading file.");
			return null;
		}
		
		return result;
	}
	
	
	
}
