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
col2 = sys.argv[fileNameIndex].split("-")[2]

plt.plotfile(sys.argv[fileNameIndex], delimiter=' ', cols=(0, 1),
             names=(col1, col2), marker='o')
plt.show()