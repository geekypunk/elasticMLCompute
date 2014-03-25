package com.cs5412.webservices.ml.dt;

import java.util.*;

public class DecisionTreeNode {
	private ArrayList<TrainingNode> trNodeArray;
	public DecisionTreeNode firstChild;
	public DecisionTreeNode secondChild;
	public int classleaf;
	private int levelNode;
	public int parentLevel;
	private int[] usedWords;
	public int majorityClass;
	
	private double entropyval;
	public int splitFeature;
	
	public DecisionTreeNode()
	{
		firstChild = null;
		secondChild = null;
		classleaf = -1;
		majorityClass = -1;
	}
	
	public void SetusedWords(int[] a)
	{
		usedWords = a;
	}
	
	public int[] GetusedWords()
	{
		return usedWords;
	}
	
	public void setLevelNode(int n)
	{
		levelNode = n;
	}
	
	public int getLevelNode()
	{
		return levelNode;
	}
	
	public void setArr_TrainNode(ArrayList<TrainingNode> tNode) {
		trNodeArray = tNode;
	}
	
	public ArrayList<TrainingNode> getArr_TrainNode() {
		return trNodeArray;
	}
	
	public void setEntrpy()
	{
		int[] classnames= new int[4];
		int total=0;
		entropyval=0.0;
		for(TrainingNode tn : trNodeArray){
			classnames[tn.getClassNum()]++;
			total++;
		}
	//	System.out.print("total="+total+" ");
	/*	System.out.println("1:"+classnames[0]+" "+classnames[3]+" "+total);
		System.out.println(((double)classnames[0]/(double)total)*(Math.log((double)classnames[0]/(double)total))/Math.log(2.0));*/
		for(int i=0;i<4;i++){
			if (classnames[i]!=0){
			entropyval = entropyval - ((double)classnames[i]/(double)total)*((Math.log((double)classnames[i]/(double)total))/Math.log(2.0));
			}
		}
	}
	
	public double getEntropy()
	{
		return entropyval;
	}
	
	public double findEntropy(int[] classes, int totalinClass)
	{
		double ent=0.0;
		for(int i=0;i<4;i++){
			if (classes[i]!=0){
			ent -= ((double)classes[i]/(double)totalinClass)*((Math.log(((double)classes[i]/(double)totalinClass)))/Math.log(2.0));
			}
		}
		return ent;
	}
	
	public int[] splittingArray(int zindex)
	{
		int[] res = new int[2000];
		int k=0;
		for(int d=0;d<2000;d++)
		{
			res[d]=-1;
		}
		int index_of_document=0;
		for(TrainingNode tn:trNodeArray)
		{
			int [] tempwords = new int[2000];
			tempwords = tn.getArray();
			if(tempwords[zindex]>0){
				res[k]=index_of_document;
				k=k+1;
			}
			index_of_document++;
		}
		return res;
	}
	
	public double CalcInfoGain(int[] classPresent1, int[] classAbsent1, int total)
	{
		double result= 0.0;
		int sumPresent=0;
		int sumAbsent=0;
		double part1=0.0;
		double part2=0.0;
		for(int i=0;i<4;i++)
		{
			sumPresent+=classPresent1[i];
			sumAbsent+=classAbsent1[i];
		}
		if(sumPresent!=0){
/*			for(int i=0;i<2;i++){*/
				part1 +=findEntropy(classPresent1, sumPresent);
/*			}*/
			part1*=((double)sumPresent/(double)total);
		}
		else{
			part1=0.0;
		}
		if(sumAbsent!=0){
/*			for(int i=0;i<2;i++){*/
				part2+=findEntropy(classAbsent1, sumAbsent);
/*			}*/
			part2*=((double)sumAbsent/(double)total);
		}
		else{
			part2=0.0;
		}
		result =  getEntropy() - part1 - part2;
		return result;
	}
	
	public int IdentifySplit()
	{
		int [] classPresent = new int[4];
		int [] classAbsent = new int[4];
		ArrayList<HashTab> hasharray= new ArrayList<HashTab>();
		for(int i=0;i<2000;i++ ){
			if(usedWords[i]==1)
			{
				continue;
			}
			HashTab hashsync = new HashTab();
			int total=0;
			for(int j=0;j<4;j++){
				classPresent[j]=0;
				classAbsent[j]=0;
			}
			for(TrainingNode tn : trNodeArray){
				int[] tempwords= tn.getArray();
				total++;
				if(tempwords[i]>0){
					classPresent[tn.getClassNum()]++;
				}
				else{
					classAbsent[tn.getClassNum()]++;
				}
			}
			double infoGain = CalcInfoGain(classPresent, classAbsent, total);
			hashsync.Setpositionnumber(i);
			hashsync.Setinfogain(infoGain);
			hasharray.add(hashsync);
			
						
			}
		Collections.sort(hasharray,new HashTab());
/*		for(HashTab hashy: hasharray){
			System.out.print("-->"+hashy.Getpositionnumber()+" "+hashy.Getinfogain());
		}
		System.out.println("");*/
		splitFeature = hasharray.get(0).Getpositionnumber();
		usedWords[splitFeature]=1;
		return splitFeature;
	}
}
