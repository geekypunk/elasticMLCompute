package com.cs5412.webservices.ml.kernel;

import java.io.*;

import jnisvmlight.*;

import com.cs5412.filesystem.IFileSystem;

public class Model {
	private static double C[] = {0.0001,0.0005,0.001,0.005,0.01,0.05,0.1};
	public static void create(String fName, int fileNum, int tradeOffNum, String kernel, String kernelParam, String modelPath, IFileSystem fs)throws Exception{
		InputStream fin = (InputStream) fs.readFile(fName + fileNum + ".train");
		BufferedReader in = new BufferedReader(new InputStreamReader(fin));
		LabeledFeatureVector[] fvVector = GetFeatureVector.readFileToFV(in);
		String[] args = new String[6];
		args[0] = "-c";
		args[1] = (new Double(C[tradeOffNum])).toString();
		args[2] = "-t";
		args[3] = kernel;
		if(kernel.equals("1")) args[4] = "-d";               //Polynomial kernel
		else if(kernel.equals("2")) args[4] = "-g";          //Radial Basis Function
		else if(kernel.equals("3")) args[4] = "-s";          //Sigmoid Function
		args[5] = kernelParam;
		SVMLightInterface svmInterface = new SVMLightInterface();
		SVMLightModel model = svmInterface.trainModel(fvVector, args);
		String path = modelPath + "Model" + fileNum + "" + (tradeOffNum + 1) + ".model";
		BufferedWriter bw =  fs.createFileToWrite(path,true);
		model.writeModelToHDFSFile(bw);
	}
	public static void main(String[] args){
		//create("E:\\crossvalidation\\SVM", 2, 3, "0", "E:\\crossvalidation\\model\\");
	}
}
