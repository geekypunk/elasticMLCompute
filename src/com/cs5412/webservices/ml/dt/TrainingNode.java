package com.cs5412.webservices.ml.dt;


/**
 * this defines the classes
 * @author rb723
 *
 */
public class TrainingNode{
	private int trainClassNum;
	private int[] words;

	public TrainingNode()
	{
		words = new int[2000];
		for(int i=0;i<2000;i++) {
			words[i] = 0;
		}
	}
			
	public void setClassNum(int cNum) {
		if (cNum==4){
			/* Treat trainClassnum as 3 wherever it is 4*/
			trainClassNum = cNum-1;
		}
		else
			trainClassNum = cNum;
	}
	
	public int getClassNum() {
		return trainClassNum;
	}
	
	public void setArray(int index, int value)
	{
		words[index]=value;
	}
	
	public int[] getArray()
	{
		return words;
	}
}
