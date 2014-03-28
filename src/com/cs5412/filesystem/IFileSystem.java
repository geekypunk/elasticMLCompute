package com.cs5412.filesystem;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;

public interface IFileSystem {
	static String CSV_FORMAT=".csv";
	static String CHART_DATA_FORMAT=".chart";
	void createFile(InputStream is,String fileName) throws IOException;
	boolean deleteFile(String fileName,String userName) throws IOException;
	String getFilePathForUploads(String fileName,String userName);
	Object getAllUploaded(String userName) throws FileNotFoundException, IOException;
	Object getUploadedTrainingDatasets(String userName) throws FileNotFoundException, IOException;
	Object getUploadedTestDatasets(String userName) throws FileNotFoundException, IOException;
	BufferedWriter createFileToWrite(String filePath,boolean overWrite) throws IOException;
	String readFileToString(String fileName) throws IOException;
	void close() throws IOException;
	Object getUserPath(String username);
	InputStream readFile(String filePath) throws IOException;
	BufferedWriter appendToFile(String resultFile) throws IOException;
	void createDir(String filePath, boolean overWrite) throws IOException;
	List<LocatedFileStatus> getFilesInPath(Path path) throws IOException;
	FSDataOutputStream createHDFSFile(String filePath, boolean overWrite)
			throws IOException;
}
