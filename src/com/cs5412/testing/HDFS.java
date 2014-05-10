package com.cs5412.testing;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFS {

	public static void main(String[] args) throws IOException, URISyntaxException {
		// TODO Auto-generated method stub

		FileSystem hdfs;
		Configuration configuration = new Configuration();
		hdfs = FileSystem.get(new URI("hdfs://128.84.216.64:9000"), configuration );
		hdfs.delete(new Path("hdfs://128.84.216.64:9000/admin101/kernel"));
		hdfs.delete(new Path("hdfs://128.84.216.64:9000/admin101/svm"));
		hdfs.delete(new Path("hdfs://128.84.216.64:9000/admin101/knn"));
		hdfs.delete(new Path("hdfs://128.84.216.64:9000/admin101/dt"));
		
		hdfs.delete(new Path("hdfs://128.84.216.64:9000/admin101/reports"));
		hdfs.mkdirs(new Path("hdfs://128.84.216.64:9000/admin101/reports"));
		
		
	}

}
