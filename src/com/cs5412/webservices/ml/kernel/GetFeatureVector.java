package com.cs5412.webservices.ml.kernel;

import java.io.BufferedReader;
import java.util.ArrayList;

import jnisvmlight.LabeledFeatureVector;
/**
 * Get the feature vector representation of data (training/test)
 * @author pms255
 *
 */
public class GetFeatureVector {
	
	/**
	 * Read the feature vector from file to feature vector
	 * @param trainFilein
	 * @return
	 */
	public static LabeledFeatureVector[] readFileToFV(BufferedReader trainFilein){
		ArrayList<LabeledFeatureVector> fvList = new ArrayList<LabeledFeatureVector>();
		try{
			BufferedReader br = trainFilein;
			String line = br.readLine();
			while(line != null){
				LabeledFeatureVector fv = new LabeledFeatureVector();
				ArrayList<Integer> dimsArray = new ArrayList<Integer>();
				ArrayList<Double> valsArray = new ArrayList<Double>();
				String[] splitItems = line.split(" ");
				if(splitItems.length == 1){
					line = br.readLine();
					continue;
				}
				for(int i=0;i<splitItems.length;i++){
					if(i==0){
						if((int)(Double.parseDouble(splitItems[0])) == 0) fv.setLabel(-1);
						else fv.setLabel(Double.parseDouble(splitItems[0]));
					}else{
						String[] keyValue = splitItems[i].split(":");
						dimsArray.add(Integer.parseInt(keyValue[0]));
						valsArray.add(Double.parseDouble(keyValue[1]));
						int[] dims = new int[dimsArray.size()];
						double[] vals = new double[valsArray.size()];
						for(int j=0;j<dimsArray.size();j++) dims[j] = dimsArray.get(j);
						for(int j=0;j<valsArray.size();j++) vals[j] = valsArray.get(j);
						fv.setFeatures(dims, vals);
					}
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
//	public static void main(String[] args){
//		String filePath = "E:\\crossvalidation\\SVM1.train";
//		LabeledFeatureVector[] fvArray = readFileToFV(filePath);
//		for(LabeledFeatureVector fv : fvArray){
//			System.out.print(fv.getLabel() + " ");
//			for(int i=0;i<fv.getDims().length;i++) {
//				System.out.print(fv.getDimAt(i) + ":" + fv.getValueAt(i) + " ");
//			}
//			System.out.println();
//		}
//	}
}
