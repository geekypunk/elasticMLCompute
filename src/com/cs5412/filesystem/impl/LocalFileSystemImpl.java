package com.cs5412.filesystem.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.utils.ServerConstants;
/*
 * Implemented using Java NIO(Blocking)
 * Reason : http://stackoverflow.com/questions/1605332/java-nio-filechannel-versus-fileoutputstream-performance-usefulness
 * */
public class LocalFileSystemImpl implements IFileSystem{

	@Override
	public void createFile(InputStream is,String fileName) throws IOException {
		// TODO Auto-generated method stub
		 
		 File storeFile = new File(fileName);
		 ReadableByteChannel rbc = Channels.newChannel(is);
		 FileOutputStream os =  new FileOutputStream(storeFile);
         FileChannel foc = os.getChannel();
         ByteBuffer buf = ByteBuffer.allocateDirect(ServerConstants.UPLOAD_BUFFER);
         while(rbc.read(buf)!= -1) {
             buf.flip();
             foc.write(buf);
             buf.clear();
         }
         os.close();
        
	}
	@Override
	public boolean deleteFile(String fileName,String userName) throws IOException {
		// TODO Auto-generated method stub
		Path p1 = Paths.get(getFilePathForUploads(fileName,userName)+File.separator+fileName);
		return Files.deleteIfExists(p1);
	}

	@Override
	public Collection<File> getAllUploaded(String username) {

		Collection<File> dir = FileUtils.listFiles(new File(ServerConstants.UPLOAD_DIRECTORY_ROOT),
		        TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		return dir;
		
	}
	@Override
	public Collection<File> getUploadedTrainingDatasets(String username) {
		
		Collection<File> dir = FileUtils.listFiles(new File(ServerConstants.UPLOAD_DIRECTORY_TRAIN),
				TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		return dir;
		
	}
	@Override
	public Collection<File> getUploadedTestDatasets(String username) {
		
		Collection<File> dir = FileUtils.listFiles(new File(ServerConstants.UPLOAD_DIRECTORY_TEST),
				TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		return dir;
		
	}

	@Override
	public String getFilePathForUploads(String fileName,String userName) {
		// TODO Auto-generated method stub
		String path = null;
		if(fileName.contains(".train")){
			path = ServerConstants.UPLOAD_DIRECTORY_TRAIN+File.separator+fileName;
		}
		else if(fileName.contains(".test")){
			path = ServerConstants.UPLOAD_DIRECTORY_TEST+File.separator+fileName;
		}
		else{
			path = ServerConstants.UPLOAD_DIRECTORY_OTHER+File.separator+fileName;
		}
			
		return path;
	}

	@Override
	public String readFileToString(String filePath) throws IOException {
		// TODO Auto-generated method stub
		return FileUtils.readFileToString(new File(filePath));
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BufferedWriter createFileToWrite(String filePath,boolean overWrite)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserPath(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream readFile(String filePath) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public BufferedWriter appendToFile(String resultFile) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void createDir(String filePath, boolean overWrite)
			throws IOException {
		// TODO Auto-generated method stub
		
	}


	
}
