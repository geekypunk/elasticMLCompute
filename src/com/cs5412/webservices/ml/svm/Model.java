package com.cs5412.webservices.ml.svm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.cs5412.filesystem.IFileSystem;

import jnisvmlight.*;

public class Model {
	private static double C[] = {0.0001,0.0005,0.001,0.005,0.01,0.05,0.1};
	public static void create(String fName, int fileNum, int tradeOffNum, String kernel, String modelPath,IFileSystem fs) throws IOException{
		InputStream fin = (InputStream) fs.readFile(fName + fileNum + ".train");
		BufferedReader in = new BufferedReader(new InputStreamReader(fin));
		LabeledFeatureVector[] fvVector = GetFeatureVector.readFileToFV(in);
		String[] args = new String[4];
		args[0] = "-c";
		args[1] = (new Double(C[tradeOffNum])).toString();
		args[2] = "-t";
		args[3] = "0";
		SVMLightInterface svmInterface = new SVMLightInterface();
		SVMLightModel model = svmInterface.trainModel(fvVector, args);
		String path = modelPath + "Model" + fileNum + "" + (tradeOffNum + 1) + ".model";
		BufferedWriter bw =  fs.createFileToWrite(path,true);
		model.writeModelToHDFSFile(bw);
	}
	
}