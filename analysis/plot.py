import os
import sys
import math

import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

LOG_PATH = "logs-2.txt"
SEARCHSTRING = "NARF: " 
SAVENAME = "plot2"

x = []
y = []
z = []

fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')

f = open(LOG_PATH, "r")
lines = f.readlines()
for line in lines:
    pos = line.index(SEARCHSTRING) + len(SEARCHSTRING)
    payload = line[pos:-2]
    # last line as no CRLF, don't care
    foo = payload.split(";")
    
    x.append(float(foo[0]))
    y.append(float(foo[1]))
    z.append(float(foo[2]))
        
ax.scatter(x, y, zs=z) #, c="r", marker="s", label="ground truth")
plt.savefig(os.path.join(".", SAVENAME + ".png"), bbox_inches='tight')
plt.show()