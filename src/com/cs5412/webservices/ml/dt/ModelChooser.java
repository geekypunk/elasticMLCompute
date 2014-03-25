package com.cs5412.webservices.ml.dt;

import java.util.*;
import java.io.*;

import com.cs5412.filesystem.IFileSystem;

public class ModelChooser {
	
	private String[] LookUp = new String[2000];
	
	public void constructDecisionTree(Queue<DecisionTreeNode> que)
	{
		while (!que.isEmpty()){
			DecisionTreeNode dT = new DecisionTreeNode();
			dT = que.remove();
			int tmpindx = dT.IdentifySplit();
			ArrayList<TrainingNode> trainingleftNodes = new ArrayList<TrainingNode>();
			ArrayList<TrainingNode> trainingrightNodes = new ArrayList<TrainingNode>();
			ArrayList<TrainingNode> trainingNodes = dT.getArr_TrainNode();
			DecisionTreeNode dT1 = new DecisionTreeNode();
			DecisionTreeNode dT2 = new DecisionTreeNode();

			int[] allSet = new int[2000];
			for(TrainingNode tn: trainingNodes)
			{
				allSet = tn.getArray();
				if(allSet[tmpindx]>0)
				{
					trainingleftNodes.add(tn);
				}
				else
				{
					trainingrightNodes.add(tn);
				}
			}
			int [] class_Left = new int[4];
			int [] class_Right = new int[4];
			int left_total=0;
			int right_total=0;
			for(TrainingNode tn: trainingleftNodes){
				class_Left[tn.getClassNum()]++;
				left_total++;
			}
			//	System.out.print(left_total+","+class_Left[0]+","+class_Left[1]+","+class_Left[2]+","+class_Left[3]+" ");

			for(TrainingNode tn: trainingrightNodes){
				class_Right[tn.getClassNum()]++;
				right_total++;
			}
			//	System.out.print(right_total+","+class_Right[0]+","+class_Right[1]+","+class_Right[2]+","+class_Right[3]+" ");
			for(int m=0;m<4;m++)
			{
				if(class_Left[m]==left_total)
				{
					dT1.classleaf=m;
				}
			}
			for(int m=0;m<4;m++)
			{
				if(class_Right[m]==right_total)
				{
					dT2.classleaf=m;
				}
			}
			dT1.setArr_TrainNode(trainingleftNodes);
			dT2.setArr_TrainNode(trainingrightNodes);			
			int[] tempwordsUsed1 = new int[2000];
			for(int i=0; i<2000;i++) {
				tempwordsUsed1[i] = dT.GetusedWords()[i];
			}
			int[] tempwordsUsed2 = new int[2000];
			for(int i=0; i<2000;i++) {
				tempwordsUsed2[i] = dT.GetusedWords()[i];
			}

			dT1.SetusedWords(tempwordsUsed1);
			dT2.SetusedWords(tempwordsUsed2);
			/*	System.out.print("Used Words= ");
			for(int e=0;e<2000;e++){
				if(tempwordsUsed1[e]>0)
					System.out.print(e+", ");
			}
			System.out.println("");*/

			dT1.setEntrpy();
			dT2.setEntrpy();
			dT1.setLevelNode(dT.getLevelNode()+1);
			dT2.setLevelNode(dT.getLevelNode()+1);
			int max=0;
			int clas=-1;
			int gotL = dT1.getLevelNode();
			if(gotL==10 || gotL==2 || gotL ==3 || gotL ==5 || gotL == 50 || gotL ==80)
			{
				for(int p=0;p<4;p++)
				{
					if(max < class_Left[p]){
						max=class_Left[p];
						clas=p;
					}
				}
			}
			dT1.majorityClass=clas;
			gotL = dT2.getLevelNode();
			if(gotL==10 || gotL==2 || gotL ==3 || gotL ==5 || gotL == 50 || gotL ==80)
			{
				max=0;
				for(int p=0;p<4;p++)
				{
					if(max < class_Right[p]){
						max=class_Right[p];
						clas=p;
					}
				}
			}
			dT2.majorityClass=clas;
			dT1.parentLevel=dT.getLevelNode();
			dT2.parentLevel=dT.getLevelNode();
			dT.firstChild=dT1;
			dT.secondChild=dT2;
			if(dT1.classleaf==-1){
				que.add(dT1);
			}
			if(dT2.classleaf==-1){
				que.add(dT2);
			}
			//System.out.println("");			
		}

	}
	
	public int Height(DecisionTreeNode d)
	{
		if(d.classleaf==-1)
			return 0;
		return (1+Math.max(Height(d.firstChild),Height(d.secondChild)));
	}
	
	public void setwords(ArrayList<TrainingNode> trData, String fileName, IFileSystem fs) {
		try{
			InputStream fin = (InputStream) fs.readFile(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin));
			String readline = br.readLine();
			while(readline != null) {
				String[] record = readline.split(" ");
				TrainingNode tn = new TrainingNode();
				int flag=0;
				for(String index: record)
				{
					if(flag==0){
						if(Integer.parseInt(record[0])==4)
							tn.setClassNum(3);
						else
							tn.setClassNum(Integer.parseInt(record[0]));
						flag=1;
						continue;
					}
					String[] indx_val = index.split(":");
					tn.setArray(Integer.parseInt(indx_val[0]),Integer.parseInt(indx_val[1]));	
				}
				trData.add(tn);
				readline=br.readLine();
			}
			br.close();
		}
		catch(Exception e) {
			System.out.print("Exception Found while setting Training Nodes." + e);
		}

	}
	
	public double earlystopping(ArrayList<TrainingNode> trData, DecisionTreeNode root, int number, int earlystop)
	{
		int [] temparraystore = new int[2000];
		int Totalcount= number;
		int successCount=0;
		DecisionTreeNode tree;
		int i=0;
		for(TrainingNode tn: trData)
		{
			temparraystore = tn.getArray();
			tree=root;
			while(tree.classleaf==-1 && tree.getLevelNode() < earlystop){
				/*				System.out.println("Node_number="+tree.splitFeature);*/
				if(temparraystore[tree.splitFeature] > 0)
				{
					tree=tree.firstChild;
					/*					System.out.println("Child="+tree.splitFeature);*/
				}
				else
				{
					tree=tree.secondChild;
					/*					System.out.println("Child="+tree.splitFeature);*/
				}
			}
			if(tree.classleaf == tn.getClassNum())
			{
				successCount++;
			}
			else if(tree.majorityClass == tn.getClassNum())
			{
				successCount++;
			}
			i++;
		}
		return (1.0-((double)successCount/(double)Totalcount));

	}
}
