package com.cs5412.webservices.ml.wsd;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.webservices.ml.wsd.domain.TestWord;
import com.cs5412.webservices.ml.wsd.domain.Word;

public class WSDJob implements Runnable{
	
	int CO_WINDOW;
	int CL_WINDOW;
	String trainigFileName;
	String testFileName;
	boolean validationRun;
	StringBuffer sb;
	IFileSystem fs;
	
	public WSDJob(
			int CO_WINDOW, 
			int CL_WINDOW,
			String trainigFileName,
			String validationFileName,
			boolean validationRun,
			StringBuffer sb, 
			IFileSystem fs) {
		super();
		this.CO_WINDOW = CO_WINDOW;
		this.CL_WINDOW = CL_WINDOW;
		this.trainigFileName = trainigFileName;
		this.testFileName = validationFileName;
		this.validationRun = validationRun;
		this.sb = sb;
		this.fs = fs;
	}

	@Override
	public void run() {
		
		try {
			Map<String, Word> trainingModelRaw = NaiveBayesWSD.parse(trainigFileName,fs);
			Map<String,Word> trainingModel = NaiveBayesWSD.process(trainingModelRaw, CO_WINDOW, CL_WINDOW);
			List<TestWord> testWordList = NaiveBayesWSD.parseTestFile(testFileName,fs);
			NaiveBayesWSD.processTestWordList(testWordList,CO_WINDOW, CL_WINDOW);
			NaiveBayesWSD.classify(trainingModel,testWordList);
			double accuracy = NaiveBayesWSD.computeParameters(testWordList);
			
			if(validationRun){
				sb.append(CO_WINDOW+","+CL_WINDOW+","+accuracy+":");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
