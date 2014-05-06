package com.cs5412.webservices.ml.wsd;

/**
 * Class that holds results for parameter setting
 * @author pbp36
 *
 */
public class WSDConfig implements Comparable<WSDConfig>{
	
	int CO_WINDOW;
	int CL_WINDOW;
	double score;
	
	public WSDConfig(int cO_WINDOW, int cL_WINDOW, double score) {
		super();
		CO_WINDOW = cO_WINDOW;
		CL_WINDOW = cL_WINDOW;
		this.score = score;
	}
	
	public int getCO_WINDOW() {
		return CO_WINDOW;
	}
	public void setCO_WINDOW(int cO_WINDOW) {
		CO_WINDOW = cO_WINDOW;
	}
	public int getCL_WINDOW() {
		return CL_WINDOW;
	}
	public void setCL_WINDOW(int cL_WINDOW) {
		CL_WINDOW = cL_WINDOW;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public int compareTo(WSDConfig arg0) {
		int scoreComp =  new Double(arg0.getScore()).compareTo(score);
		
		if(scoreComp==0){
			int coWindowComp = CO_WINDOW - arg0.getCO_WINDOW();
			if(coWindowComp==0){
				return CL_WINDOW - arg0.CL_WINDOW;
			}else{
				return coWindowComp;
			}
		}else{
			return scoreComp;
		}
	}
}