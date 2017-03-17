package input;

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

    int[][][] imageMatrix;

    public LoadImage(int imageNumber) throws IOException {
        String s = IMAGE_PATH+imageNumber+IMAGE_NAME;
        InputStream inputStream = new FileInputStream(s);
        BufferedImage image = null;
        try {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageMatrix = convertTo2DUsingGetRGB(image);
    }

    private static int[][][] convertTo2DUsingGetRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int numberOfColors = 3;
        int[][][] result = new int[height][width][numberOfColors];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int color = image.getRGB(col, row);
                int blue = color & 0xff;
                int green = (color & 0xff00) >> 8;
                int red = (color & 0xff0000) >> 16;
                int[] rgb = new int[numberOfColors];
                rgb[0] = red; rgb[1] = green; rgb[2] = blue;
                result[row][col] = rgb;
            }
        }

        return result;
    }

    public int[][][] getImageMatrix() {
        return imageMatrix;
    }

    public String toString() {
        String s = "";
        String r;
        for (int i = 0; i < imageMatrix.length; i++) {
            r = "";
            System.out.println(i);
            for (int j = 0; j < imageMatrix[i].length; j++) {
                int[] cell = imageMatrix[i][j];
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
