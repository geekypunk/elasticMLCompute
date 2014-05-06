package com.cs5412.webservices.ml.kernel;

import java.util.*;
import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.webservices.fileupload.FileUploadServlet;
/**
 * Creates the Cross Validation Files
 * @author pms255
 *
 */
public class CrossValidationFiles {
	static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	private static void getAllTrain(ArrayList<String> allTrain, String file, IFileSystem fs) {
		try {
			InputStream fin = (InputStream) fs.readFile(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin));
			String line = br.readLine();
			while(line != null) {
				if(line.length()!=2) {
					if(line.startsWith("0")) allTrain.add("-1 " + line.substring(2, line.length()));
					else allTrain.add(line);
				}
				line = br.readLine();
			}
			br.close();
		}catch(Exception e) {	
			LOG.debug("Exception caught: " + e.getLocalizedMessage());
		}
	}
	
	private static void AddToFile(String file, ArrayList<String> Content, IFileSystem fs) {
		try {
			BufferedWriter bw =(BufferedWriter) fs.createFileToWrite(file,true);
			for(String str : Content) {
				bw.append(str);bw.newLine();
			}
			bw.close();
		}catch(Exception e) {
			LOG.debug("Exception caught: " + e.getLocalizedMessage());
		}
	}
	/**
	 * Create files required for 5-cross validation
	 * @param trainingDataset
	 * @param fs
	 * @param crossvalidation
	 */
	public static void createFiles(String trainFile, IFileSystem fs, String crossvalidation) {
		ArrayList<String> allTrain = new ArrayList<String>();
		getAllTrain(allTrain, trainFile, fs);
		Collections.shuffle(allTrain);
		List<String> fold1 = allTrain.subList(0, 1421);
		List<String> fold2 = allTrain.subList(1422, 2843);
		List<String> fold3 = allTrain.subList(2844, 4265);
		List<String> fold4 = allTrain.subList(4266, 5687);
		List<String> fold5 = allTrain.subList(5688, 7106);		
		ArrayList<String> train1 = new ArrayList<String>();
		ArrayList<String> train2 = new ArrayList<String>();
		ArrayList<String> train3 = new ArrayList<String>();
		ArrayList<String> train4 = new ArrayList<String>();
		ArrayList<String> train5 = new ArrayList<String>();
		ArrayList<String> val1 = new ArrayList<String>();
		ArrayList<String> val2 = new ArrayList<String>();
		ArrayList<String> val3 = new ArrayList<String>();
		ArrayList<String> val4 = new ArrayList<String>();
		ArrayList<String> val5 = new ArrayList<String>();
		train1.addAll(fold1);train1.addAll(fold2);train1.addAll(fold3);train1.addAll(fold4);
		train2.addAll(fold2);train2.addAll(fold3);train2.addAll(fold4);train2.addAll(fold5);
		train3.addAll(fold3);train3.addAll(fold4);train3.addAll(fold5);train3.addAll(fold1);
		train4.addAll(fold4);train4.addAll(fold5);train4.addAll(fold1);train4.addAll(fold2);
		train5.addAll(fold5);train5.addAll(fold1);train5.addAll(fold2);train5.addAll(fold3);
		val1.addAll(fold5);val2.addAll(fold1);val3.addAll(fold2);val4.addAll(fold3);val5.addAll(fold4);
		
		AddToFile(crossvalidation + File.separator + "SVM1.train", train1, fs);AddToFile(crossvalidation + File.separator + "SVM2.train", train2, fs);AddToFile(crossvalidation + File.separator + "SVM3.train", train3, fs);AddToFile(crossvalidation + File.separator + "SVM4.train", train4, fs);AddToFile(crossvalidation + File.separator + "SVM5.train", train5, fs);
		AddToFile(crossvalidation + File.separator + "SVM1.val", val1, fs);AddToFile(crossvalidation + File.separator + "SVM2.val", val2, fs);AddToFile(crossvalidation + File.separator + "SVM3.val", val3, fs);AddToFile(crossvalidation + File.separator + "SVM4.val", val4, fs);AddToFile(crossvalidation + File.separator + "SVM5.val", val5, fs);
	}
}
