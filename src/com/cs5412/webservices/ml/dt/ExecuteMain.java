package com.cs5412.webservices.ml.dt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.utils.ServerConstants;

public class ExecuteMain {

	public void Construct(String trainFile, String testFile, String outputfile, IFileSystem fs, ArrayList<Double> accuracy){
		
		Queue<DecisionTreeNode> queue = new LinkedList<DecisionTreeNode>();
		ModelChooser model = new ModelChooser();
		ArrayList<TrainingNode> trainingNodes = new ArrayList<TrainingNode>();
		model.setwords(trainingNodes, trainFile, fs);
		
		DecisionTreeNode dTree = new DecisionTreeNode();
		int[] tempwordexists = new int[2000];
		for (int y=0;y<2000;y++){
			tempwordexists[y]=0;
		}
		dTree.SetusedWords(tempwordexists);
		dTree.setArr_TrainNode(trainingNodes);
		dTree.setEntrpy();
		dTree.setLevelNode(0);
		dTree.parentLevel=-1;
		queue.add(dTree);
		model.constructDecisionTree(queue);
		int [] choplist = {2,3,5,10,50,80};
		Double TempAccuracy=0.0;
		ArrayList<TrainingNode> TestingNodes = new ArrayList<TrainingNode>();
		model.setwords(TestingNodes, testFile, fs);
		int number=400;
		try{
			BufferedWriter bw = (BufferedWriter) fs.createFileToWrite(outputfile, true);
			
			for(int chop: choplist)
			{
				TempAccuracy=1.0-model.earlystopping(TestingNodes,dTree,number,chop);
				accuracy.add(TempAccuracy);
				bw.write(""+TempAccuracy);
				bw.newLine();
			}
			bw.close();
		}
		catch(Exception e)
		{
			System.out.println("Contruct: "+e);
		}
	}
	
public void FinalAccuracy(String trainFile, String testFile, int height, IFileSystem fs, String crossvalidation){
		
		Queue<DecisionTreeNode> queue = new LinkedList<DecisionTreeNode>();
		ModelChooser model = new ModelChooser();
		ArrayList<TrainingNode> trainingNodes = new ArrayList<TrainingNode>();
		model.setwords(trainingNodes, trainFile, fs);

		DecisionTreeNode dTree = new DecisionTreeNode();
		int[] tempwordexists = new int[2000];
		for (int y=0;y<2000;y++){
			tempwordexists[y]=0;
		}
		dTree.SetusedWords(tempwordexists);
		dTree.setArr_TrainNode(trainingNodes);
		dTree.setEntrpy();
		dTree.setLevelNode(0);
		dTree.parentLevel=-1;
		queue.add(dTree);
		model.constructDecisionTree(queue);
		Double TempAccuracy=0.0;
		ArrayList<TrainingNode> TestingNodes = new ArrayList<TrainingNode>();
		model.setwords(TestingNodes, testFile, fs);
		int number=2000;
		try{
				String out = crossvalidation + ServerConstants.linuxSeparator + "FinalOutput.txt";
				BufferedWriter bw = (BufferedWriter) fs.createFileToWrite(out,true);
				TempAccuracy=1.0-model.earlystopping(TestingNodes,dTree,number,height);
				bw.write(""+TempAccuracy);
				bw.newLine();
				bw.close();
		}
		catch(Exception e)
		{
			System.out.println("Contruct: "+e);
		}
	}
}
