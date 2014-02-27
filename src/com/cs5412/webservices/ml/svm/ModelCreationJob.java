package com.cs5412.webservices.ml.svm;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs5412.filesystem.IFileSystem;

public class ModelCreationJob implements Runnable{
	static final Logger LOG = LoggerFactory.getLogger(ModelCreationJob.class);
	private int fileNum;
	private int tradeOffNum;
	private String modelPath;
	private String crossvalidationPath;
	private IFileSystem fs;
	public ModelCreationJob(int fileNum, int tradeOffNum,String crossvalidation,String modelPath,IFileSystem fs){
		this.fileNum = fileNum;
		this.tradeOffNum = tradeOffNum;
		this.crossvalidationPath = crossvalidation;
		this.modelPath = modelPath;
		this.fs = fs;
	}
	public void run() {
		try{
			Model.create(crossvalidationPath +File.separator+ "SVM" , fileNum, tradeOffNum, "0", modelPath,fs);
			LOG.debug("Finished Model"+fileNum+tradeOffNum);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
