package com.cs5412.webservices.ml.knn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Metric {
	static final Logger LOG = LoggerFactory.getLogger(Metric.class);
	double accuracy;
	
	public Metric(){}
	
	public Metric( double accuracy) {
		super();
		this.accuracy = accuracy;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public void display() {
		LOG.info("\nAccuracy:"+accuracy);
	}
}