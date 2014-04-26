package com.cs5412.webservices.ml.wsd.domain;

public class TestWord {
	String wordText;
	Sense sense;
	SenseModel senseModel;
	
	public TestWord() {
		senseModel = new SenseModel();
	}

	public Sense getSense() {
		return sense;
	}

	public void setSense(Sense sense) {
		this.sense = sense;
	}

	public SenseModel getSenseModel() {
		return senseModel;
	}

	public void setSenseModel(SenseModel senseModel) {
		this.senseModel = senseModel;
	}

	public String getWordText() {
		return wordText;
	}

	public void setWordText(String wordText) {
		this.wordText = wordText;
	}
}
