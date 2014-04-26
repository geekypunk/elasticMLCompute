package com.cs5412.webservices.ml.wsd.domain;

public class Sense {
	int senseId;
	int predictedSenseId;
	
	String prevContext;
	String target;
	String nextContext;
	
	public Sense(){}

	public int getSenseId() {
		return senseId;
	}
	public void setSenseId(int senseId) {
		this.senseId = senseId;
	}
	public String getPrevContext() {
		return prevContext;
	}
	public void setPrevContext(String prevContext) {
		this.prevContext = prevContext;
	}
	public String getNextContext() {
		return nextContext;
	}
	public void setNextContext(String nextContext) {
		this.nextContext = nextContext;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}

	public int getPredictedSenseId() {
		return predictedSenseId;
	}

	public void setPredictedSenseId(int predictedSenseId) {
		this.predictedSenseId = predictedSenseId;
	}
}
