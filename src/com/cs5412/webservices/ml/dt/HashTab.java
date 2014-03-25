package com.cs5412.webservices.ml.dt;

import java.util.*;

public class HashTab implements Comparator<HashTab>{
	private int positionnumber;
	private double infogain;

	public void Setpositionnumber(int x)
	{
		positionnumber=x;
	}
	
	public void Setinfogain(double x)
	{
		infogain=x;
	}
	
	public int Getpositionnumber()
	{
		return positionnumber;
	}
	
	public double Getinfogain()
	{
		return infogain;
	}
	
	@Override
	public int compare(HashTab h1, HashTab h2) {
		if (h1.Getinfogain() < h2.Getinfogain() ){
		   return 1;
		}
		else if (h1.Getinfogain() > h2.Getinfogain()){
		   return -1;
		}
		else
		   return 0;
	}
}
