package com.cs5412.autoscaling;

public enum SRVSTATUS {
	UP {
		public String toString() {
			return "UP";
		}

	},
	DOWN {
		public String toString() {
			return "DOWN";
		}

	},
	MAINT {
		public String toString() {
			return "MAINT";
		}

	},
}