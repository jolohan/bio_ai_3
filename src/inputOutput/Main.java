package inputOutput;/**
 * Created by johan on 17/03/17.
 */

import ga.GeneticAlgorithm;
import ga.Individual;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main extends Application {

    // NEED TO BE SET!
    // ========================================================
    // WHICH IMAGE AND OBJECTIVES
    public static final String IMAGE_PATH = "Test Image 3/";
    public static final String IMAGE_NAME = "/Test image.jpg";
    public static int IMAGE_NUMBER = 1;
    public static final String filename = IMAGE_PATH
            +IMAGE_NUMBER+IMAGE_NAME;

    private static final int whichToLeaveOut = 3;

    public static final boolean[] WHICH_SCORES = new boolean[3];
    static {
        for (int i = 1; i < 4; i++) {
            if (i != whichToLeaveOut) {
                WHICH_SCORES[i - 1] = true;
            }
        }
    }

    // ========================================================

    public static final int Kth_NEAREST_NEIGHBOUR = 4;

    public static final double THRESHHOLD = 3;
    public static final int SIMILAR_SEGMENT_THRESHOLD = 5;
    public static final int SIMILARITY_ROOF = 80;
    public static final double segmentSizeDemand = 2000;
    public static final double INIT_RANDOMNESS = 1;
    public static final double CROSSOVER_RATE = 0.7;
    public static final double MUTATION_RATE = 0.1;

    public static final int POPULATION_SIZE = 20;
    public static final int ARCHIVE_SIZE = POPULATION_SIZE;
    public static final int NUMBER_OF_GENERATIONS= 200;

    // ========================================================

    public static void main(String[] args) {
        launch();
    }

    // ========================================================

    private int width;
    private int height;
    private LoadImage img;
    private int[] segmentation;
    private Individual bestIndividual;

    public void init() throws IOException {
        Scanner sc = new Scanner(System.in);

        LoadImage img = new LoadImage();
        GeneticAlgorithm ga = new GeneticAlgorithm(img);

        Individual bestInd = ga.mainLoop();
        this.bestIndividual = bestInd;

        this.width = img.getWidth();
        this.height = img.getHeight();
        this.img = img;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Drawing Operations Test");
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawOutlines(gc);
        gc.setLineWidth(1);
        gc.setFill(Color.BLACK);

        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        stop(primaryStage);
    }

    public void stop(Stage primaryStage) {
        primaryStage.close();
    }

    private void drawOutlines(GraphicsContext gc) {
        double step = 0.5;
        bestIndividual.updateEdgePixels();
        ArrayList<Integer> edgePixels = bestIndividual.getEdgePixels();
        for (int index = 0; index < edgePixels.size(); index++) {
            int pixel = edgePixels.get(index);
            int i = Individual.getRow(pixel);
            int j = Individual.getCol(pixel);
            gc.strokeLine(j-step, i-step, j+step,i+step);
        }
    }


    public static void makeDifferentColors(LoadImage image, Individual ind) {
        BufferedImage img = image.IMAGE;
        int numberOfSegments = ind.findHighestSegmentNumber();
        java.awt.Color[] colors = new java.awt.Color[numberOfSegments+1];
        for (int i = 1; i < numberOfSegments+1; i++) {
            colors[i] = new java.awt.Color((int) (Math.random()*255),
                    (int) (Math.random()*255), (int) (Math.random()*255));
        }
        int[] genoType = ind.getGenoType();
        java.awt.Color color;
        for (int i = 0; i < genoType.length; i++) {
            int segmentValue = genoType[i];
            color = colors[segmentValue];
            int[][] coordinates = getCoordinatesXY(img, i);
            for (int j = 0; j < coordinates.length; j++) {
                int[] coordinateXY = coordinates[j];
                int x = Individual.getCol(coordinateXY[0]);
                int y = Individual.getRow(coordinateXY[1]);
                img.setRGB(x, y, color.getRGB());
            }
        }
        try {
            File f = new File("segmentColours" + ".jpg");
            ImageIO.write(img, "jpg", f);
        } catch (IOException e) {
            System.out.println("IMAGE FAILED TO BE WRITTEN!");
        }
    }

    public static void colorBigAndWrite(LoadImage image, int type,
                                        Individual ind) {
        BufferedImage img = new BufferedImage(image.IMAGE.getWidth(),
                image.IMAGE.getHeight(), image.IMAGE.getType());
        java.awt.Color color;
        color = new java.awt.Color(255, 255, 255);
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    if (type == 1) {
                        img.setRGB(j,i, color.getRGB());
                    }
                    else if (type == 2) {
                        img.setRGB(j, i, image.IMAGE.getRGB(j, i));
                    }
                }
            }
        color = new java.awt.Color(0, 0, 0);
        if (type == 2) {
            color = new java.awt.Color(0, 255, 0);
        }
        ind.updateEdgePixels();
        ArrayList<Integer> edgePixels = ind.getEdgePixels();
        for (int i = 0; i < edgePixels.size(); i++) {
            int edgePixel = edgePixels.get(i);
            if (LoadImage.compressRatio != 1) {
                int[][] coordinates = getCoordinatesXY(img, edgePixel);
                for (int j = 0; j < coordinates.length; j++) {
                    int[] coordinateXY = coordinates[j];
                    int x = coordinateXY[0];
                    int y = coordinateXY[1];
                    img.setRGB(x,y, color.getRGB());
                }
            }
            else {
                int x = Individual.getCol(edgePixel);
                int y = Individual.getRow(edgePixel);
                img.setRGB(x,y,color.getRGB());
            }
        }
        try {
            String s = "Test Image 3/0_Results/"
                    +IMAGE_NUMBER+"-"+type+"-"+ind.findNumberOfSegments()+".jpg";
            File f = new File(s);
            ImageIO.write(img, "jpg", f);
        } catch (IOException e) {
            System.out.println("IMAGE FAILED TO BE WRITTEN!");
        }
    }

    public static int[][] getCoordinatesXY(BufferedImage img, int index) {
        int size = LoadImage.compressRatio;
        int[][] coordinates = new int[(int)Math.pow(size, 2)][2];
        int row = Individual.getRow(index);
        int col = Individual.getCol(index);
        row = row*size;
        col = col*size;

        int counter = 0;
        for (int i = -size/2; i < size/2; i++) {
            for (int j = -size/2; j < size/2; j++) {
                int x = col - i;
                int y = row - j;
                x = Math.min(img.getWidth()-1, x);
                x = Math.max(0, x);
                y = Math.min(img.getHeight()-1, y);
                y = Math.max(0, y);
                coordinates[counter][0] = x;
                coordinates[counter][1] = y;
                counter ++;
            }
        }
        //System.out.println(2+ " "+row+"    "+col);
        return coordinates;
    }
}
