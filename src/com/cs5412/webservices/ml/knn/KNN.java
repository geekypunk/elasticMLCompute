package com.cs5412.webservices.ml.knn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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

import com.cs5412.filesystem.HDFSFileSystemImpl;
import com.cs5412.filesystem.IFileSystem;
import com.cs5412.utils.ServerConstants;


public class KNN {
	final static int NUM_FOLDS = 5;
	static final Logger LOG = LoggerFactory.getLogger(KNN.class);
	private IFileSystem fs;
	private String trainingFile;
	private String testFile;
	private String resultFile;
	private  String workDir;
	public KNN(String trainingFile, String testFile, String resultFile,String workDir,IFileSystem fs){
		this.fs = fs;
		this.resultFile = resultFile;
		this.testFile = testFile;
		this.trainingFile = trainingFile;
		this.workDir = workDir;
	}
	
	public String runKNN() throws InterruptedException, IOException{
		long startTime = System.currentTimeMillis();
		HDFSFileSystemImpl hdfs = (HDFSFileSystemImpl)fs;
		FSDataOutputStream fos = hdfs.createHDFSFile(resultFile,true);
		BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(fos));
		List<Item> trainingInstances = parse(trainingFile);
		List<Item> testInstances = parse(testFile);
		StringBuffer output = new StringBuffer("");
		try {
			int[] kSet = computeKSet(trainingInstances.size());
			List<List<Item>> bigList = mergeAndShuffleLists(trainingInstances);
			perform5FoldCrossValidation(bigList,kSet,bw);
			bw.flush();
			fos.hflush();
			Map<Integer,Double> scoreMap = getScoreMap();
			int bestK = getHighestK(scoreMap);
			LOG.debug("BestK: "+bestK);
			//bw =(BufferedWriter)fs.createFileToWrite(resultFile,false);
			double testAccuracy = classifyInstancesUsingUnweightedKNN(bestK, trainingInstances, testInstances,false,bw);
			writeToFile(resultFile,scoreMap,testInstances,testAccuracy,bw,fs);
			bw.close();
		    long endTime = System.currentTimeMillis();
		    LOG.debug("Elapsed Time: "+(endTime-startTime));
		    LOG.debug("Test accuracy: "+ testAccuracy);
		} catch (IOException e) {
				e.printStackTrace();
				
		}finally{
			
		}
		
		
		return output.toString();
	}
	
	private  Map<Integer, Double> getScoreMap() {
		Map<Integer, Double> scoreMap  = new HashMap<Integer,Double>();
		try {
			InputStream fin = (InputStream) fs.readFile(resultFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
			String line = null;
			
			while((line=reader.readLine())!=null){
				String[] tokens = line.split(",");
				int k = Integer.parseInt(tokens[0]);
				double val = Double.parseDouble(tokens[1]);
				
				if(scoreMap.get(k)==null){
					scoreMap.put(k, 0.0);
				}
				double existingVal = scoreMap.get(k);
				scoreMap.put(k, existingVal+val);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int k: scoreMap.keySet()){
			double val = scoreMap.get(k);
			val/=NUM_FOLDS;
			scoreMap.put(k, val);
		}
		
		return scoreMap;
	}

	private void writeToFile(String resultFile, Map<Integer, Double> scoreMap, List<Item> testItems, 
			double testAccuracy,BufferedWriter bw,IFileSystem fs) {
		
		try {
			bw.write("k, Average Accuracy\n");
			for(int k:scoreMap.keySet()){
				
					bw.write(k+" , "+scoreMap.get(k)+"\n");
			}
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

	private static int getHighestK(Map<Integer, Double> scoreMap) {
		int maxK = -1;
		double maxValue = Double.MIN_NORMAL;
		
		for(int k:scoreMap.keySet()){
			double currValue = scoreMap.get(k);
			if(maxValue<currValue){
				maxK = k;
				maxValue = currValue;
			}
		}
		
		return maxK;
	}

	private void perform5FoldCrossValidation(
			List<List<Item>> bigList, int[] kSet,BufferedWriter bw) throws InterruptedException, IOException {
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
			writeToFile(workDir+"knn1.train",trainingSet1);
			writeToFile(workDir+"knn2.train",trainingSet2);
			writeToFile(workDir+"knn3.train",trainingSet3);
			writeToFile(workDir+"knn4.train",trainingSet4);
			writeToFile(workDir+"knn5.train",trainingSet5);
			writeToFile(workDir+"knn1.valid",validationSet1);
			writeToFile(workDir+"knn2.valid",validationSet2);
			writeToFile(workDir+"knn3.valid",validationSet3);
			writeToFile(workDir+"knn4.valid",validationSet4);
			writeToFile(workDir+"knn5.valid",validationSet5);
			StringBuffer sb = new StringBuffer();
			for(int k: kSet){
				for(int i=1;i<=5;i++){
					List<Item> trainList = parse(workDir+"knn"+i+".train");
					List<Item> validationList = parse(workDir+"knn"+i+".valid");
					Runnable job = new KNNCrossValidationJob(
							k,
							workDir+"knn"+i+".train",
							workDir+"knn"+i+".valid",
							trainList,
							validationList,
							true,
							resultFile,
							sb,
							fs);
					
					Thread modelThread = new Thread(job);
					modelThread.start();
					modelThread.join();
				}
			}
			bw.write(sb.toString());
			
		}
	
	
	private void writeToFile(String file, List<Item> Content) {
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
	
	private static int[] computeKSet(int size) {
		List<Integer> kSet = new ArrayList<Integer>();
		int upperBound = (int) Math.sqrt(size);
		int step = (upperBound-1)/10;
		int count=0;
		for(int k=1; k<=upperBound; k+=step){
			kSet.add(k);
			count++;
		}
		int[] tempArray  = new int[count];
		
		int index=0;
		for(int k: kSet){
			tempArray[index++] = k;
		}
		
		return tempArray;
	}

	private List<Item> parse(String file) {
		
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
	
	
	private double classifyInstancesUsingUnweightedKNN(int k,
			 List<Item> trainingInstances, List<Item> testInstances, boolean validationRun,BufferedWriter bw) throws IOException {
		
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
		if(validationRun){
			
			bw.write(k+","+metric.getAccuracy()+"\n");
		}
		return metric.getAccuracy();
	}
	
	/**
	 * @param testInstances
	 * @param centroidVectors
	 */
	public static Metric computeParameters(List<Item> testInstances) {
		double accuracy=0.0,precision=0.0,recall=0.0;
		double count = 0,correctPredictions=0;
		
		Metric metric = new Metric();
		
		//Map<Integer,Genre> genres = new HashMap<Integer,Genre>();
		//metric.setGenres(genres);
		
		/*for(int i=0;i<5;i++){
			genres.put(i, new Genre());
		}*/
		
		for(Item testInstance:testInstances){
			int predictedClass= testInstance.getPredictedClass();
			
			//Genre genreObj = genres.get(predictedGenre);
			//genreObj.setNoOfPredictionsForThisGenre(genreObj.getNoOfPredictionsForThisGenre()+1);
			
			count++;
			
			if(predictedClass==testInstance.getClassId()){
				correctPredictions++;
				//genreObj.setNoOfCorrectPredictionsForThisGenre(genreObj.getNoOfCorrectPredictionsForThisGenre()+1);
			}
			
			//Genre genreObj2 = genres.get(testBookInstance.getGenre());
			//genreObj2.setNoOfInstances(genreObj2.getNoOfInstances()+1);
			
		}
		
		accuracy=correctPredictions/count;
		metric.setAccuracy(accuracy);
		
		/*for(int i=0;i<5;i++){
			Genre genreObj = genres.get(i);
			precision = 0.0;
			recall = 0.0;
			if(genreObj.getNoOfPredictionsForThisGenre()!=0){
				precision = genreObj.getNoOfCorrectPredictionsForThisGenre()/genreObj.getNoOfPredictionsForThisGenre();
				genreObj.setPrecision(precision);
			}
			if(genreObj.getNoOfInstances()!=0){
				recall = genreObj.getNoOfCorrectPredictionsForThisGenre()/genreObj.getNoOfInstances();
				genreObj.setRecall(recall);
			}
		}*/
		
		return metric;
	}
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
}

