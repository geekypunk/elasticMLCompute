package com.cs5412.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface IFileSystem {
	static String CSV_FORMAT=".csv";
	static String CHART_DATA_FORMAT=".chart";
	void createFile(InputStream is,String fileName) throws IOException;
	boolean deleteFile(String fileName) throws IOException;
	String getFilePath(String fileName);
	Collection<File> getAllUploaded();
	Collection<File> getUploadedTrainingDatasets();
	Collection<File> getUploadedTestDatasets();
	void createFile(String text, String fileName) throws IOException;
	String readFileToString(String fileName) throws IOException;
}
