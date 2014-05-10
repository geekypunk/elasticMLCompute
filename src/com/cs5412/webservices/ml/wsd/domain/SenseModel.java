package com.cs5412.webservices.ml.wsd.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that holds model for SenseModel
 * @author pbp36
 *
 */
public class SenseModel {
	
	int senseId;
	String POS;
	int totalOccurances; //totalOccurancesWithThisSense
	
	//Co-occurrence features
	Map<String,Integer> coMap;
	
	//Collocational features
	List<Map<String,Integer>> prevList;
	List<Map<String,Integer>> forwardList;
	List<Map<String,Integer>> specialCLList;
	
	//POS features
	List<Map<String,Integer>> prevPOSList;
	List<Map<String,Integer>> forwardPOSList;
	List<Map<String,Integer>> specialPOSCLList;
	
	public SenseModel(){
		coMap = new HashMap<String,Integer>();
		prevList = new ArrayList<Map<String,Integer>>();
		forwardList = new ArrayList<Map<String,Integer>>();
		specialCLList = new ArrayList<Map<String,Integer>>();
		
		prevPOSList = new ArrayList<Map<String,Integer>>();
		forwardPOSList = new ArrayList<Map<String,Integer>>();
		specialPOSCLList = new ArrayList<Map<String,Integer>>();
	}

	public int getSenseId() {
		return senseId;
	}

	public void setSenseId(int senseId) {
		this.senseId = senseId;
	}

	public String getPOS() {
		return POS;
	}

	public void setPOS(String pOS) {
		POS = pOS;
	}

	public int getTotalOccurances() {
		return totalOccurances;
	}

	public void setTotalOccurances(int totalOccurances) {
		this.totalOccurances = totalOccurances;
	}

	public Map<String, Integer> getCoMap() {
		return coMap;
	}

	public void setCoMap(Map<String, Integer> coMap) {
		this.coMap = coMap;
	}

	public List<Map<String, Integer>> getPrevList() {
		return prevList;
	}

	public void setPrevList(List<Map<String, Integer>> prevList) {
		this.prevList = prevList;
	}

	public List<Map<String, Integer>> getForwardList() {
		return forwardList;
	}

	public void setForwardList(List<Map<String, Integer>> forwardList) {
		this.forwardList = forwardList;
	}

	public List<Map<String, Integer>> getSpecialCLList() {
		return specialCLList;
	}

	public void setSpecialCLList(List<Map<String, Integer>> specialCLList) {
		this.specialCLList = specialCLList;
	}
	
	public void incrementOccurances(){
		totalOccurances++;
	}

	public List<Map<String, Integer>> getPrevPOSList() {
		return prevPOSList;
	}

	public void setPrevPOSList(List<Map<String, Integer>> prevPOSList) {
		this.prevPOSList = prevPOSList;
	}

	public List<Map<String, Integer>> getForwardPOSList() {
		return forwardPOSList;
	}

	public void setForwardPOSList(List<Map<String, Integer>> forwardPOSList) {
		this.forwardPOSList = forwardPOSList;
	}

	public List<Map<String, Integer>> getSpecialPOSCLList() {
		return specialPOSCLList;
	}

	public void setSpecialPOSCLList(List<Map<String, Integer>> specialPOSCLList) {
		this.specialPOSCLList = specialPOSCLList;
	}
}
