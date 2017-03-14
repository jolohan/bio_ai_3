package main;

import input.LoadImage;

import java.io.IOException;

/**
 * Created by johan on 14/03/17.
 */
public class Main {

    public static void main(String[] args) throws IOException{
        LoadImage img = new LoadImage(1);
        System.out.println(img);
    }

}
