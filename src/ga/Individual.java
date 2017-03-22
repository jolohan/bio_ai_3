package ga;

import inputOutput.LoadImage;
import inputOutput.Main;

import java.util.*;

/**
 * Created by johan on 14/03/17.
 */
public class Individual {



    public final int[][] imageArray;
    private int[] genoType;
    private ArrayList<Integer> edgePixels;

    // =====================
    private double overallDeviation;
    private double edgeValue;
    private double connectivity;
    private int numberOfSegments;
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
        int genoTypeSize = imageArray.length;
        this.genoType = new int[genoTypeSize];
        initIndividual();
    }

    private void initIndividual() {
        regionGrowing();
        numberOfSegments = findHighestSegmentNumber();
        System.out.println(numberOfSegments);
        joinSimilarSegmentsLoop();
        //printArrayAsMatrix(genoType);
        updateRepresentations();
    }

    private void updateRepresentations() {
        numberOfSegments = findHighestSegmentNumber();
        System.out.println(numberOfSegments);
        if (Main.WHICH_SCORES[0]) { setOverallDeviation(); }
        if (Main.WHICH_SCORES[1]) { setEdgeValues(); }
        if (Main.WHICH_SCORES[2]) { setConnectivity(); }
        setScores();
    }

    void updateFitness(double fitnessScore) {
        this.fitness = fitnessScore;
    }

    void updateScores() {
        updateRepresentations();
    }

    public int findHighestSegmentNumber() {
        int highestSegmentNumber = -1;
        for (int i = 0; i < genoType.length; i++) {
            highestSegmentNumber = Math.max(highestSegmentNumber, genoType[i]);
        }
        return highestSegmentNumber;
    }

    private int regionGrowing() {
        boolean[] visited = new boolean[imageArray.length];
        int segmentNumber = 1;
        for (int i = 0; i < imageArray.length; i++) {
            if (! visited[i]) {
                growSingleRegion(visited, i, segmentNumber);
                segmentNumber ++;
            }
        }
        return segmentNumber;
    }

    void growSingleRegion(boolean[] visited, int seedPoint,
                          int segmentNumber) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(seedPoint);
        visited[seedPoint] = true;
        int thisPoint;

        while (! queue.isEmpty()) {
            thisPoint = queue.poll();
            genoType[thisPoint] = segmentNumber;
            //System.out.println("segmentNumber: "+segmentNumber);
            ArrayList<Integer> neighbours =getNeighbours(thisPoint);
            for (int i = 0; i < neighbours.size(); i++) {
                int neighbour = neighbours.get(i);
                if (!visited[neighbour] && getDistanceInColors(thisPoint, neighbour)
                        < Main.THRESHHOLD) {
                    queue.add(neighbour);
                    visited[neighbour] = true;
                }
                else {
                    //System.out.println("\n"+thisPoint+"      "+neighbour);
                    double distance = getDistanceInColors(thisPoint, neighbour);
                }
            }
        }
    }

    private void joinSimilarSegmentsLoop() {
        ArrayList<Integer> newAvailableSegmentNumbers = joinSimilarSegments();
        int counter = 0;
        ArrayList<Integer> availableSegmentNumbers = new ArrayList<>();
        while (newAvailableSegmentNumbers.size() > 0) {
            newAvailableSegmentNumbers = joinSimilarSegments();
            updateSegmentNumbers(newAvailableSegmentNumbers);
            counter ++;
        }
    }

    private ArrayList<Integer> joinSimilarSegments() {
        int[][] centroids = getCentroids();
        ArrayList<Integer> availableSegmentNumbers = new ArrayList<>();
        for (int i = 0; i < imageArray.length; i++) {
            ArrayList<Integer> neighbours = getNeighbours(i);
            for (int j = 0; j < neighbours.size(); j++) {
                int neighbour = neighbours.get(j);
                if (genoType[i] != genoType[neighbour]) {
                    double distance = getDistance(
                            centroids[genoType[i]], centroids[genoType[neighbour]]);
                    if (distance < Main.SIMILAR_SEGMENT_THRESHOLD) {
                        availableSegmentNumbers.add(genoType[neighbour]);
                        //System.out.println("\n hey");
                        //System.out.println(genoType[i]+"    "+genoType[neighbour]);
                        renumberSegment(genoType[i], neighbour);
                    }
                }
            }
        }
        return availableSegmentNumbers;
    }

    private void renumberSegment(int newSegmentNumber, int index) {
        int oldSegmentNumber = genoType[index];
        //System.out.println("old segment number: "+oldSegmentNumber);
        Queue<Integer> queue = new LinkedList();
        queue.add(index);
        genoType[index] = newSegmentNumber;
        int point;
        while (! queue.isEmpty()) {
            point = queue.poll();
            ArrayList<Integer> neighbours = getNeighbours(point);
            for (int i = 0; i < neighbours.size(); i++) {
                int neighbour = neighbours.get(i);
                if (genoType[neighbour] == oldSegmentNumber) {
                    queue.add(neighbour);
                    //System.out.println("old "+genoType[neighbour]);
                    genoType[neighbour] = newSegmentNumber;
                    //System.out.println("new " +genoType[neighbour]);
                    //System.out.println(1/0);
                }
            }
        }
    }

    private void updateSegmentNumbers(ArrayList<Integer> availableSegmentNumbers) {
        //System.out.println(availableSegmentNumbers);
        IntComp intComp = new IntComp();
        PriorityQueue<Integer> q = new PriorityQueue<>(intComp);
        q.addAll(availableSegmentNumbers);
        System.out.println(q);
        int segmentNumber;
        for (int i = 0; i < genoType.length; i++) {
            segmentNumber = genoType[i];
            int numberOfAvailable = availableSegmentNumbers.size();
            if (segmentNumber > numberOfSegments-numberOfAvailable) {
                int newSegmentNumber = q.poll();
                System.out.println(newSegmentNumber);
                renumberSegment(newSegmentNumber, i);
            }
        }
    }

    private int getRandomIntFromArray(ArrayList<Integer> array) {
        return (int) (Math.random()*array.size());
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

    private boolean isSimilar(int pixel1, int pixel2) {
        //System.out.println(getDistanceInColors(pixel1,pixel2));
        return getDistanceInColors(pixel1, pixel2) < Main.THRESHHOLD;
    }

    public double getDistance(int[] colors1, int[] colors2) {
        double answer = 0;
        for (int i = 0; i < colors1.length; i++) {
            double colorDelta = Math.pow(colors1[i] - colors2[i], 2);
            answer += colorDelta;
        }
        return Math.sqrt(answer);
    }

    public double getDistanceInColors(int p1, int p2) {
        int[] pixel1 = imageArray[p1];
        int[] pixel2 = imageArray[p2];
        return getDistance(pixel1, pixel2);
    }

    // GA ========================================================

    private void setOverallDeviation() {
        int[][] centroids = getCentroids();
        int segmentNumber;
        double deviation;
        double overAllDeviation = 0;
        for (int i = 0; i < genoType.length; i++) {
            segmentNumber = genoType[i];
            deviation = getDistanceFromCentroid(centroids[segmentNumber], i);
            overAllDeviation += deviation;
        }
        this.overallDeviation = overAllDeviation;
    }

    private int[][] getCentroids() {
        int segmentNumber;
        int numColors = 3;
        int[][] centroids = new int[numberOfSegments+1][numColors];
        int[] numberOfPixelsInSegments = new int[numberOfSegments+1];
        for (int i = 0; i < genoType.length; i++) {
            segmentNumber = genoType[i];
            for (int j = 0; j < numColors; j++) {
                //System.out.println(i+"      "+centroids.length+"        "+segmentNumber);
                centroids[segmentNumber][j] += imageArray[i][j];
                numberOfPixelsInSegments[segmentNumber] ++;
            }
        }
        for (int i = 1; i < numberOfSegments+1; i++) {
            if (numberOfPixelsInSegments[i] > 0) {
                for (int j = 0; j < numColors; j++) {
                    centroids[i][j] = centroids[i][j]/numberOfPixelsInSegments[i];
                }
            }
        }
        return centroids;
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
                if (genoType[edgePixel] != genoType[neighbour]) {
                    double distance = getDistanceInColors(edgePixel, neighbour);
                    totalEdgeValue += distance;
                }
            }
        }
        this.edgeValue = (-1)*totalEdgeValue;
    }

    private void setConnectivity() {
        double totalConnectivity = 0;
        for (int pixel = 0; pixel < genoType.length; pixel++) {
            ArrayList<Integer> neighbours = getNeighbours(pixel);
            int numberOfNonConnections = 0;
            for (int i = 0; i < neighbours.size(); i++) {
                int neighbour = neighbours.get(i);
                if (genoType[pixel] != genoType[neighbour]) {
                    numberOfNonConnections ++;
                }
            }
            for (int i = 1; i <= numberOfNonConnections; i++) {
                if (i == 4) {
                    //System.out.println("connectivity goes to 4");
                }
                totalConnectivity += (1/i);
            }
        }
        this.connectivity = totalConnectivity;
    }

    private void setScores() {
        scores = new double[3];
        if (Main.WHICH_SCORES[0]) { scores[0] = getOverAllDeviation(); }
        else { scores[0] = -1; }

        if (Main.WHICH_SCORES[1]){ scores[1] = getEdgeValues(); }
        else { scores[1] = -1; }

        if (Main.WHICH_SCORES[2]) { scores[2] = getConnectivity(); }
        else { scores[2] = -1; }
    }

    public double[] getScores() {
        return scores;
    }

    public double getOverAllDeviation() {
        return overallDeviation;
    }

    public double getEdgeValues() {
        return edgeValue;
    }

    public double getConnectivity() {
        return connectivity;
    }

    public int getNumberOfSegments() {
        return numberOfSegments;
    }

    public double getFitness() { return fitness; }

    // ===========================================================

    boolean editGeneType(int index, int newValue) {
        ArrayList<Integer> neighbours = getNeighbours(index);
        for (int i = 0; i < neighbours.size(); i++) {
            int neighbourValue = genoType[neighbours.get(i)];
            if (newValue == neighbourValue) {
                genoType[index] = neighbourValue;
                return true;
            }
        }
        return false;
    }

    public int[] getGenoType() {
        return genoType;
    }

    public ArrayList<Integer> getEdgePixels() {
        return edgePixels;
    }

    public void printGenoType() {
        String s = "";
        for (int i = 0; i < LoadImage.imageHeight; i ++) {
            String r = "";
            for (int j = 0; j < LoadImage.imageWidth; j++) {
                r += genoType[i*LoadImage.imageWidth+j] + " ";
            }
            r = r.trim();
            s += r + "\n";
        }
        System.out.println(s);
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < LoadImage.imageHeight; i ++) {
            String r = "";
            for (int j = 0; j < LoadImage.imageWidth; j++) {
                r += genoType[getRowCol(i, j)];
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
        int neighbour = neighbours.get(newIndex);
        int newSegmentNumber = genoType[neighbour];
        genoType[index] = newSegmentNumber;
    }


    // UPDATE ====================================================

    public void updateEdgePixels() {
        this.edgePixels = new ArrayList<>();
        ArrayList<Integer> temp = new ArrayList<>(10000);
        for (int i = 0; i < genoType.length; i++) {
            if (isEdgePixel(i)) {
                edgePixels.add(i);
            }
        }
    }


    //=================================================================================

    public static int getRowCol(int row, int col) {
        return row*LoadImage.imageWidth + col;
    }

    public static int[] getRowCol(int rowCol) {
        int[] coordinates = new int[2];
        coordinates[0] = rowCol/LoadImage.imageWidth;
        coordinates[1] = rowCol%LoadImage.imageWidth;
        return coordinates;
    }

    public static int getRow(int rowCol) {
        return rowCol/LoadImage.imageWidth;
    }

    public static int getCol(int rowCol) {
        return rowCol%LoadImage.imageWidth;
    }

    private ArrayList<Integer> getNeighbours(int pixel) {
        ArrayList<Integer> neighbours = new ArrayList<>(4);
        ArrayList<Integer> temp = new ArrayList<>(4);
        temp.add(pixel-1);
        temp.add(pixel+1);
        temp.add(pixel-LoadImage.imageWidth);
        temp.add(pixel+LoadImage.imageWidth);
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
            if (genoType[pixel] != genoType[neighbour]) {
                //System.out.println(pixel +" is edgepixel");
                return true;
            }
        }
        return false;
    }

    Individual makeCopy(int[][] imageArray) {
        int[] genoType = new int[getGenoType().length];
        int value;
        for (int i = 0; i < genoType.length; i++) {
            value = (int) (getGenoType()[i]*1.0);
            genoType[i] = value;
        }
        return new Individual(imageArray, genoType);
    }

/*    public Individual makeCopy() {
        Individual ind = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
            ind = (Individual) new ObjectInputStream(bais).readObject();
        }
        catch (Exception e) {
            System.out.println("couldn't copy object");
            e.printStackTrace();
        }
        return ind;
    }
    */

    public static void printArrayAsMatrix(int[] array) {
        String s = "";
        String r;
        r = "";
        for (int i = 0; i < array.length; i++) {
            if (i %LoadImage.imageWidth == 0) {
                s += r + "\n";
                r = "";
            }
            r += array[i]+ " ";
        }
        s += r + "\n";
        System.out.println(s);
    }

}
