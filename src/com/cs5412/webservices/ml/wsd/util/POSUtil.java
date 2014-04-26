package com.cs5412.webservices.ml.wsd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.InvalidFormatException;

public class POSUtil {
	static POSTaggerME tagger;
	
	public static String[] getPOSTags(String[] operand) throws InvalidFormatException, IOException{
		
		String tags[] = tagger.tag(operand);
		return tags;
	}
	
	static{
		InputStream modelIn = null;
		POSModel model= null;
		try {
		  //modelIn = new FileInputStream("data"+File.separator+"en-pos-maxent.bin");
		  modelIn =POSUtil.class.getResourceAsStream("en-pos-maxent.bin");
		  model = new POSModel(modelIn);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
		
		 tagger = new POSTaggerME(model);	 
	}

}
