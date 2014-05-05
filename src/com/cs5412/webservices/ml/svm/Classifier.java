package com.cs5412.webservices.ml.svm;

import jnisvmlight.*;

import java.io.*;
import java.util.*;

import com.cs5412.filesystem.IFileSystem;


/**
 * APIS to classify the validation data using SVM
 * @author pms255
 *
 */
public class Classifier {
	public static ArrayList<ArrayList<Double>> valAccuracies = new ArrayList<ArrayList<Double>>();
	public static ArrayList<Double> avgValAccuracies = new ArrayList<Double>();
	private static void valClassify(int fileNum, String modelPath, String crossvalidation, String of,IFileSystem fs){
		try{
			String modelFile = modelPath + "Model" + fileNum;
			String testFile = crossvalidation + File.separator+"SVM";
			ArrayList<Double> acc = null;
			testFile += fileNum + ".val";
			InputStream fin = (InputStream) fs.readFile(testFile);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(fin));
			LabeledFeatureVector[] fvVectors = GetFeatureVector.readFileToFV(in);
			//BufferedWriter bw = new BufferedWriter(new FileWriter(of, true));
			acc = new ArrayList<Double>();
			for(int i=1;i<=7;i++){
				String mf = modelFile + i + ".model";
				fin = (InputStream) fs.readFile(mf);
				in = new BufferedReader(new InputStreamReader(fin));
				SVMLightModel model = SVMLightModel.readSVMLightModelFromHDFS(in);				
				double total = 0.0, match = 0.0;
				for(LabeledFeatureVector fvVector : fvVectors){
					int label = (int)fvVector.getLabel();
					double prediction = -1.0;
					if(fvVector.getDimAt(fvVector.getDims().length-1) > 60830) {
						int x = (int)(Math.random()*2.0);
						if(x == 1) prediction = 1.0;
					}else prediction = model.classify(fvVector);					
					int intPrediction = 0;
					if(prediction >= 0.0) intPrediction = 1;
					else intPrediction = -1;
					if(intPrediction == label) match += 1.0;
					total += 1.0;
				}
				//bw.append((match/total) + " ");
				acc.add(match/total);
			}
			valAccuracies.add(acc);
			//bw.newLine();
			//bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Perform validation data classification
	 * @param fs
	 * @param crossvalidation
	 * @param modelPath
	 * @param of
	 * @return
	 */
	public static String valClassifyCaller(IFileSystem fs,String crossvalidation, String modelPath, String of){
		of = crossvalidation+File.separator+"output"+File.separator+of;
		valClassify(1, modelPath, crossvalidation, of,fs);
		valClassify(2, modelPath, crossvalidation, of,fs);
		valClassify(3, modelPath, crossvalidation, of,fs);
		valClassify(4, modelPath, crossvalidation, of,fs);
		valClassify(5, modelPath, crossvalidation, of,fs);
		double max = -1.0;
		int maxIndex = -1;
		try{
			BufferedWriter bw =(BufferedWriter) fs.createFileToWrite(of,true);
			bw.newLine();
			double acc1 = (valAccuracies.get(0).get(0) + valAccuracies.get(1).get(0) + valAccuracies.get(2).get(0)+ valAccuracies.get(3).get(0)+ valAccuracies.get(4).get(0))/5.0;
			if(acc1 > max) {
				avgValAccuracies.add(acc1);
				max = acc1;
				maxIndex = 0;
			}
			double acc2 = (valAccuracies.get(0).get(1) + valAccuracies.get(1).get(1) + valAccuracies.get(2).get(1)+ valAccuracies.get(3).get(1)+ valAccuracies.get(4).get(1))/5.0;
			if(acc2 > max) {
				avgValAccuracies.add(acc2);
				max = acc2;
				maxIndex = 1;
			}
			double acc3 = (valAccuracies.get(0).get(2) + valAccuracies.get(1).get(2) + valAccuracies.get(2).get(2)+ valAccuracies.get(3).get(2)+ valAccuracies.get(4).get(2))/5.0;
			if(acc3 > max) {
				avgValAccuracies.add(acc3);
				max = acc3;
				maxIndex = 2;
			}
			double acc4 = (valAccuracies.get(0).get(3) + valAccuracies.get(1).get(3) + valAccuracies.get(2).get(3)+ valAccuracies.get(3).get(3)+ valAccuracies.get(4).get(3))/5.0;
			if(acc4 > max) {
				avgValAccuracies.add(acc4);
				max = acc4;
				maxIndex = 3;
			}
			double acc5 = (valAccuracies.get(0).get(4) + valAccuracies.get(1).get(4) + valAccuracies.get(2).get(4)+ valAccuracies.get(3).get(4)+ valAccuracies.get(4).get(4))/5.0;
			if(acc5 > max) {
				avgValAccuracies.add(acc5);
				max = acc5;
				maxIndex = 4;
			}
			double acc6 = (valAccuracies.get(0).get(5) + valAccuracies.get(1).get(5) + valAccuracies.get(2).get(5)+ valAccuracies.get(3).get(5)+ valAccuracies.get(4).get(5))/5.0;
			if(acc6 > max) {
				avgValAccuracies.add(acc6);
				max = acc6;
				maxIndex = 5;
			}
			double acc7 = (valAccuracies.get(0).get(6) + valAccuracies.get(1).get(6) + valAccuracies.get(2).get(6)+ valAccuracies.get(3).get(6)+ valAccuracies.get(4).get(6))/5.0;
			if(acc7 > max) {
				avgValAccuracies.add(acc7);
				max = acc7;
				maxIndex = 6;
			}
			bw.append(acc1 + " " + acc2 + " " + acc3 + " " + acc4 + " " + acc5 + " " + acc6 + " " + acc7);
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return maxIndex + " " + max;
	}
	
	protected static void main(String[] args){
	}
}
