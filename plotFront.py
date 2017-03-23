import matplotlib.pyplot as plt
import numpy as np
import sys

# Get the total number of args passed to the demo.py
total = len(sys.argv)

# Get the arguments list
cmdargs = str(sys.argv)

# Print it
print ("The total numbers of args passed to the script: %d " % total)
print ("Args list: %s " % cmdargs)

fileNameIndex = 1
col1 = sys.argv[fileNameIndex].split("-")[1]
col2 = sys.argv[fileNameIndex].split("-")[2].split(".")[0]

plt.plotfile(sys.argv[fileNameIndex], delimiter=' ', cols=(0, 1),
             names=(col1, col2), marker='o', comments="#1 Edge and deviation:")
with open(sys.argv[fileNameIndex]) as f:
	lines = f.readlines()
	print(len(lines))
	data = [line.split() for line in lines]
	out = [(float(x), float(y), int(z)) for x, y, z in data]
title = ""
for j in range(len(out)):
	i = out[j]
	plt.scatter(i[0],i[1])
	plt.xlabel(col1)
	plt.ylabel(col2)
	title += "#"+str(j+1)+": "+str(out[len(out)-j-1][2]) +"		"
plt.title(title)
plt.show()