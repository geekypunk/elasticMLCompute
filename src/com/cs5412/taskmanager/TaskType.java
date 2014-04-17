package com.cs5412.taskmanager;

/**
 * @author kt466
 *
 */
public enum TaskType {

	DATASET_UPLOAD {
	    public String toString() {
	        return "Dataset Upload";
	    }
	},
	ALGORITHM_EXEC {
	    public String toString() {
	        return "Algorithm Execution";
	    }
	},

}
