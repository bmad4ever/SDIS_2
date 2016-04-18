

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileUtilities {

	private final static int SIZE_CHUNK = 64000;

	public static String backUpFile(String path, int repdeg) {
		
		String fileid = null;
		String temp2 = null;
		
		String filename = (new File(path)).getName().toString();
		if(ProgramData.myfileslog.get_file_info(filename)!=null)
			return "RESPONSE: BACKUP is not possible because a file with the same name already has been backedtup. Please delete the previous backedup version of the file and upload the new version after.";
		
		byte[] data = new byte[SIZE_CHUNK];
		int count = 0;
		int num_of_files = splitFileChunks(new File(path));
		List<File> temp = listOfFilesToMerge(filename, ProgramData.getTempFolderPath(), num_of_files);
		
		//System.out.println("XXX PATH:" + path);
		fileid = CommunicationThread.getChunkIdentifier(path);
		try {temp2 = CommunicationThread.sha256(fileid);}
		catch(Exception e){  return "RESPONSE: FAILED because of an exception in sha256";}
		
		ProgramData.myfileslog.save_new_file(filename, Integer.toString(
				temp.get(temp.size()-1).length() == 0 ?
						temp.size()-1 : 
							temp.size()
				), temp2);
		
		for(int i=0; i < temp.size(); i++){

			Path chunkPath;
			ChunkBackupSender cbs;
			try {
				chunkPath = Paths.get(temp.get(i).getPath());
				data = Files.readAllBytes(chunkPath);



				cbs = new ChunkBackupSender(temp2, i, repdeg, data);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				cleanDir(new File(ProgramData.getTempFolderPath()));
				ProgramData.myfileslog.delete_file_info(filename);
				return "RESPONSE: BACKUP fail exception 1!";
			}
			RefBoolean stored_received = new RefBoolean(null);
			RefBoolean replication_degree_achieved = new RefBoolean(null);
			cbs.passQuestions(stored_received, replication_degree_achieved);
			cbs.start();
			try {
				cbs.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				cleanDir(new File(ProgramData.getTempFolderPath()));
				e.printStackTrace();
				ProgramData.myfileslog.delete_file_info(filename);
				return "RESPONSE: BACKUP fail exception 2!";
			} 
			boolean completed_Ok = stored_received.getValue() == null ? false: stored_received.getValue();
			boolean completed_Ok_2 = replication_degree_achieved.getValue() == null ? false: replication_degree_achieved.getValue();
			if (!completed_Ok) {
				// TODO
				cleanDir(new File(ProgramData.getTempFolderPath()));
				ProgramData.myfileslog.delete_file_info(filename);
				if(i!=0)
				return "RESPONSE: BACKUP failed. One of the chunks didnt receive STORED confirmation. Please try again "
				+ "laterd or send DELETE so peers can free their space";
				else return "RESPONSE: Failed to backup file. No chunks were backedup";
			}

			if (!completed_Ok_2) {
				count++;
			}
		}
		cleanDir(new File(ProgramData.getTempFolderPath()));
		if (count > 0)
			return "RESPONSE: BACKUP success but replication degree not achived on " + count + "chunks";
		
		return "RESPONSE: success!";
	}

	public static String restoreFile(String file) {
		String[] arr_file_info = null;
		ChunkRestoreGet crg = null;
		arr_file_info = ProgramData.myfileslog.get_file_info(file);
		if(arr_file_info == null)
			return "RESPONSE: RESTORE file not in backup system!";
		int num_of_chunks = Integer.parseInt(arr_file_info[1]);
		System.out.print(num_of_chunks);
		for (int i = 0; i< num_of_chunks; i++) {
			RefBoolean chunk_rec = new RefBoolean(null);
			try {
				crg = new ChunkRestoreGet(arr_file_info[0],i);
				crg.passQuestions(chunk_rec);
				crg.start();
				crg.join();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				cleanDir(new File(ProgramData.getRestoreFolderPath()));
				return "RESPONSE: RESTORE exception in chunk restore get!";
			}
			boolean rcv_Ok = chunk_rec.getValue() == null ? false: chunk_rec.getValue();
			if (!rcv_Ok) {
				// TODO
				cleanDir(new File(ProgramData.getRestoreFolderPath()));
				return "RESPONSE: RESTORE failed chunk num-" + i + " missing";
			}
		}
		List<File> temp = listOfFilesToMerge(arr_file_info[0], ProgramData.getRestoreFolderPath(),num_of_chunks);
		
		if(!mergeFiles(temp, file))
			return "RESPONSE: merged fail!";
		
		cleanDir(new File(ProgramData.getRestoreFolderPath()));
		return "RESPONSE: success";
	}

	public static boolean spaceReclaim(int num) {
		int new_storage_limit = ProgramData.max_storage_space_allowed-num;
		if(new_storage_limit<0) new_storage_limit = 0;
		ProgramData.set_storage_max_space(new_storage_limit);
		if (ProgramData.max_storage_space_allowed >= ProgramData.storage_space_used)
			return true;
		
		Map<Integer,Map<String,List<String>>> toRem= ProgramData.chunkslog.find_safer2_remove();

		//remove files that have more than one copy
		//starts with files that have higher value of -> num copies - deseired rep degree
		for(Integer i:toRem.keySet())
		{
			Map<String, List<String>> files = toRem.get(i);
			for (String fileid : files.keySet()) {

				List<String> value = files.get(fileid);

				if (value != null) {
					for (String chunk : value) {

						if (chunk != null) {
							try {
							SpaceReclaimRemove srr =new SpaceReclaimRemove(fileid, chunk);
							srr.start();
							srr.join();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						if (ProgramData.max_storage_space_allowed >= ProgramData.storage_space_used)
							return true;
					}
				}
			}
		}
		
		//if needed remove files that only our peer has but try to 'backitup' 1st
		Map<String,List<String>> fileschunks = ProgramData.chunkslog.findAllChunks_with1Copy();
		for(String fileid:fileschunks.keySet())
		{
			List<String> chunks = fileschunks.get(fileid); 
			for(String chunk:chunks)
			{
				if (chunk != null) {
					try {
					ChunkBackupSender cbs = new ChunkBackupSender(fileid, 
							chunk,
							ProgramData.chunkslog.get_ChunkRepDegree(fileid, chunk), 
							ProgramData.getFileChunk(fileid,chunk),
							false,false);
					cbs.start();
					cbs.join();
					SpaceReclaimRemove srr = new SpaceReclaimRemove(fileid, chunk);
					srr.start();
					srr.join();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				if (ProgramData.max_storage_space_allowed >= ProgramData.storage_space_used)
					return true;
				
			}
		}
		
		return true;
	}

	public static void saveChunkFile(byte[] filedata,String fileid,long chunkNum, int size)
	{
		try{
			FileOutputStream fos = new FileOutputStream(ProgramData.getTempFolderPath()+"/"+fileid+"."+chunkNum);
			fos.write(filedata, 0, size);
			fos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean mergeFiles(List<File> listfiles, String name) {
		if(!verifyChunksList(listfiles))
			return false;
		File mergedFile = new File(ProgramData.getRestoredFiles()+"/"+ "restored_" +name);
		try (BufferedOutputStream mergingStream = new BufferedOutputStream(
				new FileOutputStream(mergedFile))) {
			for (File f : listfiles) {
				Files.copy(f.toPath(), mergingStream);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return true;
	}

	public static List<File> listOfFilesToMerge(String fileid, String folder) {
		File filesFolder = new File(folder);

		File[] filesChunks = filesFolder.listFiles(
				(File dir, String name) -> name.matches(fileid + "[.]\\d+"));
		//Arrays.sort(filesChunks);
		
		return Arrays.asList(filesChunks);
	}

	public static List<File> listOfFilesToMerge(String fileid, String folder,int numOfFiles) {
		//File filesFolder = new File(folder);

		File[] filesChunks = new File[numOfFiles];
		for(int i = numOfFiles-1 ; i >=0; --i)
		filesChunks[i] = new File(folder+"/"+fileid+"."+Integer.toString(i));
		
				//filesFolder.listFiles(
				//(File dir, String name) -> name.matches(fileid + "[.]\\d+"));
		
		
		/*Arrays.sort(filesChunks, 
				new Comparator<File>(){
			@Override
			public int compare(File s1,File s2) {
			
			Pattern pattern = Pattern.compile(fileid + "[.](\\d+)");
			Matcher matcher = pattern.matcher(s1.getName());
			int value1 = Integer.parseInt( matcher.group(1).toString());
			matcher = pattern.matcher(s2.getName());
			int value2 = Integer.parseInt( matcher.group(1).toString());
			
			return value1-value2; 
			}
		}
				);*/
		
		return Arrays.asList(filesChunks);
	}
	
	public static void printList(List<File> list) {
		for(int i=0; i<list.size(); i++){
			System.out.println(list.get(i).getName());
		}
	}

	public static boolean verifyChunksList(List<File> list) {
		int size = list.size()-1;
		if(list.get(size).length() != 0 && list.get(size).length() > SIZE_CHUNK) {
			System.err.println("verifyChunksList() error: size of last chunk not match");
			return false;
		}

		for(int i=0; i<list.size(); i++){
			String temp = list.get(i).getName();
			int pos = temp.lastIndexOf('.');
			if(Integer.parseInt(temp.substring(pos+1, list.get(i).getName().length())) != i) {
				System.err.println("verifyChunksList() error: chunk file is missing");
				return false;
			}
		}
		return true;
	}

	public static int splitFileChunks(File file) {
		int chunkNum = 0;
		long fileSize = file.length();
		byte[] buffer = new byte[SIZE_CHUNK];

		try (BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(file))) {
			String fileid = file.getName().toString();

			int tmp = 0;
			while ((tmp = bis.read(buffer)) > 0) {
				saveChunkFile(buffer, fileid, chunkNum, tmp);
				chunkNum++;
			}

			if ((fileSize % SIZE_CHUNK) == 0) {
				buffer = new byte[0];
				saveChunkFile(buffer, fileid, chunkNum, 0);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return chunkNum;
	}

	static void cleanDir(File dir) {
		for (File file: dir.listFiles()) {
			file.delete();
		}
	}


}
