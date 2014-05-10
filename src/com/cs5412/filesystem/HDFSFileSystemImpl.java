package com.cs5412.filesystem;


import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

/**
 * Implementation class for IFileSystem
 * @author kt466
 *
 */
public class HDFSFileSystemImpl implements IFileSystem{
	
	private FileSystem hdfs;
	private Configuration configuration = new Configuration();
	private String seperator ="/";

	private int UPLOAD_BUFFER;
	public HDFSFileSystemImpl(PropertiesConfiguration config) throws IOException, URISyntaxException{
		hdfs = FileSystem.get(new URI(config.getString("HDFS_URI")), configuration );
		UPLOAD_BUFFER = config.getInt("UPLOAD_BUFFER");
	}

	@Override
	public void createFile(InputStream is, String filePath) throws IOException {
		Path file = new Path(filePath);
		FSDataOutputStream fs = hdfs.create(file,true);
		int bytesRead = 0;
		byte[] buffer = new byte[UPLOAD_BUFFER];
		while ((bytesRead = is.read(buffer)) > 0) {
			  fs.write(buffer, 0, bytesRead);
		}
		fs.close();		
	}

	@Override
	public String getFilePathForUploads(String fileName,String userName) {
		// TODO Auto-generated method stub
		String path = null;
		if(fileName.contains(".train")){
			path = getUserPath(userName)+seperator+"train"+seperator+fileName;
		}
		else if(fileName.contains(".test")){
			path = getUserPath(userName)+seperator+"test"+seperator+fileName;
		}
		else{
			path = getUserPath(userName)+seperator+"others"+seperator+fileName;
		}
			
		return path;
	}

	@Override
	public List<LocatedFileStatus> getAllUploaded(String userName) throws FileNotFoundException, IOException {
		Path file = new Path(hdfs.getUri()+seperator+userName);
		return getFilesInPath(file);
	}

	@Override
	public List<LocatedFileStatus> getUploadedTrainingDatasets(String userName) throws FileNotFoundException, IOException {
		Path file = new Path(hdfs.getUri()+seperator+userName+seperator+"train");
		return getFilesInPath(file);
	}

	@Override
	public List<LocatedFileStatus> getUploadedTestDatasets(String userName) throws FileNotFoundException, IOException {
		Path file = new Path(hdfs.getUri()+seperator+userName+seperator+"test");
		return getFilesInPath(file);
	}

	@Override
	public BufferedWriter createFileToWrite(String filePath,boolean overWrite) throws IOException {
		Path path = new Path(filePath);
		FSDataOutputStream fos = hdfs.create(path, overWrite); 
		OutputStream os = fos.getWrappedStream();
		BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(os));
		return bw;
		
		
	}

	@Override
	public void createDir(String filePath,boolean overWrite) throws IOException {
		Path path = new Path(filePath);
		hdfs.mkdirs(path); 
		
		
	}
	
	@Override
	public FSDataOutputStream createHDFSFile(String filePath,boolean overWrite) throws IOException {
		Path path = new Path(filePath);
		FSDataOutputStream fos = hdfs.create(path, overWrite); 
		return fos;
		
	}
	
	@Override
	public BufferedWriter appendToFile(String filePath) throws IOException {
		Path path = new Path(filePath);
		FSDataOutputStream fos = hdfs.append(path); 
		BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(fos));
		return bw;
		
		
	}

	@Override
	public String readFileToString(String filePath) throws IOException {
		InputStream in  = readFile(filePath);
		StringWriter writer = new StringWriter();
		IOUtils.copy(in, writer);
		String theString = writer.toString();
		return theString;
	}
	@Override
	public InputStream readFile(String filePath) throws IOException {
		//TODO Have to use BufferedInputStream
		Path path = new Path(filePath);
		FSDataInputStream fin =  hdfs.open(path);
		InputStream in = fin.getWrappedStream();
		return in;
	}
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		hdfs.close();
	}
	@Override
	public boolean deleteFile(String fileName, String userName)
			throws IOException {
		String filePath = getFilePathForUploads(fileName,userName);
		Path file = new Path(filePath);
		return hdfs.delete(file, false);
		
	}
	@Override
	public Path getUserPath(String username){
		Path file = new Path(hdfs.getUri()+seperator+username);
		return file;
		
	}
	@Override
	public List<LocatedFileStatus> getFilesInPath(Path path) throws IOException{
		List<LocatedFileStatus> filesList = new ArrayList<LocatedFileStatus>();
		try{
		
			RemoteIterator<LocatedFileStatus> files = hdfs.listFiles(path, true);
			LocatedFileStatus f;
			while(files.hasNext()){
				f = files.next();
				if(f.isFile()){
					filesList.add(f);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return filesList;
	}

	@Override
	public boolean isPathPresent(String filePath) throws IllegalArgumentException, IOException{
		
		return hdfs.exists(new Path(filePath));
		
	}
	

}
