#!/usr/bin/python

# python haproxy_autoscale.py /path/to/your/haproxy/httplog

import sys
import sys
import os
import time
import re
from threading import Timer
from datetime import datetime

import urllib2
import random

# response time threshold in milliseconds: when backend starts responding 
# slower than the threshold we scale up, otherwise scale down.
THRESHOLD = 500

# num of requests to calc average
NUM_REQ = 15

# need this backend to set correct initial status of backend servers
BACKEND = "servers"

HALOG="/var/log/haproxy.log"

HASOCKET="/var/run/haproxy.stat"

# any url that goes straight to the backend is fine as warmup_url
# active == True will initially set its status as UP, MAINT otherwise
# always_up - never set MAINT on that backend (leave at least one host as always_up)
SERVERS = {
  'node2': { 'active': True,  
              'always_up': True,  
              'warmup_url': 'http://localhost:8081/' },
              
  'node3'  : { 'active': False, 
              'always_up': False, 
              'warmup_url': 'http://localhost:8082/' },
              
  
}


CMD_DISABLE    = 'echo "disable server %s/%s" | socat stdio '+HASOCKET
CMD_ENABLE     = 'echo "enable server %s/%s" | socat stdio '+HASOCKET
CMD_SET_WEIGHT = 'echo "set weight %s/%s %d" | socat stdio '+HASOCKET

def watch(thefile):
  """
  opens thefile and keeps reading new lines.
  this is supposed to be a syslog log file.
  """
  thefile.seek(0,2)      # Go to the end of the file
  while True:
    line = thefile.readline()
    if not line:
      time.sleep(0.1)    # Sleep briefly
      continue
    yield line

def host_to_scaleup():
  """
  searches through the list of not yet active backends 
  and returns a random choice, otherwise returns None
  """
  print "In host_to_scaleup"
  print SERVERS
  hosts = filter(lambda h: not SERVERS[h]['active'], SERVERS)
  print "Hosts available"
  print hosts
  if len(hosts):
    return random.choice(hosts)
  # otherwise return None, nothing to scale up
  
def host_to_scaledown():
  """
  filters only active hosts and returns a random choice, 
  None otherwise.
  """
  hosts = filter(lambda h: SERVERS[h]['active'] and not SERVERS[h]['always_up'], SERVERS)
  if len(hosts):
    return random.choice(hosts)
  # otherwise return None, nothing to scale down
  
def scale_up(backend, host):
  """
  send a 'warmup' request to the host in question
  and adds it to the HAProxy's active backend servers list,
  i.e. sets UP status
  """
  warmup_url = SERVERS[host]['warmup_url']
  print "%s: warming up at %s" % (datetime.now(), warmup_url)
  req = urllib2.Request(warmup_url)
  req.add_header('User-Agent', 'haproxy_autoscale')
  try: 
    r = urllib2.urlopen(req)
    #print r.info()
  except urllib2.HTTPError, e:
    print "*** didn't get a 200/OK response, sorry: ", e.code
  except urllib2.URLError, e:
    print "*** couldn't reach the backend server: ", e.reason
  else:
    # send socket commands to (re-)enable the backend
    cmd1 = CMD_ENABLE % (backend, host)
    print "Scaling up!"
    print cmd1
    cmd2 = CMD_SET_WEIGHT % (backend, host, 1)
    os.system(cmd1)
    os.system(cmd2)
    SERVERS[host]['active'] = True
  
def scale_down(backend, host):
  """
  removes host from HAProxy active backend servers list,
  i.e. sets MAINT status
  """
  print "%s: turning DOWN %s/%s" % (datetime.now(), backend, host)
  cmd1 = CMD_SET_WEIGHT % (backend, host, 0)
  cmd2 = CMD_DISABLE % (backend, host)
  print cmd1
  os.system(cmd1)
  # for some reason cmd1 does not always work
  # so we set weight to 0, just in case.
  os.system(cmd2)
  print cmd2
  SERVERS[host]['active'] = False

# this is where we store response times
resps = []

def avg_resp_time(new_val):
  """
  adds new_val to the resps arrays 
  and returns average over all requests in the list.
  """
  resps.append(new_val)
  if len(resps) > NUM_REQ: 
    # keep list length up to the NUM_REQ maximum items
    del(resps[0])
    return sum(resps) / len(resps)
  # otherwise we return None: not enough data

def random_scale_up(backend):
  """does the opposite of random_scale_down()"""
  h_up = host_to_scaleup()
  print "Adding %s" % (h_up)
  if h_up: 
    scale_up(backend, h_up)
  reset_cooldown_timer(backend)
  
def random_scale_down(backend):
  """
  runs after about 5 mins of inactivity 
  (e.g. no incoming requests)
  """
  h_down = host_to_scaledown()
  if h_down:
    print "%s: scaling down %s" % (datetime.now(), h_down)
    scale_down(backend, h_down)
    SERVERS[h_down]['active'] = False
    reset_cooldown_timer(backend)
  
# when no requests are coming in anymore we still 
# want to scale down automatically, after some time.
cooldown_timer = None

def reset_cooldown_timer(backend):
  """
  creates new timer to scale down 
  after 5 min of inactivity
  """
  global cooldown_timer
  if cooldown_timer: cooldown_timer.cancel()
  cooldown_timer = Timer(60, random_scale_down, [backend])
  cooldown_timer.start()
  
# set initial status of every backend server
for h in SERVERS:
  if SERVERS[h]['active']:
    scale_up(BACKEND, h)
  else:
    scale_down(BACKEND, h)
  
  
# regexp to match against haproxy log file
p = re.compile('.*([a-zA-Z0-9\-_]+)/([a-zA-Z0-9\-_]+) \d+/\d+/\d+/(\d+)/.*')

# scale up/down count threshold
scale_threshold_count = 0

print "Before loop"
print SERVERS
# endless loop
for line in watch(open(HALOG)):
  
  r = p.match(line)
  if r:
    backend, host, rt = r.groups()
    print  backend, host, rt
    #if host in SERVERS:
    #  SERVERS[host]['active'] = True # set as active since it's in the logs
      
    # calculate average response time
    resp_time = avg_resp_time(int(rt))
    
    # check whether we have enough data to reason
    if resp_time is None: 
      continue
      
    elif resp_time > THRESHOLD:
      # please, do scale
      print "%s: avg resp time: %d" % (datetime.now(), resp_time)
      random_scale_up(BACKEND)
     
