package com.cs5412.webservices.ml.knn;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cs5412.filesystem.IFileSystem;

public class KNNCrossValidationJob implements Runnable{
	
	int k;
	String trainigFileName;
	String validationFileName;
	String resultFile;
	List<Item> trainingInstances;
	List<Item> testInstances;
	boolean validationRun;
	StringBuffer sb;
	IFileSystem fs;
	
	public KNNCrossValidationJob(
			int k, 
			String trainigFileName,
			String validationFileName,
			List<Item> trainingInstances,
			List<Item> testInstances,
			boolean validationRun,
			String resultFile,
			StringBuffer sb, 
			IFileSystem fs) {
		super();
		this.k = k;
		this.trainigFileName = trainigFileName;
		this.validationFileName = validationFileName;
		this.trainingInstances = trainingInstances;
		this.testInstances = testInstances;
		this.validationRun = validationRun;
		this.resultFile = resultFile;
		this.sb = sb;
		this.fs = fs;
	}

	@Override
	public void run() {
		for(Item testInstance: testInstances){
			//Compute Cosine Similarity
			for(Item trainingInstance: trainingInstances){
				double cosineSimilarity = KNN.computeCosineSimilarity(testInstance, trainingInstance);
				trainingInstance.setCosineSimilarity(cosineSimilarity);
			}
			
			//Sort Instances
			Collections.sort(trainingInstances);

			//Select top k neighbors
			List<Item> kNearesrtInstances = new ArrayList<Item>();
			
			for(int i=0;i<k;i++){
				kNearesrtInstances.add(trainingInstances.get(i));
			}

			//Predict Class
			int predictedClass = KNN.findMajorityVote(kNearesrtInstances);
			testInstance.setPredictedClass(predictedClass);
		}
		
		//Compute Metric
		Metric metric = KNN.computeParameters(testInstances);
		//metric.display();
		if(validationRun){
			//BufferedWriter bw =(BufferedWriter) fs.appendToFile(resultFile);
			sb.append(k+","+metric.getAccuracy()+"\n");
			//bw.write(k+","+metric.getAccuracy()+"\n");
			//bw.close();
		}
	}

}
