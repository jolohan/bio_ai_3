package inputOutput;/**
 * Created by johan on 17/03/17.
 */

import ga.Individual;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    // ========================================================

    public static final double THRESHHOLD = 100;
    public static final double INIT_RANDOMNESS = 0.0002;

    public static void main(String[] args) {
        launch();
    }

    // ========================================================

    private int width;
    private int height;
    private int step = 5;
    private int margin = 30;
    private int graphLineWidth = 2;
    private LoadImage img;
    private int[][] readableSegmentation;

    public void init() throws IOException {
        LoadImage img = new LoadImage(1);
        //System.out.println(img);
        Individual a = new Individual(img.getImageMatrix());
        this.readableSegmentation = a.makeReadableSegmenation();
        //System.out.println(a);
        //a.printGenoType();
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



        /*try {
            Thread.sleep(1000*10);
            stop(primaryStage);
        }
        catch (Exception e) {
            System.out.println(e.getStackTrace());
        }*/
    }

    public void stop(Stage primaryStage) {
        primaryStage.close();
    }

    private void drawOutlines(GraphicsContext gc) {
        double step = 0.5;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (Individual.isEdgePixel(this.readableSegmentation, i, j)) {
                    gc.strokeLine(j-step, i-step, j+step,i+step);
                }
            }
        }
    }
}
