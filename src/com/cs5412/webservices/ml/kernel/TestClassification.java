package com.cs5412.webservices.ml.kernel;

import java.io.*;
import java.util.ArrayList;

import com.cs5412.filesystem.IFileSystem;

import jnisvmlight.LabeledFeatureVector;
import jnisvmlight.SVMLightInterface;
import jnisvmlight.SVMLightModel;

public class TestClassification {
	public static double C[] = {0.0001,0.0005,0.001,0.005,0.01,0.05,0.1};
	
	private static LabeledFeatureVector[] readFileToFV(BufferedReader br){
		ArrayList<LabeledFeatureVector> fvList = new ArrayList<LabeledFeatureVector>();
		try{
			String line = br.readLine();
			while(line != null){
				if(line.equals("")){
					line = br.readLine();
					continue;
				}
				LabeledFeatureVector fv = new LabeledFeatureVector();
				ArrayList<Integer> dimsArray = new ArrayList<Integer>();
				ArrayList<Double> valsArray = new ArrayList<Double>();
				String[] splitItems = line.split(" ");
				for(int i=0;i<splitItems.length;i++){
					String[] keyValue = splitItems[i].split(":");
					dimsArray.add(Integer.parseInt(keyValue[0]));
					valsArray.add(Double.parseDouble(keyValue[1]));
					int[] dims = new int[dimsArray.size()];
					double[] vals = new double[valsArray.size()];
					for(int j=0;j<dimsArray.size();j++) dims[j] = dimsArray.get(j);
					for(int j=0;j<valsArray.size();j++) vals[j] = valsArray.get(j);
					fv.setFeatures(dims, vals);
				}
				fvList.add(fv);
				line = br.readLine();
			}
			br.close();			
		}catch(Exception e){
			e.printStackTrace();
		}
		LabeledFeatureVector[] fvarray = new LabeledFeatureVector[fvList.size()];
		for(int i=0;i<fvList.size();i++) fvarray[i] = fvList.get(i);
		return fvarray;
	}
	
	private static SVMLightModel create(BufferedReader trainFilein, int tradeOffNum, String kernel, String kernelParam){
		LabeledFeatureVector[] fvVector = GetFeatureVector.readFileToFV(trainFilein);
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
		return model;
	}
	
	public static void testClassify(int tradeOffNum, String kernel, IFileSystem fs, String trainFile, String testOutputFile, String testFile, String kernelParam){
		try{
			InputStream fin = (InputStream) fs.readFile(trainFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(fin));
			SVMLightModel mainModel = create(in, tradeOffNum, kernel, kernelParam);
			BufferedWriter bw = new BufferedWriter(new FileWriter(testOutputFile));
			fin = (InputStream) fs.readFile(testFile);
			in = new BufferedReader(new InputStreamReader(fin));
			LabeledFeatureVector[] testFV = readFileToFV(in);
			for(LabeledFeatureVector fvVector : testFV){
				double prediction = -1.0;
				if(fvVector.getDimAt(fvVector.getDims().length-1) > 60830) {
					int x = (int)(Math.random()*2.0);
					if(x == 1) prediction = 1.0;
				}else prediction = mainModel.classify(fvVector);					
				int intPrediction = 0;
				if(prediction >= 0.0) intPrediction = 1;
				else intPrediction = -1;
				bw.write(intPrediction + "");
				bw.newLine();
			}
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		//testClassify(2, "0");
	}
}

