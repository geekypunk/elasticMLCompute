package com.cs5412.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface IFileSystem {
	void createFile(InputStream is,String fileName) throws IOException;
	boolean deleteFile(String fileName) throws IOException;
	String getFilePath(String fileName);
	Collection<File> getAllUploaded();
	Collection<File> getUploadedTrainingDatasets();
	Collection<File> getUploadedTestDatasets();
}
