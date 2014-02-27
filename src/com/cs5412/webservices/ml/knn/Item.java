package com.cs5412.webservices.ml.knn;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Item implements Comparable<Item>{
	static final Logger LOG = LoggerFactory.getLogger(Item.class);
	int itemId;
	int classId;
	int predictedClass;
	double l2norm;
	double cosineSimilarity;
	
	Map<Integer, Double> featureValueMap;
	
	public Item(){
		featureValueMap = new HashMap<Integer,Double>();
	}
	
	@Override
	public int compareTo(Item item) {
		
		return new Double(item.getCosineSimilarity()).compareTo(this.getCosineSimilarity());
	}
	
	public void normalize(){
		Map<Integer, Double> features = getFeatureValueMap();
		
		for(Integer feature: features.keySet()){
			double featureValue = features.get(feature);
			features.put(feature, featureValue/getL2norm());
		}
	}
	
	public void computeL2Norm() {
		
		Map<Integer, Double> features = getFeatureValueMap();
		double sum = 0;
		for(Integer feature: features.keySet()){
			double featureValue = features.get(feature);
			sum=sum+featureValue*featureValue;
		}
		setL2norm(Math.sqrt(sum));
	}
	
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public double getL2norm() {
		return l2norm;
	}
	public void setL2norm(double l2norm) {
		this.l2norm = l2norm;
	}
	public double getCosineSimilarity() {
		return cosineSimilarity;
	}
	public void setCosineSimilarity(double cosineSimilarity) {
		this.cosineSimilarity = cosineSimilarity;
	}
	public Map<Integer, Double> getFeatureValueMap() {
		return featureValueMap;
	}
	public void setFeatureValueMap(Map<Integer, Double> featureValueMap) {
		this.featureValueMap = featureValueMap;
	}
	public int getPredictedClass() {
		return predictedClass;
	}
	public void setPredictedClass(int predictedClass) {
		this.predictedClass = predictedClass;
	}
	public int getClassId() {
		return classId;
	}
	public void setClassId(int classId) {
		this.classId = classId;
	}
}
