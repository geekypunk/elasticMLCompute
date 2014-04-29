package com.cs5412.filesystem;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;

/**
 * Interface governing Hadoop File System implementation semantics
 * @author kt466
 *
 */
public interface IFileSystem {
	static String CSV_FORMAT=".csv";
	static String CHART_DATA_FORMAT=".chart";
	/**
	 * Create a file in HDFS, given a InputStream
	 * @param is
	 * @param fileName
	 * @throws IOException
	 */
	void createFile(InputStream is,String fileName) throws IOException;
	
	/**
	 * Delete a file in HDFS
	 * @param fileName
	 * @param userName
	 * @return
	 * @throws IOException
	 */
	boolean deleteFile(String fileName,String userName) throws IOException;
	
	/**
	 * Return the exact path in HDFS, given a file name and a username
	 * @param fileName
	 * @param userName
	 * @return
	 */
	String getFilePathForUploads(String fileName,String userName);
	
	/**
	 * Get all uploaded datasets for a given user
	 * @param userName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	Object getAllUploaded(String userName) throws FileNotFoundException, IOException;
	
	/**
	 * Get all uploaded training datasets for a given user
	 * @param userName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	Object getUploadedTrainingDatasets(String userName) throws FileNotFoundException, IOException;
	
	/**
	 * Get all uploaded training datasets for a given user
	 * @param userName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	Object getUploadedTestDatasets(String userName) throws FileNotFoundException, IOException;
	
	/**
	 * Open up a file in HDFS to write
	 * @param filePath
	 * @param overWrite
	 * @return
	 * @throws IOException
	 */
	BufferedWriter createFileToWrite(String filePath,boolean overWrite) throws IOException;
	
	/**
	 * Read a file in HDFS to string
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	String readFileToString(String fileName) throws IOException;
	
	/**
	 * Close the HDFS instance
	 * @throws IOException
	 */
	void close() throws IOException;
	
	/**
	 * Get the user namespace in HDFS
	 * @param username
	 * @return
	 */
	Object getUserPath(String username);
	
	/**
	 * Get an InputStream for a file in HDFS
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	InputStream readFile(String filePath) throws IOException;
	
	BufferedWriter appendToFile(String resultFile) throws IOException;
	
	/**
	 * Create a directory in HDFS
	 * @param filePath
	 * @param overWrite
	 * @throws IOException
	 */
	void createDir(String filePath, boolean overWrite) throws IOException;
	
	/**
	 * Get all the files for a given path in HDFS
	 * @param path
	 * @return
	 * @throws IOException
	 */
	List<LocatedFileStatus> getFilesInPath(Path path) throws IOException;
	
	/**
	 * Create file and open it for writing in HDFS
	 * @param filePath
	 * @param overWrite
	 * @return
	 * @throws IOException
	 */
	FSDataOutputStream createHDFSFile(String filePath, boolean overWrite)
			throws IOException;
	
	/**
	 * Check if this path exists in HDFS
	 * @param filePath
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public boolean isPathPresent(String filePath) throws IllegalArgumentException, IOException;
}
