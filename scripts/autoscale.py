#!/usr/bin/python

#Pyhton script which facilitates 

import psutil
import threading
import time

MEMORY_THRESHOLD=100
class autoScale(threading.Thread):
	def __init__(self, statInterval, sleepInterval):
		threading.Thread.__init__(self)
		self.statInterval = statInterval
		self.sleepInterval = sleepInterval
	def run(self):
		while True:
			times =  psutil.cpu_times_percent(interval=self.statInterval , percpu=False)
			print times
			idleTime =  times[3]
			print idleTime
			mem = psutil.virtual_memory()
			print mem.available
			time.sleep(self.sleepInterval)

autoScaleDaemon = autoScale(2, 2)
autoScaleDaemon.start()