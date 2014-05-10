package com.cs5412.taskmanager;

/**
 * <p><b>Enum for task type</b></p>
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
