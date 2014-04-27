package com.cs5412.webservices.ml.knn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.utils.Utils;


public class KNN {
	final static int NUM_FOLDS = 5;
	static final Logger LOG = LoggerFactory.getLogger(KNN.class);
	
	public KNN(){}
	
	/**
	 * Creates report for KNN run
	 * @param bw
	 * @param testItems
	 * @param testAccuracy
	 */
	private static void writeToFile(BufferedWriter bw,  List<Item> testItems, double testAccuracy) {
		
		try {
			bw.write("Test Data Classification (id, class, predictedClass)\n");
			for(Item item: testItems){
				bw.write(item.getItemId()+" , "+item.getClassId()+" , "+item.getPredictedClass()+"\n");
			}
			bw.write("\n");
			bw.write("Test Accuracy: "+ testAccuracy);
			bw.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates files for Cross Validation
	 * @param file
	 * @param Content
	 * @param fs
	 */
	private static void writeToFile(String file, List<Item> Content, IFileSystem fs) {
		try {
			BufferedWriter bw =(BufferedWriter) fs.createFileToWrite(file,true);
			for(Item item : Content) {
				bw.append(item.getClassId()+" ");
				Map<Integer,Double> map = item.getFeatureValueMap();
				for(int key: map.keySet()){
					bw.append(key+":"+map.get(key)+" ");
				}
				bw.newLine();
			}
			bw.close();
		}catch(Exception e) {
			LOG.debug("Exception caught: " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Helper method for cross validation
	 * @param trainingDocs
	 * @return
	 */
	private static List<List<Item>> mergeAndShuffleLists(
			List<Item> trainingDocs) {
		List<List<Item>> bigList = new ArrayList<List<Item>>();
		List<Item> tempList = new ArrayList<Item>();
		
		tempList.addAll(trainingDocs);
		
		Collections.shuffle(tempList);
		int size = tempList.size();
		List<Item> list0 = new ArrayList<Item>();
		list0.addAll(tempList.subList(0, size/5));
		List<Item> list1 = new ArrayList<Item>();
		list1.addAll(tempList.subList(size/5, 2*size/5));
		List<Item> list2 = new ArrayList<Item>();
		list2.addAll(tempList.subList(2*size/5, 3*size/5));
		List<Item> list3 = new ArrayList<Item>();
		list3.addAll(tempList.subList(3*size/5, 4*size/5));
		List<Item> list4 = new ArrayList<Item>();
		list4.addAll(tempList.subList(4*size/5, size));
		
		bigList.add(list0);
		bigList.add(list1);
		bigList.add(list2);
		bigList.add(list3);
		bigList.add(list4);
		
		return bigList;
	}
	
	/**
	 * @param file
	 * @param fs
	 * @return
	 */
	private static List<Item> parse(String file, IFileSystem fs) {
		
		double l2Norm = 0.0;
		List<Item> items = new ArrayList<Item>();
		try {
			InputStream fin = (InputStream) fs.readFile(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin));
			String lineBuffer = null;
			
			while((lineBuffer=br.readLine())!=null){
				l2Norm = 0.0;
				String[] tokens = lineBuffer.split("\\s+");
				
				Item item = new Item();
				
				item.setClassId(Integer.parseInt(tokens[0]));
				
				Map<Integer,Double> featureValueMap = new HashMap<Integer, Double>();
				item.setFeatureValueMap(featureValueMap);
				
				for(int i=1; i<tokens.length;i++){
					String token = tokens[i];
					String[] tokenParts = token.split(":");
					int feature=Integer.parseInt(tokenParts[0]);
					double featureValue=Double.parseDouble(tokenParts[1]); 
					l2Norm+=featureValue*featureValue;
					
					featureValueMap.put(feature, featureValue);
				}
				item.setL2norm(Math.sqrt(l2Norm));
				items.add(item);
			}

			br.close();
		} catch (FileNotFoundException e) {
			LOG.debug("There was problem opening a file...");
			e.printStackTrace();
		} catch (IOException e) {
			LOG.debug("There was problem opening a file...");
			e.printStackTrace();
		}
		return items;
	}
	
	/**
	 * Classifies test data
	 * @param k
	 * @param trainingInstances
	 * @param testInstances
	 * @return
	 * @throws IOException
	 */
	private static double classify(int k,
			 List<Item> trainingInstances, List<Item> testInstances) throws IOException {
		
		for(Item testInstance: testInstances){
			//Compute Cosine Similarity
			for(Item trainingInstance: trainingInstances){
				double cosineSimilarity = computeCosineSimilarity(testInstance, trainingInstance);
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
			int predictedClass = findMajorityVote(kNearesrtInstances);
			testInstance.setPredictedClass(predictedClass);
		}
		
		//Compute Metric
		Metric metric = computeParameters(testInstances);
		//metric.display();
		return metric.getAccuracy();
	}
	
	/**
	 * @param testInstances
	 * @param centroidVectors
	 */
	public static Metric computeParameters(List<Item> testInstances) {
		double accuracy=0.0;
		double count = 0,correctPredictions=0;
		
		Metric metric = new Metric();

		
		for(Item testInstance:testInstances){
			int predictedClass= testInstance.getPredictedClass();
			
			count++;
			
			if(predictedClass==testInstance.getClassId()){
				correctPredictions++;
			}
		}
		
		accuracy=correctPredictions/count;
		metric.setAccuracy(accuracy);
		
		return metric;
	}
	
	/**
	 * 
	 * @param kNearestItems
	 * @return
	 */
	public static int findMajorityVote(List<Item> kNearestItems) {
		
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		for(Item item: kNearestItems){
			if(map.get(item.getClassId())==null){
				map.put(item.getClassId(), 0);
			}
			int count = map.get(item.getClassId());
			map.put(item.getClassId(), count+1);
		}
		
		int majorClass = 0;
		int max = Integer.MIN_VALUE;
		
		for(int key: map.keySet()){
			if(max<map.get(key)){
				majorClass=key;
				max=map.get(key);
			}
		}
		return majorClass;
	}

	/**
	 * 
	 * @param testInstance
	 * @param trainingInstance
	 * @return
	 */
	public static double computeCosineSimilarity(Item testInstance,
			Item trainingInstance) {
		
		Map<Integer, Double> trainMap = trainingInstance.getFeatureValueMap();
		Map<Integer, Double> testMap = testInstance.getFeatureValueMap();
		
		if(trainMap==null || testMap==null) return 0;
		
		double numerator = 0;
		double denominator = testInstance.getL2norm()*trainingInstance.getL2norm();
		
		for(int trainingFeature: trainMap.keySet()){
			Double trainingFeatureValue = trainMap.get(trainingFeature);
			Double testFeatureValue = testMap.get(trainingFeature);
			
			if(testFeatureValue!=null){
				numerator=numerator+testFeatureValue*trainingFeatureValue;
			}
		}
		
		if(trainingInstance.getL2norm()==0||testInstance.getL2norm()==0){
			return 0;
		}
		
		return numerator/denominator;
	}
	/**
	 * 
	 * @param trainFile
	 * @param fs
	 * @param crossvalidationBasePath
	 */
	public static void createFiles(String trainFile, IFileSystem fs,String crossvalidationBasePath) {
		
		List<Item> trainingInstances = parse(trainFile, fs);
		List<List<Item>> bigList = mergeAndShuffleLists(trainingInstances);
		
		List<Item> trainingSet1 = new ArrayList<Item>();
		List<Item> validationSet1 = new ArrayList<Item>();
		List<Item> trainingSet2 = new ArrayList<Item>();
		List<Item> validationSet2 = new ArrayList<Item>();
		List<Item> trainingSet3 = new ArrayList<Item>();
		List<Item> validationSet3 = new ArrayList<Item>();
		List<Item> trainingSet4 = new ArrayList<Item>();
		List<Item> validationSet4 = new ArrayList<Item>();
		List<Item> trainingSet5 = new ArrayList<Item>();
		List<Item> validationSet5 = new ArrayList<Item>();
		trainingSet1.addAll(bigList.get(0));trainingSet1.addAll(bigList.get(1));trainingSet1.addAll(bigList.get(2));trainingSet1.addAll(bigList.get(3));validationSet1.addAll(bigList.get(4));
		trainingSet2.addAll(bigList.get(0));trainingSet2.addAll(bigList.get(1));trainingSet2.addAll(bigList.get(2));trainingSet2.addAll(bigList.get(4));validationSet2.addAll(bigList.get(3));
		trainingSet3.addAll(bigList.get(0));trainingSet3.addAll(bigList.get(1));trainingSet3.addAll(bigList.get(3));trainingSet3.addAll(bigList.get(4));validationSet3.addAll(bigList.get(2));
		trainingSet4.addAll(bigList.get(0));trainingSet4.addAll(bigList.get(2));trainingSet4.addAll(bigList.get(3));trainingSet4.addAll(bigList.get(4));validationSet4.addAll(bigList.get(1));
		trainingSet5.addAll(bigList.get(1));trainingSet5.addAll(bigList.get(2));trainingSet5.addAll(bigList.get(3));trainingSet5.addAll(bigList.get(4));validationSet5.addAll(bigList.get(0));
		writeToFile(crossvalidationBasePath+"knn0.train",trainingSet1,fs);
		writeToFile(crossvalidationBasePath+"knn1.train",trainingSet2,fs);
		writeToFile(crossvalidationBasePath+"knn2.train",trainingSet3,fs);
		writeToFile(crossvalidationBasePath+"knn3.train",trainingSet4,fs);
		writeToFile(crossvalidationBasePath+"knn4.train",trainingSet5,fs);
		writeToFile(crossvalidationBasePath+"knn0.valid",validationSet1,fs);
		writeToFile(crossvalidationBasePath+"knn1.valid",validationSet2,fs);
		writeToFile(crossvalidationBasePath+"knn2.valid",validationSet3,fs);
		writeToFile(crossvalidationBasePath+"knn3.valid",validationSet4,fs);
		writeToFile(crossvalidationBasePath+"knn4.valid",validationSet5,fs);
		
	}
	
	/**
	 * 
	 * @param trainingDataHDFSPath
	 * @param validationDataHDFSPath
	 * @param k
	 * @param fs
	 * @return
	 * @throws IOException
	 */
	public static double runValidation(String trainingDataHDFSPath,
			String validationDataHDFSPath, int k, IFileSystem fs) throws IOException {
		List<Item> trainingInstances = parse(trainingDataHDFSPath,fs);
		List<Item> testInstances = parse(validationDataHDFSPath,fs);
		return classify(k, trainingInstances, testInstances);
	}
	
	/**
	 * 
	 * @param username
	 * @param hdfs
	 * @param trainFile
	 * @param testFile
	 * @param bestK
	 * @throws IOException
	 */
	public static void runClassification(String username, IFileSystem hdfs,
			String trainFile, String testFile, int bestK) throws IOException {
		String resultFile = hdfs.getUserPath(username)+Utils.linuxSeparator+"output"+Utils.linuxSeparator+"output.txt";
		FSDataOutputStream fos = hdfs.createHDFSFile(resultFile,true);
		BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(fos));
		List<Item> trainingInstances = parse(trainFile,hdfs);
		List<Item> testInstances = parse(testFile,hdfs);
		double testAccuracy = classify(bestK, trainingInstances, testInstances);
		writeToFile(bw,testInstances,testAccuracy);
		bw.close();
	}
}

