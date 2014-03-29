package com.cs5412.webservices.ml.dt;

import java.io.*;
import java.util.ArrayList;

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.utils.Utils;

public class BestHeight {
	private int CrossValidations;
	private Double[] Accuracy;
	private int numHeights;
	private int[] heights;
	
	public BestHeight()
	{
		CrossValidations = 5;
		numHeights = 6;
		int[] temp = {1,2,5,10,50,80};
		heights = temp;
		Accuracy = new Double[numHeights];
		for(int i=0;i<numHeights;i++)
		{
			Accuracy[i]=0.0;
		}
	}
	
	public BestHeight(int num, int height, int[] labelHeights)
	{
		CrossValidations = num;
		numHeights = height;
		this.heights = labelHeights;
		Accuracy = new Double[numHeights];
		for(int i=0;i<numHeights;i++)
		{
			Accuracy[i]=0.0;
		}
	}
	
	public int calculateAccuracy(String crossvalidation, IFileSystem fs, ArrayList<Double> avgAcc)
	{
		BufferedReader[] br = new BufferedReader[CrossValidations];
		BufferedWriter bw = null;
		String OutputFileName = crossvalidation + Utils.linuxSeparator + "output.txt";
		try{
			for(int i =0;i<CrossValidations;i++)
			{
				// read each temporary output file
				String FileName = crossvalidation + Utils.linuxSeparator + "output"+(i+1)+".txt";
				InputStream fin = (InputStream) fs.readFile(FileName);
				br[i] = new BufferedReader(new InputStreamReader(fin));
				String readline = br[i].readLine();
				int line=0;
				while(readline!=null)
				{
					Accuracy[line++] += Double.parseDouble(readline);
					readline=br[i].readLine();
				}
				br[i].close();
			}
			bw = (BufferedWriter) fs.createFileToWrite(OutputFileName, true);
			Double max=0.0;
			Double temp=0.0;
			int indexHeight=0;
			for(int i=0;i<numHeights;i++)
			{
				// calculate accuracy
				temp = Accuracy[i]/CrossValidations;
				if(max<temp)
				{
					max=temp;
					indexHeight=i;
				}
				avgAcc.add(temp);
				bw.write("The accuracy with height of tree "+heights[i]+" is: "+temp);
				bw.newLine();
			}
			// The max
			bw.write("The best accuracy is seen with height "+heights[indexHeight]+" : "+max);
			return heights[indexHeight];
		}
		catch(Exception e)
		{
			// Exception
			System.out.println("calculateAccuracy: "+e);
		}
		finally{
			try{
				// close every time
				bw.close();
			}
			catch(Exception e1)
			{
				System.out.println("calculateAccuracy: finally: "+e1.getStackTrace());
			}

		}
		return 0;
	}
	
// Test only
/*	public static void main(String[] args)
	{
		BestHeight BH = new BestHeight();
		BH.calculateAccuracy();
	}*/
}
