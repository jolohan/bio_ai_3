package ga;

import inputOutput.LoadImage;
import inputOutput.Main;

import java.util.ArrayList;

/**
 * Created by johan on 14/03/17.
 */
public class Individual {

    // =====================
    static int imageHeight;
    static int imageWidth;
    // =====================

    public final int[][] imageArray;
    private int[] genoType;
    private int[] segmentation;
    private ArrayList<ArrayList<Integer>> listOfSegments;
    private ArrayList<Integer> edgePixels;

    public Individual(int[][] imageArray, int[] genoType) {
        this.imageArray = imageArray;
        this.genoType = genoType;
        updateRepresentations();
    }

    public Individual(LoadImage loadedImage) {
        this.imageArray = loadedImage.getImageArray();
        this.imageHeight = loadedImage.getHeight();
        this.imageWidth = loadedImage.getWidth();
        int genoTypeSize = imageArray.length;
        this.genoType = new int[genoTypeSize];
        initIndividual();
    }

    void updateRepresentations() {
        updateSegmentation();
        //updateSegmentLists();
        //updateEdgePixels();
    }

    private void initIndividual() {
        initialAdjacency();
        //printArrayAsMatrix(genoType);
        updateRepresentations();
       // printArrayAsMatrix(segmentation);
    }

    // NEEDS RANDOMNESS
    void initialAdjacency() {
        for (int pixel = 0; pixel < genoType.length; pixel++) {
            ArrayList<Integer> neighbours = getNeighbours(pixel);
            int mostSimilarNeighbour = getMostSimilarNeighbour(neighbours, pixel);
            genoType[pixel] = mostSimilarNeighbour;
        }
    }

    void regionGrowing() {
        boolean[] visited = new boolean[imageArray.length];
        int lastSeedPoint = -1;
        int seedPoint;
        while (true) {
            seedPoint = getSeedPoint(visited, lastSeedPoint);
            if (seedPoint == -1) {
                break;
            }
            lastSeedPoint = seedPoint;
            growSingleRegion(visited, seedPoint);
            if (seedPoint%1000 == 0) {
                System.out.println(seedPoint);
            }
        }
    }

    private int getSeedPoint(boolean[] visited, int lastSeedPoint) {
        int seedPoint = lastSeedPoint;
        if (lastSeedPoint == -1) {
            return 0;
        }
        while (visited[seedPoint]) {
            if (seedPoint == genoType.length-1) {
                return -1;
            }
            seedPoint ++;
        }
        return seedPoint;
    }

    private void growSingleRegion(boolean[] visited, int seedPoint) {
        ArrayList<Integer> q = new ArrayList<>();
        q.add(seedPoint);
        while (! q.isEmpty()) {
            int pixel = q.remove(q.size()-1);
            visited[pixel] = true;
            ArrayList<Integer> neighbours = getUnvisitedNeighbours(pixel, visited);
            double rnd = Math.random();
            if (rnd < 1 - Main.INIT_RANDOMNESS) {
                int i = 0;
                while (i < neighbours.size()) {
                    int neighbour = neighbours.get(i);
                    if (isSimilar(seedPoint, neighbour)) {
                        editGenoType(pixel, neighbour);
                        q.add(neighbour);
                        break;
                    }
                    i ++;
                }
                if (i == neighbours.size()) {
                    editGenoType(pixel, pixel);
                }
            }
            else {
                int mostSimilarNeighbour = getMostSimilarNeighbour(neighbours, pixel);
                if (mostSimilarNeighbour == -1
                        || getDistanceInColors(seedPoint,
                        mostSimilarNeighbour) > Main.THRESHHOLD) {
                    editGenoType(pixel, pixel);
                }
                else {
                    if (Math.random() < 1 - Main.INIT_RANDOMNESS) {
                        editGenoType(pixel, mostSimilarNeighbour);
                    }
                    else {
                        int index = (int) (Math.random()*neighbours.size());
                        editGenoType(pixel, neighbours.get(index));
                    }
                }
            }

        }

    }

    private int getMostSimilarNeighbour(ArrayList<Integer> neighbours, int pixel) {
        int mostSimilar = -1;
        double mostSimilarScore = 10000;
        for (int neighbour : neighbours) {
            double score = getDistanceInColors(neighbour, pixel);
            if (score < mostSimilarScore) {
                mostSimilar = neighbour;
                mostSimilarScore = score;
            }
        }
        return mostSimilar;
    }

    private void editGenoType(int from, int to) {
        this.genoType[from] = to;
    }

    private boolean isSimilar(int pixel1, int pixel2) {
        //System.out.println(getDistanceInColors(pixel1,pixel2));
        return getDistanceInColors(pixel1, pixel2) < Main.THRESHHOLD;
    }

    public double getDistanceInColors(int p1, int p2) {
        int[] pixel1 = imageArray[p1];
        int[] pixel2 = imageArray[p2];
        double answer = 0;
        for (int i = 0; i < pixel1.length; i++) {
            double colorDelta = Math.pow(pixel1[i] - pixel2[i], 2);
            answer += colorDelta;
        }
        return Math.sqrt(answer);
    }



    // UPDATE
    // ===========================================================

    private void updateSegmentation() {
        this.segmentation = new int[genoType.length];
        boolean[] visited = new boolean[genoType.length];
        int segmentNumber = 1;
        for (int i = 0; i < genoType.length; i++) {
            if (segmentation[i] == 0) {
                if (findSegment(i, segmentNumber, visited) == segmentNumber) {
                    segmentNumber ++;
                }
            }
        }
    }

    private int findSegment(int pixel, int segmentNumber, boolean[] visited) {
        visited[pixel] = true;
        int pointer = genoType[pixel];
        if (segmentation[pointer] == 0) {
            if (visited[pointer]) {
                segmentation[pixel] = segmentNumber;
                return segmentNumber;
            }
            else {
                segmentation[pixel] =
                        findSegment(pointer, segmentNumber, visited);
                return segmentation[pixel];
            }
        }
        else {
            segmentation[pixel] = segmentation[pointer];
            return segmentation[pointer];
        }
    }

    private void updateSegmentLists() {
        this.listOfSegments = new ArrayList<>(10000);
        for (int i = 0; i < genoType.length; i++) {
            int segmentNumber = segmentation[i];
            while (listOfSegments.size() <= segmentNumber) {
                listOfSegments.add(new ArrayList<>(10000));
            }
            if (segmentNumber == -1) {
                System.out.println(i);
            }
            listOfSegments.get(segmentNumber).add(i);
        }
    }

    private void updateEdgePixels() {
        this.edgePixels = new ArrayList<>();
        ArrayList<Integer> temp = new ArrayList<>(1000);
        for (int i = 0; i < genoType.length; i++) {
            if (isEdgePixel(i)) {
                temp.add(i);
            }
            if (temp.size()%1000 == 0) {
                edgePixels.addAll(temp);
                temp = new ArrayList<>(1000);
            }
        }
    }

    // ===========================================================


    public int[] getGenoType() {
        return genoType;
    }

    public int[] getSegmenation() {
        return segmentation;
    }

    public ArrayList<ArrayList<Integer>> getListOfSegments() {
        return listOfSegments;
    }

    public ArrayList<Integer> getEdgePixels() {
        return edgePixels;
    }

    public void printGenoType() {
        String s = "";
        for (int i = 0; i < imageHeight; i ++) {
            String r = "";
            for (int j = 0; j < imageWidth; j++) {
                r += genoType[i*imageWidth+j] + " ";
            }
            r = r.trim();
            s += r + "\n";
        }
        System.out.println(s);
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < imageHeight; i ++) {
            String r = "";
            for (int j = 0; j < imageWidth; j++) {
                r += segmentation[getRowCol(i, j)];
                r += " ";
            }
            r = r.trim();
            s += r + "\n";
        }
        return s;
    }

    // GA operations
    //=================================================================================

    void mutateGene(int index) {
        ArrayList<Integer> neighbours = getNeighbours(index);
        int newIndex = (int) (Math.random()*neighbours.size());
        genoType[index] = neighbours.get(newIndex);
    }



    //=================================================================================

    public static int getRowCol(int row, int col) {
        return row*imageWidth + col;
    }

    public static int[] getRowCol(int rowCol) {
        int[] coordinates = new int[2];
        coordinates[0] = rowCol/imageWidth;
        coordinates[1] = rowCol%imageWidth;
        return coordinates;
    }

    public static int getRow(int rowCol) {
        return rowCol/imageWidth;
    }

    public static int getCol(int rowCol) {
        return rowCol%imageWidth;
    }

    private ArrayList<Integer> getNeighbours(int pixel) {
        ArrayList<Integer> neighbours = new ArrayList<>(4);
        ArrayList<Integer> temp = new ArrayList<>(4);
        temp.add(pixel-1);
        temp.add(pixel+1);
        temp.add(pixel-imageWidth);
        temp.add(pixel+imageWidth);
        for (int i = 0; i < temp.size(); i++) {
            int neighbour = temp.get(i);
            if (neighbour >= 0 && neighbour < genoType.length) {
                if (getRow(pixel) == getRow(neighbour) ||
                        getCol(pixel) == getCol(neighbour)) {
                    neighbours.add(neighbour);
                }
            }
        }
        //System.out.println(pixel+"      "+neighbours);
        return neighbours;
    }

    private ArrayList<Integer> getUnvisitedNeighbours(
            int pixel, boolean[] visited) {
        ArrayList<Integer> neighbours = getNeighbours(pixel);
        for (int i = 0; i < neighbours.size(); i++) {
            if (visited[neighbours.get(i)]) {
                neighbours.remove(i);
            }
        }
        return neighbours;
    }

    public boolean isEdgePixel(int pixel) {
        ArrayList<Integer> neighbours = getNeighbours(pixel);
        for (int neighbour : neighbours) {
            if (isDifferentSegments(pixel, neighbour)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDifferentSegments(int pixel1, int pixel2) {
        return (segmentation[pixel1] == segmentation[pixel2]);
    }

    private static void printArrayAsMatrix(int[] array) {
        String s = "";
        String r;
        r = "";
        for (int i = 0; i < array.length; i++) {
            if (i %imageWidth == 0) {
                s += r + "\n";
                r = "";
            }
            r += array[i]+ " ";
        }
        s += r + "\n";
        System.out.println(s);
    }

}
