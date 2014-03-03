package com.cs5412.filesystem.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.utils.ServerConstants;
import com.google.common.collect.Lists;

public class HDFSFileSystemImpl implements IFileSystem{
	
	private FileSystem hdfs;
	private Configuration configuration = new Configuration();
	
	public HDFSFileSystemImpl(String HDFS_URI) throws IOException, URISyntaxException{
		hdfs = FileSystem.get( new URI( HDFS_URI ), configuration );
	}

	@Override
	public void createFile(InputStream is, String filePath) throws IOException {
		Path file = new Path(filePath);
		FSDataOutputStream fs = hdfs.create(file,true);
		int bytesRead = 0;
		byte[] buffer = new byte[ServerConstants.UPLOAD_BUFFER];
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
			path = getUserPath(userName)+File.separator+"train"+File.separator+fileName;
		}
		else if(fileName.contains(".test")){
			path = getUserPath(userName)+File.separator+"test"+File.separator+fileName;
		}
		else{
			path = getUserPath(userName)+File.separator+"others"+File.separator+fileName;
		}
			
		return path;
	}

	@Override
	public List<LocatedFileStatus> getAllUploaded(String userName) throws FileNotFoundException, IOException {
		Path file = new Path(hdfs.getUri()+File.separator+userName);
		return getFilesInPath(file);
	}

	@Override
	public List<LocatedFileStatus> getUploadedTrainingDatasets(String userName) throws FileNotFoundException, IOException {
		Path file = new Path(hdfs.getUri()+File.separator+userName+File.separator+"train");
		return getFilesInPath(file);
	}

	@Override
	public List<LocatedFileStatus> getUploadedTestDatasets(String userName) throws FileNotFoundException, IOException {
		Path file = new Path(hdfs.getUri()+File.separator+userName+File.separator+"test");
		return getFilesInPath(file);
	}

	@Override
	/* 
	 ** 
	 * */
	public BufferedWriter createFileToWrite(String filePath,boolean overWrite) throws IOException {
		Path path = new Path(filePath);
		FSDataOutputStream fos = hdfs.create(path, overWrite); 
		OutputStream os = fos.getWrappedStream();
		BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(os));
		return bw;
		
	}
	
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
		Path file = new Path(hdfs.getUri()+File.separator+username);
		return file;
		
	}
	private List<LocatedFileStatus> getFilesInPath(Path path) throws IOException{
		RemoteIterator<LocatedFileStatus> files = hdfs.listFiles(path, true);
		List<LocatedFileStatus> filesList = Lists.newArrayList();
		LocatedFileStatus f;
		while(files.hasNext()){
			f = files.next();
			if(f.isFile()){
				filesList.add(f);
			}
		}
		return filesList;
	}

	@Override
	public void createUserSpace(String username) throws IOException {
		// TODO Auto-generated method stub
		Path path= getUserPath(username);
		boolean isPresent = hdfs.exists(path);
		if(!isPresent)
			hdfs.create(path);
		
	}

}
