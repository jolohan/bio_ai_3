package main;

import ga.Individual;
import input.LoadImage;

import java.io.IOException;

/**
 * Created by johan on 14/03/17.
 */
public class Main {

    public static final double THRESHHOLD = 0.1;

    public static void main(String[] args) throws IOException{
        LoadImage img = new LoadImage(1);
        //System.out.println(img);
        Individual a = new Individual(img.getImageMatrix());
        System.out.println(a);
    }

}
