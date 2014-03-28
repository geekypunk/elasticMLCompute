package com.cs5412.taskmanager;

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
