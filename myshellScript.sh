#!/usr/bin/env bash

echo "hello world"
open "/Users/johan/Documents/bio_ai_3/Test Image 3/0_Results/"$1"-1-"$2".jpg" -a Preview
open "/Users/johan/Documents/bio_ai_3/Test Image 3/0_Results/"$1"-2-"$2".jpg" -a Preview
python plotFront.py $3

