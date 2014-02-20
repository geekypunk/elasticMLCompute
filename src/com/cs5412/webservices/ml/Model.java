package com.cs5412.webservices.ml;

import jnisvmlight.*;

public class Model {
	private static double C[] = {0.0001,0.0005,0.001,0.005,0.01,0.05,0.1};
	public static void create(String fName, int fileNum, int tradeOffNum, String kernel, String modelPath){
		LabeledFeatureVector[] fvVector = GetFeatureVector.readFileToFV(fName + fileNum + ".train");
		String[] args = new String[4];
		args[0] = "-c";
		args[1] = (new Double(C[tradeOffNum])).toString();
		args[2] = "-t";
		args[3] = "0";
		SVMLightInterface svmInterface = new SVMLightInterface();
		SVMLightModel model = svmInterface.trainModel(fvVector, args);
		model.writeModelToFile(modelPath + "Model" + fileNum + "" + (tradeOffNum + 1) + ".model");
	}
	public static void main(String[] args){
		create("E:\\crossvalidation\\SVM", 2, 3, "0", "E:\\crossvalidation\\model\\");
	}
}