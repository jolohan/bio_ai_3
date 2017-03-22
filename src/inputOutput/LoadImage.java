package inputOutput;

import ga.Individual;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by johan on 14/03/17.
 */
public class LoadImage {

    public static final String IMAGE_PATH = "Test Image 3/";
    public static final String IMAGE_NAME = "/Test image.jpg";
    public static final int imageNumber = 1;

    // =====================
    public static int imageHeight;
    public static int imageWidth;
    // =====================

    // "Test Image 3/1/Test image.jpg"

    static {
        String s = IMAGE_PATH+Main.IMAGE_NUMBER+IMAGE_NAME;
        try {
            InputStream inputStream = new FileInputStream(s);
            BufferedImage image;
            image = ImageIO.read(inputStream);
            IMAGE = image;
            imageHeight = image.getHeight();
            imageWidth = image.getWidth();

        } catch (IOException e) {
            try {
                System.out.println("couldn't read file name." +
                        "\ninput filename: ");
                Scanner scanner = new Scanner(System.in);
                s = scanner.next();
                InputStream inputStream = new FileInputStream(s);
                BufferedImage image;
                image = ImageIO.read(inputStream);
                IMAGE = image;
                imageHeight = image.getHeight();
                imageWidth = image.getWidth();
            }
            catch (Exception ei) {
                ei.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public static BufferedImage IMAGE;
    private final int[][] imageArray;
    private final int[][] imgArray;
    private int height;
    private int width;

    public LoadImage() {
        this.imgArray = convertToArrayRGB(IMAGE);
        //image = scale(image, LoadImage.imageNumber);
        int[][] temp = convertToArrayRGB(IMAGE);
        if (imageNumber == 1) {
            this.imageArray = temp;
        }
        else {
            this.imageArray = convertImgArray(temp, imageNumber);
        }
    }

    public static BufferedImage scale(BufferedImage sbi, int scale) {
        BufferedImage dbi = null;
        if(sbi != null) {
            dbi = new BufferedImage(sbi.getWidth()/scale,
                    sbi.getHeight()/scale, sbi.getType());
            Graphics2D g = dbi.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
            g.drawRenderedImage(sbi, at);
        }
        return dbi;
    }

    // Changed to 3x2 image while testing
    private int[][] convertToArrayRGB(BufferedImage image) {
        int width = 10;//image.getWidth();
        int height = 10;//image.getHeight();
        setHeightAndWidth(height, width);
        int numberOfColors = 3;
        int[][] result = new int[height*width][numberOfColors];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int color = image.getRGB(col, row);
                int blue = color & 0xff;
                int green = (color & 0xff00) >> 8;
                int red = (color & 0xff0000) >> 16;
                int[] rgb = new int[numberOfColors];
                rgb[0] = red; rgb[1] = green; rgb[2] = blue;
                result[row*width+col] = rgb;
            }
        }

        return result;
    }

    private void setHeightAndWidth(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int[][] getImageArray() {
        return imageArray;
    }

    private int[][] convertImgArray(int[][] imageArray, int size) {
        int newWidth = width/size;
        int newHeight = height/size;
        int newSize = newWidth*newHeight;
        int[][] newImageArray = new int[newSize][3];
        for (int i = 0; i < newImageArray.length; i++) {
            int[][] coordinates = Main.getCoordinatesXY(IMAGE, i);
            int[] averageColor = findAverageColorValues(
                    imageArray, coordinates);
            newImageArray[i] = averageColor;
        }
        return newImageArray;
    }

    private int[] findAverageColorValues(int[][] imageArray,
                                         int[][] coordinates) {
        int[] averageValues = new int[3];
        for (int i = 0; i < coordinates.length; i++) {
            int[] coordinateXY = coordinates[i];
            int row = coordinateXY[1];
            int col = coordinateXY[0];
            int index = Individual.getRowCol(row, col);
            int[] colorValues = imageArray[index];
            for (int j = 0; j < 3; j++) {
                averageValues[j] += colorValues[j];
            }
        }
        for (int i = 0; i < 3; i++) {
            averageValues[i] = averageValues[i]/coordinates.length;
        }
        return averageValues;
    }

    public String toString() {
        String s = "";
        String r;
        for (int i = 0; i < imageArray.length; i++) {
            r = "";
            System.out.println(i);
            for (int j = 0; j < imageArray[i].length; j++) {
                int[] cell = imageArray[i*width+j];
                r += "[ ";
                for (int k = 0; k < 3; k++) {
                    r += cell[k]+", ";
                }
                r = r.trim();
                r = r.trim();
                r = r.substring(0, r.length()-1);
                r += " ], ";
            }
            s += r + "\n\n";
        }
        return s;
    }
}
