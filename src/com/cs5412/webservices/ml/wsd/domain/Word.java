package com.cs5412.webservices.ml.wsd.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that holds Training Word instance
 * @author pbp36
 *
 */
public class Word {
	
	String word;
	int totalOccurances;

	Map<Integer,SenseModel> senseMap;
	Map<Integer,Double> priorProbabilityMap; //Initially: senseId -> Count. 
											 //After post processing senseId -> prior probability	
	List<Sense> senseList;
	public Word(){
		senseMap = new HashMap<Integer,SenseModel>();
		priorProbabilityMap = new HashMap<Integer,Double>();
		senseList = new ArrayList<Sense>();
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getTotalOccurances() {
		return totalOccurances;
	}

	public void setTotalOccurances(int totalOccurances) {
		this.totalOccurances = totalOccurances;
	}

	public Map<Integer, SenseModel> getSenseMap() {
		return senseMap;
	}

	public void setSenseMap(Map<Integer, SenseModel> senseMap) {
		this.senseMap = senseMap;
	}

	public Map<Integer, Double> getPriorProbabilityMap() {
		return priorProbabilityMap;
	}

	public void setPriorProbabilityMap(Map<Integer, Double> priorProbabilityMap) {
		this.priorProbabilityMap = priorProbabilityMap;
	}

	public List<Sense> getSenseList() {
		return senseList;
	}

	public void setSenseList(List<Sense> senseList) {
		this.senseList = senseList;
	}
	
	public void incrementTotalOccurances(){
		totalOccurances+=1;
	}
}
