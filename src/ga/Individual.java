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

    // =====================
    private double overallDeviation;
    private double edgeValue;
    private double connectivity;
    private double[] scores;
    private double fitness;
    // =====================


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

    void updateFitness(double fitnessScore) {
        this.fitness = fitnessScore;
    }

    void updateScores() {
        updateRepresentations();
    }

    private void updateRepresentations() {
        int numberOfSegments = updateSegmentation();
        System.out.println(String.format(
                "Number of segments: %d", numberOfSegments));
        updateSegmentLists(numberOfSegments);
        //updateEdgePixels();
        if (Main.WHICH_SCORES[0]) { setOverallDeviation(); }
        if (Main.WHICH_SCORES[1]) { setEdgeValues(); }
        if (Main.WHICH_SCORES[2]) { setConnectivity(); }
        setScores();
    }

    private void initIndividual() {
        initialAdjacency();
        //printArrayAsMatrix(genoType);
        updateRepresentations();
        //printArrayAsMatrix(segmentation);
        //System.out.println(edgePixels);
    }

    // NEEDS RANDOMNESS
    void initialAdjacency() {
        for (int pixel = 0; pixel < genoType.length; pixel++) {
            ArrayList<Integer> neighbours = getNeighbours(pixel);
            int mostSimilarNeighbour = getMostSimilarNeighbour(neighbours, pixel);
            if (genoType[mostSimilarNeighbour] == pixel) {
                genoType[pixel] = neighbours.get(getRandomIntFromArray(neighbours));
            }
            else {
                genoType[pixel] = mostSimilarNeighbour;
            }
        }
    }

    private int getRandomIntFromArray(ArrayList<Integer> array) {
        return (int) (Math.random()*array.size());
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

    // GA ========================================================

    private void setOverallDeviation() {
        double overAllDeviation = 0;
        for (int i = 0; i < listOfSegments.size(); i++) {
            overAllDeviation += getDeviationInSegment(listOfSegments.get(i));
        }
        this.overallDeviation = overAllDeviation;
    }

    private double getDeviationInSegment(ArrayList<Integer> segment) {
        double deviationInSegment = 0;
        if (segment.size() > 0) {
            int numColors = 3;
            int[] average = new int[numColors];
            for (int pixel = 0; pixel < segment.size(); pixel++) {
                for (int i = 0; i < numColors; i++) {
                    average[i] += imageArray[pixel][i];
                }
            }
            for (int i = 0; i < numColors; i++) {
                average[i] = average[i]/segment.size();
            }
            for (int pixel = 0; pixel < segment.size(); pixel++) {
                deviationInSegment += getDistanceFromCentroid(average, pixel);
            }

        }
        else {
            System.out.println("segment size is 0");
        }
        return deviationInSegment;
    }

    private double getDistanceFromCentroid(int[] centroid, int pixel) {
        int[] colorValues = imageArray[pixel];
        double answer = 0;
        for (int i = 0; i < centroid.length; i++) {
            double colorDelta = Math.pow(centroid[i] - colorValues[i], 2);
            answer += colorDelta;
        }
        return Math.sqrt(answer);
    }

    private void setEdgeValues() {
        double totalEdgeValue = 0;
        for (int i = 0; i < genoType.length; i++) {
            int edgePixel = genoType[i];
            ArrayList<Integer> neighbours = getNeighbours(edgePixel);
            for (int j = 0; j < neighbours.size(); j++) {
                int neighbour = neighbours.get(j);
                if (segmentation[edgePixel] != segmentation[neighbour]) {
                    double distance = getDistanceInColors(edgePixel, neighbour);
                    totalEdgeValue += distance;
                }
            }
        }
        this.edgeValue = totalEdgeValue;
    }

    private void setConnectivity() {
        double totalConnectivity = 0;
        for (int pixel = 0; pixel < segmentation.length; pixel++) {
            ArrayList<Integer> neighbours = getNeighbours(pixel);
            int numberOfNonConnections = 0;
            for (int i = 0; i < neighbours.size(); i++) {
                int neighbour = neighbours.get(i);
                if (segmentation[pixel] != segmentation[neighbour]) {
                    numberOfNonConnections ++;
                }
            }
            for (int i = 1; i <= numberOfNonConnections; i++) {
                if (i == 4) {
                    System.out.println("connectivity goes to 4");
                }
                totalConnectivity += (1/i);
            }
        }
        this.connectivity = totalConnectivity;
    }

    private void setScores() {
        scores = new double[3];
        if (Main.WHICH_SCORES[0]) { scores[0] = getOverAlldeviation(); }
        else { scores[0] = -1; }

        if (Main.WHICH_SCORES[1]){ scores[1] = getEdgeValues(); }
        else { scores[1] = -1; }

        if (Main.WHICH_SCORES[2]) { scores[2] = getConnectivity(); }
        else { scores[2] = -1; }
    }

    public double[] getScores() {
        return scores;
    }

    public double getOverAlldeviation() {
        return overallDeviation;
    }

    public double getEdgeValues() {
        return edgeValue;
    }

    public double getConnectivity() {
        return connectivity;
    }

    public double getFitness() { return fitness; }


    // UPDATE ====================================================

    private int updateSegmentation() {
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
        return segmentNumber;
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

    private void updateSegmentLists(int numberOfSegments) {
        this.listOfSegments = new ArrayList<>(numberOfSegments);
        for (int i = 0; i < genoType.length; i++) {
            int segmentNumber = segmentation[i];
            while (listOfSegments.size() <= segmentNumber) {
                listOfSegments.add(new ArrayList<>(
                        genoType.length/numberOfSegments*2));
            }
            if (segmentNumber == -1) {
                System.out.println(i);
            }
            listOfSegments.get(segmentNumber).add(i);
        }
    }

    public void updateEdgePixels() {
        this.edgePixels = new ArrayList<>();
        ArrayList<Integer> temp = new ArrayList<>(1000);
        for (int i = 0; i < genoType.length; i++) {
            if (isEdgePixel(i)) {
                edgePixels.add(i);
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
        for (int i = 0; i < neighbours.size(); i++) {
            int neighbour = neighbours.get(i);
            if (segmentation[pixel] != segmentation[neighbour]) {
                return true;
            }
        }
        return false;
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
