package com.cs5412.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface IFileSystem {
	public void createFile(InputStream is,String fileName) throws IOException;
	boolean deleteFile(String fileName) throws IOException;
	public Collection<File> getUploadedDatasets();
}
