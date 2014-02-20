package com.cs5412.webservices.ml;



public class ModelCreationJob implements Runnable{

	private int fileNum;
	private int tradeOffNum;
	public ModelCreationJob(int fileNum, int tradeOffNum){
		this.fileNum = fileNum;
		this.tradeOffNum = tradeOffNum;
	}
	public void run() {
		try{
			Model.create(MyListener.crossvalidation + "SVM" , fileNum, tradeOffNum, "0", MyListener.model);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
