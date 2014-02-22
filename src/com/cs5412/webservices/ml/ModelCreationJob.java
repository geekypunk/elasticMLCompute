package com.cs5412.webservices.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelCreationJob implements Runnable{
	static final Logger LOG = LoggerFactory.getLogger(ModelCreationJob.class);
	private int fileNum;
	private int tradeOffNum;
	public ModelCreationJob(int fileNum, int tradeOffNum){
		this.fileNum = fileNum;
		this.tradeOffNum = tradeOffNum;
	}
	public void run() {
		try{
			Model.create(MyListener.crossvalidation + "SVM" , fileNum, tradeOffNum, "0", MyListener.model);
			LOG.debug("Finished Model"+fileNum+tradeOffNum);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
