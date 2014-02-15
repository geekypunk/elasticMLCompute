package com.cs5412.filesystem.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class FileSystemImpl implements IFileSystem{

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
	public boolean deleteFile(String fileName) throws IOException {
		// TODO Auto-generated method stub
		Path p1 = Paths.get(ServerConstants.UPLOAD_DIRECTORY+File.separator+fileName);
		return Files.deleteIfExists(p1);
	}

	@Override
	public Collection<File> getUploadedDatasets() {

		Collection<File> dir = FileUtils.listFiles(new File(ServerConstants.UPLOAD_DIRECTORY),
		        TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		return dir;
		
	}


	
}
