package com.cs5412.webservices.ml.wsd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs5412.filesystem.HDFSFileSystemImpl;
import com.cs5412.filesystem.IFileSystem;
import com.cs5412.utils.Utils;
import com.cs5412.webservices.ml.knn.KNN;
import com.cs5412.webservices.ml.wsd.domain.Sense;
import com.cs5412.webservices.ml.wsd.domain.SenseModel;
import com.cs5412.webservices.ml.wsd.domain.TestWord;
import com.cs5412.webservices.ml.wsd.domain.Word;
import com.cs5412.webservices.ml.wsd.util.POSUtil;


public class NaiveBayesWSD {
	
	static final Logger LOG = LoggerFactory.getLogger(KNN.class);
	
	final static CharArraySet STOPWORDS = CharArraySet.copy(Version.LUCENE_44, StandardAnalyzer.STOP_WORDS_SET);
	private static Version matchVersion = Version.LUCENE_44;
	static List<WSDConfig> scoreList;
	static int[] coWindowSet = new int[]{1,3,5};
	static int[] clWindowSet = new int[]{2,4,6};

	public static void runClassification(String userName, IFileSystem fs, String trainingFile, 
			                             String testFile, Integer bestCoWindow, Integer bestClWindow) throws IOException, InterruptedException {
		String outputFileName = "wsdOutput.txt";
		String resultFile = fs.getUserPath(userName)+Utils.linuxSeparator+"reports"+Utils.linuxSeparator+outputFileName;
		HDFSFileSystemImpl hdfs = (HDFSFileSystemImpl)fs;
		FSDataOutputStream fos = hdfs.createHDFSFile(resultFile,true);
		BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(fos));
		Map<String,Word> trainingModelRaw = parse(trainingFile,fs);
		Map<String,Word> trainingModel = process(trainingModelRaw,bestCoWindow,bestClWindow);
		List<TestWord> testWordList = parseTestFile(testFile,fs);
		processTestWordList(testWordList,bestCoWindow,bestClWindow);
		classify(trainingModel,testWordList);
		writeToOutputFile(bw,testWordList);
	}
	
	public static void tuneParameters(String trainingFile, String validationFile, IFileSystem fs) throws InterruptedException{
		
		
		StringBuffer resultString = new StringBuffer();
		
		for(int coWindow: coWindowSet){
			for(int clWindow: clWindowSet){
				Runnable job = new WSDJob(coWindow, clWindow, trainingFile,
						                  validationFile, true, resultString, fs);
				Thread validationThread = new Thread(job);
				validationThread.start();
				validationThread.join();
			}
		}
		
		scoreList = getScoreList(resultString.toString());
	}
	
	public static double tuneParametersSub(String trainingFile, String validationFile, int coWindow, 
			                             int clWindow, IFileSystem fs) throws InterruptedException, IOException{
		
		Map<String,Word> trainingModelRaw = parse(trainingFile,fs);
		Map<String,Word> trainingModel = process(trainingModelRaw,coWindow,clWindow);
		List<TestWord> testWordList = parseTestFile(validationFile,fs);
		processTestWordList(testWordList,coWindow,clWindow);
		classify(trainingModel,testWordList);
		double accuracy = computeParameters(testWordList);
		return accuracy;	
	}

	private static List<WSDConfig> getScoreList(String resultString) {
		List<WSDConfig> scoreList = new ArrayList<WSDConfig>();
		String[] wsdConfigTokens = resultString.split(":");
		
		for(String wsdConfigToken: wsdConfigTokens){
			String[] wsdConfigSubTokens = wsdConfigToken.split(",");
			WSDConfig currWSDConfig = new WSDConfig(Integer.parseInt(wsdConfigSubTokens[0]),
					                                Integer.parseInt(wsdConfigSubTokens[1]),
					                                Double.parseDouble(wsdConfigSubTokens[2]));
			scoreList.add(currWSDConfig);
		}
		
		Collections.sort(scoreList);
		return scoreList;
	}

	private static void writeToOutputFile(BufferedWriter writer, List<TestWord> testWordList) throws IOException {
		
		writer.write("Id,Prediction\n");
		int index=1;
		for(TestWord testWord: testWordList){
			writer.write(index+","+testWord.getSense().getPredictedSenseId()+"\n");
			index++;
		}
		
		writer.close();
	}

	public static double computeParameters(List<TestWord> testWordList) {
		double correctPredictions = 0;
		for(TestWord testWord: testWordList ){
			Sense sense = testWord.getSense();
			if(sense.getSenseId()==sense.getPredictedSenseId()){
				correctPredictions++;
			}
		}
		double total = testWordList.size();
		double accuracy = correctPredictions/total;
		System.out.println("Accuracy: "+ accuracy);
		return accuracy;		
	}

	public static void classify(Map<String, Word> trainingModel,
			List<TestWord> testWordList) {
		
		for(TestWord testWord: testWordList){
			Word trainingWord = trainingModel.get(testWord.getWordText());
			Map<Integer,SenseModel> trainSenseMap = trainingWord.getSenseMap();
			int predictedSenseId = getMLSense(testWord.getSenseModel(),trainSenseMap,trainingWord.getTotalOccurances());
			testWord.getSense().setPredictedSenseId(predictedSenseId);
		}
	}

	private static int getMLSense(SenseModel testSenseModel,
			Map<Integer, SenseModel> trainSenseMap, int total) {
		int bestSenseId = 0;
		double maxEstimate = Double.NEGATIVE_INFINITY;
		
		for(int trainSenseId: trainSenseMap.keySet()){
			SenseModel currTrainSenseModel = trainSenseMap.get(trainSenseId);
			double currEstimate = getSimilarity(testSenseModel,currTrainSenseModel, total);
			if(currEstimate>maxEstimate){
				bestSenseId = trainSenseId;
				maxEstimate = currEstimate;
			}
		}
		
		return bestSenseId;
	}

	private static double getSimilarity(SenseModel testSenseModel,
			SenseModel currTrainSenseModel, int total) {
		double similarity = 0.0;
		
		//add co-occurrence estimates
		Map<String,Integer> testCoMap = testSenseModel.getCoMap();
		Map<String,Integer> trainCoMap = currTrainSenseModel.getCoMap();
		
		similarity = getSimilarityContri(currTrainSenseModel, total,
					testCoMap, trainCoMap);
		
		//add collocational features estimates
		//previous list
		List<Map<String,Integer>> testPrevList = testSenseModel.getPrevList();
		List<Map<String,Integer>> trainPrevList = currTrainSenseModel.getPrevList();
		
		int min = testPrevList.size()<trainPrevList.size()?testPrevList.size():trainPrevList.size();
		
		for(int i=0; i<min;i++){
			Map<String,Integer> currtestCLMap = testPrevList.get(i);
			Map<String,Integer> currtrainCLMap = trainPrevList.get(i);
			
			similarity += getSimilarityContri(currTrainSenseModel, total,
					currtestCLMap, currtrainCLMap);
			
		}
		
		//forward list
		List<Map<String,Integer>> testForwList = testSenseModel.getForwardList();
		List<Map<String,Integer>> trainForwList = currTrainSenseModel.getForwardList();
		
		min = testForwList.size()<trainForwList.size()?testForwList.size():trainForwList.size();
		
		for(int i=0; i<min;i++){
			Map<String,Integer> currtestCLMap = testForwList.get(i);
			Map<String,Integer> currtrainCLMap = trainForwList.get(i);
			
			similarity += getSimilarityContri(currTrainSenseModel, total,
					currtestCLMap, currtrainCLMap);
			
		}
		
		
		//add collocational POS features estimates
		//previous POS list
		testPrevList = testSenseModel.getPrevPOSList();
		trainPrevList = currTrainSenseModel.getPrevPOSList();
		
		min = testPrevList.size()<trainPrevList.size()?testPrevList.size():trainPrevList.size();
		
		for(int i=0; i<min;i++){
			Map<String,Integer> currtestCLMap = testPrevList.get(i);
			Map<String,Integer> currtrainCLMap = trainPrevList.get(i);
			
			similarity += getSimilarityContri(currTrainSenseModel, total,
					currtestCLMap, currtrainCLMap);
			
		}
		
		//forward POS list
		testForwList = testSenseModel.getForwardPOSList();
		trainForwList = currTrainSenseModel.getForwardPOSList();
		
		min = testForwList.size()<trainForwList.size()?testForwList.size():trainForwList.size();
		
		for(int i=0; i<min;i++){
			Map<String,Integer> currtestCLMap = testForwList.get(i);
			Map<String,Integer> currtrainCLMap = trainForwList.get(i);
			
			similarity += getSimilarityContri(currTrainSenseModel, total,
					currtestCLMap, currtrainCLMap);
			
		}
		
		//add special features estimate
		
		List<Map<String,Integer>> testSpclList = testSenseModel.getSpecialCLList();
		List<Map<String,Integer>> trainSpclList = currTrainSenseModel.getSpecialCLList();
		
		min = testSpclList.size()<trainSpclList.size()?testSpclList.size():trainSpclList.size();
		
		for(int i=0; i<min;i++){
			Map<String,Integer> currtestCLMap = testSpclList.get(i);
			Map<String,Integer> currtrainCLMap = trainSpclList.get(i);
			
			similarity += getSimilarityContri(currTrainSenseModel, total,
					currtestCLMap, currtrainCLMap);
			
		}
		
		//add special POS features estimate
		List<Map<String,Integer>> testSpclPOSList = testSenseModel.getSpecialPOSCLList();
		List<Map<String,Integer>> trainSpclPOSList = currTrainSenseModel.getSpecialPOSCLList();
		
		min = testSpclPOSList.size()<trainSpclPOSList.size()?testSpclPOSList.size():trainSpclPOSList.size();
		
		for(int i=0; i<min;i++){
			Map<String,Integer> currtestCLMap = testSpclPOSList.get(i);
			Map<String,Integer> currtrainCLMap = trainSpclPOSList.get(i);
			
			similarity += getSimilarityContri(currTrainSenseModel, total,
					currtestCLMap, currtrainCLMap);
			
		}
		
		//multiply prior probability
		similarity+=Math.log((double)currTrainSenseModel.getTotalOccurances()/total);
		return similarity;
	}

	/**
	 * @param currTrainSenseModel
	 * @param total
	 * @param similarity
	 * @param testCoMap
	 * @param trainCoMap
	 * @return
	 */
	private static double getSimilarityContri(SenseModel currTrainSenseModel,
			int total, Map<String, Integer> testCoMap,
			Map<String, Integer> trainCoMap) {
		double similarity=0.0;
		for(String testWord: testCoMap.keySet()){
			int testWordFreq = testCoMap.get(testWord);
			Integer trainWordFreq = trainCoMap.get(testWord);
			if(trainWordFreq==null || "-".equals(testWord)){
				trainWordFreq = 0;
			}
			double contribution = (Math.log(((double)(trainWordFreq+1)/(currTrainSenseModel.getTotalOccurances()+total))))+(Math.log(testWordFreq));
			similarity+=contribution;
		}
		return similarity;
	}

	public static void processTestWordList(List<TestWord> testWordList, int CO_WINDOW, int CL_WINDOW) throws IOException {
		
		for(TestWord word: testWordList){
			loadSenseIntoModel(word.getSense(), word.getSenseModel(),CO_WINDOW, CL_WINDOW );
		}
	}

	public static List<TestWord> parseTestFile(String filePath, IFileSystem fs) throws IOException {
		List<TestWord> wordList = new ArrayList<TestWord>();
		BufferedReader bufferedReader = getReader(filePath,fs);
		String line = null;
		while((line=bufferedReader.readLine())!=null){
			String[] headTokens = line.split("\\|");
			
			String currWord = headTokens[0].trim().toLowerCase();
			
			TestWord word = new TestWord();
			word.setWordText(currWord);
			
			Sense sense = new Sense();
			word.setSense(sense);
			
			sense.setSenseId(Integer.parseInt(headTokens[1].trim()));
			String[] subTokens = headTokens[2].split("%%");
			sense.setPrevContext(subTokens[0].trim().toLowerCase());
			sense.setTarget(subTokens[1].trim().toLowerCase());
			if(subTokens[2].length()==3){
				sense.setNextContext(subTokens[2].trim().toLowerCase());
			}
			
			wordList.add(word);
		}
		bufferedReader.close();
		return wordList;
	}

	public static Map<String, Word> process(Map<String, Word> wordModelRaw, int CO_WINDOW, int CL_WINDOW) throws IOException {
		
		for(String wordTxt :wordModelRaw.keySet()){
			Word word = wordModelRaw.get(wordTxt);
			List<Sense> senseList = word.getSenseList();
			Map<Integer,SenseModel> senseMap = new HashMap<Integer,SenseModel>();
			word.setSenseMap(senseMap);
			for(Sense sense: senseList){
				loadSense(sense,senseMap,CO_WINDOW, CL_WINDOW);
			}
		}
		return wordModelRaw;
	}

	private static void loadSense(Sense sense, Map<Integer, SenseModel> senseMap, int CO_WINDOW, int CL_WINDOW) throws IOException {
		int currSenseId = sense.getSenseId();
		if(senseMap.get(currSenseId)==null){
			senseMap.put(currSenseId,new SenseModel());
		}
		SenseModel model = senseMap.get(currSenseId);
		model.incrementOccurances();
		
		loadSenseIntoModel(sense, model, CO_WINDOW, CL_WINDOW);
	}

	/**
	 * @param sense
	 * @param model
	 * @throws IOException
	 */
	private static void loadSenseIntoModel(Sense sense, SenseModel model,int CO_WINDOW, int CL_WINDOW)
			throws IOException {
		//load co-occurrence features
		String prevContext = sense.getPrevContext();
		prevContext = process(prevContext).trim();
		String nextContext = sense.getNextContext();
		nextContext = process(nextContext).trim();
		String[] prevTokens = removeStopWordsAndStem(prevContext).split("\\s+");
		String[] nextTokens = removeStopWordsAndStem(nextContext).split("\\s+");
		String[] lastKTokens = getLastKTokens(prevTokens,CO_WINDOW);
		String[] firstKTokens = getFirstKTokens(nextTokens,CO_WINDOW);
		loadTokens(lastKTokens,model);
		loadTokens(firstKTokens,model);
		
		//load collocational features
		prevTokens = prevContext.split("\\s+");
		nextTokens = nextContext.split("\\s+");
		lastKTokens = getLastKTokens(prevTokens,CL_WINDOW);
		firstKTokens = getFirstKTokens(nextTokens,CL_WINDOW);
		loadCLTokensPrev(lastKTokens,model.getPrevList(),CL_WINDOW);
		loadCLTokensForw(firstKTokens,model.getForwardList(),CL_WINDOW);
		
		//load POS features
		String[] lastKTokensPOS = POSUtil.getPOSTags(lastKTokens);
		String[] firstKTokensPOS = POSUtil.getPOSTags(firstKTokens);
		loadCLTokensPrev(lastKTokensPOS,model.getPrevPOSList(),CL_WINDOW);
		loadCLTokensForw(firstKTokensPOS,model.getForwardPOSList(),CL_WINDOW);
		
		//load special CL features & load special CL POS features
		String[] spclClTokens = new String[]{"-","-","-","-","-","-"};
		String[] spclClPOSTokens = new String[]{"-","-","-","-","-","-"};
		
		if(lastKTokens.length>=2){
			spclClTokens[0]=lastKTokens[1]+lastKTokens[0];
			spclClPOSTokens[0]=(POSUtil.getPOSTags(new String[]{lastKTokens[1]+lastKTokens[0]+""}))[0];
		}
		if(lastKTokens.length>=1){
			spclClTokens[1]=lastKTokens[0]+sense.getTarget();
			spclClPOSTokens[1]=(POSUtil.getPOSTags(new String[]{lastKTokens[0]+sense.getTarget()+""}))[0];
		}
		if(firstKTokens.length>=1){
			spclClTokens[2]=sense.getTarget()+firstKTokens[0];
			spclClPOSTokens[2]=(POSUtil.getPOSTags(new String[]{sense.getTarget()+firstKTokens[0]+""}))[0];
		}
		if(firstKTokens.length>=2){
			spclClTokens[3]=firstKTokens[0]+firstKTokens[1];
			spclClPOSTokens[3]=(POSUtil.getPOSTags(new String[]{firstKTokens[0]+firstKTokens[1]+""}))[0];
		}
		if(lastKTokens.length>=1 && firstKTokens.length>=1){
			spclClTokens[4]=lastKTokens[0]+sense.getTarget()+firstKTokens[0];
			spclClPOSTokens[4]=(POSUtil.getPOSTags(new String[]{lastKTokens[0]+sense.getTarget()+firstKTokens[0]+""}))[0];
		}

		spclClTokens[5]=sense.getTarget();
		spclClPOSTokens[5]=(POSUtil.getPOSTags(new String[]{sense.getTarget()}))[0];
		
		loadSpclFeatures(spclClTokens,model.getSpecialCLList());
		loadSpclFeatures(spclClPOSTokens,model.getSpecialPOSCLList());
	}
	
	private static void loadSpclFeatures(String[] spclClTokens,
			List<Map<String, Integer>> specialCLList) {
		
		for(int i=0; i<spclClTokens.length;i++){
			if(i>=specialCLList.size()){
				specialCLList.add(new HashMap<String,Integer>());
			}
			Map<String,Integer> map = specialCLList.get(i);
			if(map.get(spclClTokens[i])==null){
				map.put(spclClTokens[i], 0);
			}
			int count = map.get(spclClTokens[i]);
			map.put(spclClTokens[i], count+1);
		}
	}

	private static void loadCLTokensForw(String[] firstKTokens,
			List<Map<String, Integer>> list, int clWindow) {
		int min = clWindow<firstKTokens.length?clWindow:firstKTokens.length;
		for(int i=0; i<min;i++){
			if(i>=list.size()){
				list.add(new HashMap<String,Integer>());
			}
			Map<String,Integer> map = list.get(i);
			if(map.get(firstKTokens[i])==null){
				map.put(firstKTokens[i], 0);
			}
			int count = map.get(firstKTokens[i]);
			map.put(firstKTokens[i], count+1);
		}
	}

	private static void loadCLTokensPrev(String[] lastKTokens,
			List<Map<String, Integer>> list, int clWindow) {
		int min = clWindow<lastKTokens.length?clWindow:lastKTokens.length;
		int last = lastKTokens.length;
		for(int i=0,j=last-1; i<min;i++,j--){
			if(i>=list.size()){
				list.add(new HashMap<String,Integer>());
			}
			Map<String,Integer> map = list.get(i);
			if(map.get(lastKTokens[j])==null){
				map.put(lastKTokens[j], 0);
			}
			int count = map.get(lastKTokens[j]);
			map.put(lastKTokens[j], count+1);
		}
		
	}
	
	private static void loadTokens(String[] tokens, SenseModel model) {
		Map<String,Integer> map = model.getCoMap();
		for(String token:tokens){
			if(map.get(token)==null){
				map.put(token, 0);
			}
			int count = map.get(token);
			map.put(token, count+1);
		}
	}

	private static String[] getFirstKTokens(String[] nextTokens, int coWindow) {
		String[] tokens = new String[coWindow];
		for(int i=0; i<coWindow; i++){
			tokens[i]="-";
		}
		int length = nextTokens.length;
		int min = length<coWindow? length:coWindow;
		
		for(int i=0;i<min;i++){
			tokens[i]=nextTokens[i];
		}
		return tokens;
	}

	private static String[] getLastKTokens(String[] prevTokens, int coWindow) {
		String[] tokens = new String[coWindow];
		for(int i=0; i<coWindow; i++){
			tokens[i]="-";
		}
		int last = prevTokens.length;
		int min = last<coWindow? last:coWindow;
		
		for(int i=0,j=last-1;i<min;i++,j--){
			tokens[i]=prevTokens[j];
		}
		return tokens;
	}

	private static String process(String token) {
		if(token==null){
			token="";
		}
		token = token.replaceAll("\\s's", "s");
		token = token.replaceAll("[()]", " ");
		token = token.replaceAll("((\\d+[.,])+)?\\d+", " number "); 
		token = token.replaceAll("&", "and");
		token = token.replaceAll("%", "percent");
		
		//processing apostrophes
		token = token.replaceAll("\\s+n't", "not ");
		token = token.replaceAll("n't", " not ");
		token = token.replaceAll("'ve", " have ");
		token = token.replaceAll("'m", "");
		token = token.replaceAll("'s", "");
		token = token.replaceAll("'d", "");
		token = token.replaceAll("'", "");
		token = token.replaceAll("(`)+", "");
		
		//processing periods
		token = token.replaceAll("\\.([a-zA-Z\\d])", "$1");  //P.hd will become Phd
		
		//processing other punctuation marks
		token = token.replaceAll("-", " ");
		token = token.replaceAll("\\.([a-zA-Z])", "$1"); 

		token = token.replaceAll("([!:;?\",.])(\\s+)([!:;?\",.])", " $1$3 "); 
		token = token.replaceAll("([!:;?\",.])+", " $1 "); 
		token = token.replaceAll("[!:;?\",.]", "punctuation"); 
		token = token.replaceAll("\\s+", " "); 
		return token;
	}

	public static Map<String,Word> parse(String filePath, IFileSystem fs) throws IOException {
		Map<String,Word> wordMap = new HashMap<String,Word>();
		BufferedReader bufferedReader = getReader(filePath,fs);
		String line = null;
		while((line=bufferedReader.readLine())!=null){
			String[] headTokens = line.split("\\|");
			
			String currWord = headTokens[0].trim().toLowerCase();
			
			if(wordMap.get(currWord)==null){
				wordMap.put(currWord, new Word());
			}
			
			Word word = wordMap.get(currWord);
			word.setWord(currWord);
			Sense sense = new Sense();
			sense.setSenseId(Integer.parseInt(headTokens[1].trim()));
			String[] subTokens = headTokens[2].split("%%");
			sense.setPrevContext(subTokens[0].trim().toLowerCase());
			sense.setTarget(subTokens[1].trim().toLowerCase());
			
			if(subTokens.length==3){
				sense.setNextContext(subTokens[2].trim().toLowerCase());
			}
			word.getSenseList().add(sense);
			word.incrementTotalOccurances();

		
		}
		bufferedReader.close();
		return wordMap;
	}

	private static BufferedReader getReader(String file, IFileSystem fs)
			throws IOException {
		
		InputStream fin = (InputStream) fs.readFile(file);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fin));
		
		return bufferedReader;
	}
	
	public static String removeStopWordsAndStem(String input) throws IOException {
	    
	    StringReader inputMod = new StringReader(input);
		final StandardTokenizer src = new StandardTokenizer(matchVersion, inputMod);
		TokenStream tok = new StandardFilter(matchVersion, src);
		tok = new LowerCaseFilter(matchVersion, tok);

		// Add additional filters here 
		tok = new StopFilter(matchVersion, tok, STOPWORDS);
		tok = new PorterStemFilter(tok);
		
		StringBuilder sb = new StringBuilder();
		CharTermAttribute charTermAttribute = tok.addAttribute(CharTermAttribute.class);

		try {
			tok.reset();
            while(tok.incrementToken()) {

            	 if (sb.length() > 0) {
     	            sb.append(" ");
     	        }
            	 if(!"number".equals(charTermAttribute.toString())){
            		 sb.append(charTermAttribute.toString());
            	 }
            }
            tok.close();
        }
        catch(IOException e) {
            e.printStackTrace();
            throw new IOException();
        }
		return sb.toString();
	}
}
