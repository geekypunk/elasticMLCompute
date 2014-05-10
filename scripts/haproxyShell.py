#!/usr/bin/python

import subprocess
from pprint import pprint

# response time threshold in milliseconds: when backend starts responding 
# slower than the threshold we scale up, otherwise scale down.
THRESHOLD = 500

#Location of HAProxy log file
HALOG="/var/log/haproxy.log"

HASOCKET="/var/run/haproxy.stat"

# warmup_url : URL for health check
# active == True will initially set its status as UP, MAINT otherwise
# always_up - never set MAINT on that backend (leave at least one host as always_up)
#Configure the below list with all the servers in HAProxy config filez
SERVERS = {
  'server1': { 'active': True,  
              'always_up': True,  
              'warmup_url': 'http://127.0.0.1:8081/' },
              
  'server2'  : { 'active': False, 
              'always_up': False, 
              'warmup_url': 'http://127.0.0.1:8082/' },
              
  
}

#awkCMD="awk 'NR==1; NR>1 { print $0 | \"sort -k12rn,12\"}'"
awkCMD="awk '$1 ~ /servers\/node/{ print $1, $12 }'"

CMD_DISABLE    = 'echo "disable server b-%s/%s" | socat stdio '+HASOCKET
CMD_ENABLE     = 'echo "enable server b-%s/%s" | socat stdio '+HASOCKET
CMD_SET_WEIGHT = 'echo "set weight b-%s/%s %d" | socat stdio '+HASOCKET

p = subprocess.Popen("cat "+HALOG+" | halog -srv -H -q | "+awkCMD+" | column -t", stdout=subprocess.PIPE, shell=True)
(output, err) = p.communicate()
## Wait for command to terminate. Get return returncode ##
p_status = p.wait()
avg_resp_times =  output.split()
resp_dict = {}
counter = 1
key=""
value=0
for line in avg_resp_times:
	if counter%2==1:	
		key = line
	else:
		value = int(line)
		resp_dict[key] = value	
	counter+=1
pprint(resp_dict)
 
