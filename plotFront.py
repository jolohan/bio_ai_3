import matplotlib.pyplot as plt
import numpy as np
import sys
from mpl_toolkits.mplot3d import Axes3D

# Get the total number of args passed to the demo.py
total = len(sys.argv)

fileNameIndex = 1
listOfColumns = sys.argv[fileNameIndex].split("-")
print(listOfColumns)
if (len(listOfColumns) == 3):
	col1 = sys.argv[fileNameIndex].split("-")[1]
	col2 = sys.argv[fileNameIndex].split("-")[2].split(".")[0]

	plt.plotfile(sys.argv[fileNameIndex], delimiter=' ', cols=(0, 1),
	             names=(col1, col2), marker='o',
	             comments="#1 Edge and deviation:")
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

elif (len(listOfColumns) == 4):
	fig = plt.figure()
	ax = fig.add_subplot(111, projection='3d')
	col1 = sys.argv[fileNameIndex].split("-")[1]
	col2 = sys.argv[fileNameIndex].split("-")[2].split(".")[0]
	with open(sys.argv[fileNameIndex]) as f:
		lines = f.readlines()
		print(len(lines))
		data = [line.split() for line in lines]
		out = [(float(x), float(y), float(z), int(a)) for x, y, z, a in data]
	title = ""
	for j in range(len(out)):
		i = out[j]
		#ax.bar(i[0], i[1], i[2], zdir='z')
		ax.scatter(i[0], i[1], i[2], zdir='z', s=500, c=None)
		#ax.xlabel(col1)
		#ax.ylabel(col2)

		title += "#"+str(j+1)+": "+str(out[len(out)-j-1][2]) +"		"
	#ax.title(title)
plt.show()