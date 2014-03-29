package com.cs5412.webservices.ml.dt;

import java.io.*;

import com.cs5412.filesystem.IFileSystem;
import com.cs5412.utils.Utils;
import com.cs5412.webservices.fileupload.FileUploadServlet;

import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossValidation {
	static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);
	public void createCrossValidationFiles(String FileName, IFileSystem fs, String crossvalidation)
	{
		BufferedReader br = null;
		ArrayList<String> allLines = new ArrayList<String>();
		BufferedWriter[] bwt = new BufferedWriter[5];
		BufferedWriter[] bwv = new BufferedWriter[5];
		try{
			InputStream fin = (InputStream) fs.readFile(FileName);
			br = new BufferedReader(new InputStreamReader(fin));
			for(int i=0;i<5;i++)
			{
				String outFile = crossvalidation + Utils.linuxSeparator + "trainFile"+(i+1)+".txt";
				bwt[i] = (BufferedWriter) fs.createFileToWrite(outFile, true);
			}
			for(int i=0;i<5;i++)
			{
				String outFile = crossvalidation + Utils.linuxSeparator + "validFile"+(i+1)+".txt";
				bwv[i] = (BufferedWriter) fs.createFileToWrite(outFile, true);
			}
			String ReadLine = br.readLine();

			while(ReadLine!=null)
			{
				allLines.add(ReadLine);
				ReadLine = br.readLine();
			}
			br.close();	
			Collections.shuffle(allLines);
			int line_number=0;
			for(String eachLine: allLines)
			{
				if(line_number < 400)
				{
					for(int k=0;k<5;k++)
					{
						if(k!=0)
						{
							bwt[k].write(eachLine);
							bwt[k].newLine();
						}
						else
						{
							bwv[k].write(eachLine);
							bwv[k].newLine();
						}
					}
				}
				else if(line_number < 800)
				{
					for(int k=0;k<5;k++)
					{
						if(k!=1)
						{
							bwt[k].write(eachLine);
							bwt[k].newLine();
						}
						else
						{
							bwv[k].write(eachLine);
							bwv[k].newLine();
						}
					}
				}
				else if(line_number < 1200)
				{
					for(int k=0;k<5;k++)
					{
						if(k!=2)
						{
							bwt[k].write(eachLine);
							bwt[k].newLine();
						}
						else
						{
							bwv[k].write(eachLine);
							bwv[k].newLine();
						}
					}
				}
				else if(line_number < 1600)
				{
					for(int k=0;k<5;k++)
					{
						if(k!=3)
						{
							bwt[k].write(eachLine);
							bwt[k].newLine();
						}
						else
						{
							bwv[k].write(eachLine);
							bwv[k].newLine();
						}
					}
				}
				else
				{
					for(int k=0;k<5;k++)
					{
						if(k!=4)
						{
							bwt[k].write(eachLine);
							bwt[k].newLine();
						}
						else
						{
							bwv[k].write(eachLine);
							bwv[k].newLine();
						}
					}
				}
				line_number++;
			}
				
				

			
			for(int i=0;i<5;i++)
			{
				bwv[i].close();
				bwt[i].close();
			}

		}
		catch(Exception e)
		{
			System.out.println("createCrossValidationFiles: "+e);
		}
	}
	
// Test only
/*	public static void main(String[] args)
	{
		String file = "groups.train";
		CrossValidation CV = new CrossValidation();
		CV.createCrossValidationFiles(file);
		
	}*/
}
