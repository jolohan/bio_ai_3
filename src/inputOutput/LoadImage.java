package inputOutput;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by johan on 14/03/17.
 */
public class LoadImage {

    public static final String IMAGE_PATH = "Test Image 3/";
    public static final String IMAGE_NAME = "/Test image.jpg";

    // "Test Image 3/1/Test image.jpg"

    private final int[][] imageArray;
    private int height;
    private int width;

    public LoadImage(int imageNumber) throws IOException {
        String s = IMAGE_PATH+imageNumber+IMAGE_NAME;
        InputStream inputStream = new FileInputStream(s);
        BufferedImage image = null;
        try {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageArray = convertToArrayRGB(image);
    }


    // Changed to 3x2 image while testing
    private int[][] convertToArrayRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
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
