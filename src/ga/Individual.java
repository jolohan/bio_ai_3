package ga;

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

    public final int[][][] imageMatrix;
    private int[] genoType;
    private int[][] segmenation;
    private int[][] listOfSegments;
    private int[] edgePixels;

    public Individual(int[][][] imageMatrix, int[] genoType) {
        this.imageMatrix = imageMatrix;
        this.genoType = genoType;
    }

    public Individual(int[][][] imageMatrix) {
        this.imageMatrix = imageMatrix;
        this.imageHeight = imageMatrix.length;
        this.imageWidth = imageMatrix[0].length;
        int genoTypeSize = imageHeight*imageWidth;
        this.genoType = new int[genoTypeSize];
        initIndividual();
    }

    void updateRepresentations() {
        segmenation = makeReadableSegmenation();
        listOfSegments = makeListOfSegments();
    }

    private void initIndividual() {
        regionGrowing();
        makeReadableSegmenation();
    }

    void regionGrowing() {
        int[][] visited = new int[imageMatrix.length][imageMatrix[0].length];
        int i = 0;
        int[] lastSeedPoint = null;
        int[] seedPoint;
        while (true) {
            seedPoint = getSeedPoint(visited, lastSeedPoint);
            if (seedPoint[0] == -1) {
                //System.out.println("Done with all regions");
                break;
            }

            lastSeedPoint = seedPoint;
            growSingleRegion(visited, seedPoint);

            //if (i > 1000) { break; }
            //else { i ++; }
        }
        //System.out.println(this);
    }

    private int[] getSeedPoint(int[][] visited, int[] lastSeedPoint) {
        int[] seedPoint = new int[2];
        if (lastSeedPoint == null) {
            seedPoint[0] = 0;
            seedPoint[1] = 0;
            return seedPoint;
        }
        int row = lastSeedPoint[0];
        int col = lastSeedPoint[1];
        while (visited[row][col] == 1) {
            if (col < imageWidth -1) {
                col ++;
            }
            else {
                if (row == imageHeight -1) {
                    seedPoint[0] = -1;
                    seedPoint[1] = -1;
                    return seedPoint;
                }
                else {
                    col = 0;
                    row ++;
                }
            }
        }
        seedPoint[0] = row;
        seedPoint[1] = col;
        return seedPoint;
    }

    private void growSingleRegion(int[][] visited, int[] seedPoint) {
        int row = seedPoint[0];
        int col = seedPoint[1];
        int seedPointRowCol = getRowCol(row, col);
        ArrayList<Integer> q = new ArrayList<>();
        q.add(seedPointRowCol);
        while (! q.isEmpty()) {
            int pixel = q.remove(q.size()-1);
            addToVisited(visited, pixel);
            ArrayList<Integer> neighbours = getAdjacentNeighbours(pixel, visited, true);
            double rnd = Math.random();
            if (rnd < 1 - Main.INIT_RANDOMNESS) {
                int i = 0;
                while (i < neighbours.size()) {
                    int neighbour = neighbours.get(i);
                    if (isSimilar(seedPointRowCol, neighbour)) {
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
                        || getDistanceInColors(seedPointRowCol,
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

    private void addToVisited(int[][] visited, int pixel) {
        int[] nRowCol = getRowCol(pixel);
        visited[nRowCol[0]][nRowCol[1]] = 1;
    }

    private void editGenoType(int from, int to) {
        this.genoType[from] = to;
    }

    private boolean isSimilar(int pixel1, int pixel2) {
        //System.out.println(getDistanceInColors(pixel1,pixel2));
        return getDistanceInColors(pixel1, pixel2) < Main.THRESHHOLD;
    }

    public void print2DMatrix(int[][] matrix) {
        String s = "";
        for (int i = 0; i < matrix.length; i ++) {
            String r = "";
            int[] row = matrix[i];
            for (int j = 0; j < row.length; j++) {
                r += row[j] + ", ";
            }
            s += r + "\n";
        }
        System.out.println(s);
    }

    public double getDistanceInColors(int p1, int p2) {
        int row1 = getRowCol(p1)[0];
        int col1 = getRowCol(p1)[1];
        int row2 = getRowCol(p2)[0];
        int col2 = getRowCol(p2)[1];
        int[] pixel1 = imageMatrix[row1][col1];
        int[] pixel2 = imageMatrix[row2][col2];
        double answer = 0;
        for (int i = 0; i < pixel1.length; i++) {
            //System.out.println(pixel1[i]+"  "+pixel2[i]);
            double colorDelta = Math.pow(pixel1[i] - pixel2[i], 2);
            answer += colorDelta;
        }
        return Math.sqrt(answer);
    }

    // TATT UTGANGSPUNKT I SNAKE. MÅ ENDRES
    public int[][] makeReadableSegmenation() {
        int[][] readableMatrix = new int[imageHeight][imageWidth];
        int[][] visited = new int[imageHeight][imageWidth];
        int segmentNumber = 1;
        for (int i = 0; i < imageHeight; i++) {
            for (int j = 0; j < imageWidth; j++) {
                int pixel = getRowCol(i, j);
                int pointer = genoType[pixel];
                if (isVisited(pointer, readableMatrix)) {
                    int pointerSegmentNumber = getValueFromMatrix(
                            pointer, readableMatrix);
                    insertValueIntoMatrix(readableMatrix, pointerSegmentNumber, pixel);
                }
                while (! isVisited(pixel, readableMatrix)) {
                    insertValueIntoMatrix(readableMatrix, segmentNumber, pixel);
                    pixel = pointer;
                    pointer = genoType[pixel];
                }

                if (! isVisited(pixel, visited)) {
                    int pointer = genoType[pixel];
                    if (pixel == 0) {
                        //System.out.println(isVisited(pixel, visited));
                    }
                    while (! isVisited(pointer, visited)) {
                        pixel = pointer;
                        pointer = genoType[pixel];
                        insertValueIntoMatrix(readableMatrix, segmentNumber, pixel);
                        insertValueIntoMatrix(visited, 1, pixel);
                    }
                    segmentNumber ++;
                }
            }
        }
        //print2DMatrix(readableMatrix);
        return readableMatrix;
    }

    private int[][] makeListOfSegments() {
        ArrayList<ArrayList> 
    }

    private void insertValueIntoMatrix(int[][] matrix, int value, int index) {
        int[] rowCol = getRowCol(index);
        int row = rowCol[0];
        int col = rowCol[1];
        //System.out.println(matrix[row][col]);
        matrix[row][col] = value;
        //System.out.println(matrix[row][col]);
    }


    public int[] getGenoType() {
        return genoType;
    }

    public int[][][] getImageMatrix() {
        return imageMatrix;
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
        int[][] matrix = makeReadableSegmenation();
        String s = "";
        for (int i = 0; i < matrix.length; i ++) {
            String r = "";
            int[] row = matrix[i];
            for (int j = 0; j < row.length; j++) {
                r += row[j] + " ";
            }
            r = r.trim();
            s += r + "\n";
        }
        return s;
    }

    // GA operations
    //=================================================================================

    void mutateGene(int index) {
        ArrayList<Integer> neighbours = getAdjacentNeighbours(
                index, new int[1][1], false);
        int newIndex = (int) (Math.random()*neighbours.size());
        genoType[index] = neighbours.get(newIndex);
    }



    //=================================================================================

    static int getRowCol(int row, int col) {
        return row*imageWidth + col;
    }

    static int[] getRowCol(int rowCol) {
        int[] coordinates = new int[2];
        coordinates[0] = rowCol/imageWidth;
        coordinates[1] = rowCol%imageWidth;
        return coordinates;
    }

    private static ArrayList<Integer> getAdjacentNeighbours(int pixel, int[][] visited,
                                                            boolean b) {
        ArrayList<Integer> neighbours = new ArrayList<>();
        int row = getRowCol(pixel)[0];
        int col = getRowCol(pixel)[1];
        int newPixel;
        if (row != 0) {
            int newRow = row - 1;
            newPixel = getRowCol(newRow, col);
            if (! b || !isVisited(newPixel, visited)) {
                neighbours.add(newPixel);
            }

        }
        if (row != imageHeight - 1) {
            int newRow = row + 1;
            newPixel = getRowCol(newRow, col);
            if (! b || !isVisited(newPixel, visited)) {
                neighbours.add(newPixel);
            }
        }
        if (col != 0) {
            int newCol = col - 1;
            newPixel = getRowCol(row, newCol);
            if (! b || !isVisited(newPixel, visited)) {
                neighbours.add(newPixel);
            }
        }
        if (col != imageWidth - 1) {
            int newCol = col + 1;
            newPixel = getRowCol(row, newCol);
            if (! b || !isVisited(newPixel, visited)) {
                neighbours.add(newPixel);
            }
        }
        //Collections.shuffle(neighbours);
        return neighbours;
    }

    private static boolean isVisited(int rowCol, int[][] visited) {
        int[] coordinates = getRowCol(rowCol);
        int row = coordinates[0];
        int col = coordinates[1];
        if (visited[row][col] == 0) return false;
        return true;
    }

    public static boolean isEdgePixel(int[][] readableSegmentation, int row, int col) {
        int pixel = getRowCol(row, col);
        ArrayList<Integer> neighbours = getAdjacentNeighbours(
                pixel, new int[1][1], false);
        for (int neighbour : neighbours) {
            if (isDifferentSegments(readableSegmentation, pixel, neighbour)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDifferentSegments(int[][] readableSegmentation,
                                        int pixel1, int pixel2) {
        int row1 = getRowCol(pixel1)[0];
        int col1 = getRowCol(pixel1)[1];
        int row2 = getRowCol(pixel2)[0];
        int col2 = getRowCol(pixel2)[1];

        return (readableSegmentation[row1][col1]
                != readableSegmentation[row2][col2]);
    }

    private static int getValueFromMatrix(int rowCol, int[][] matrix) {
        int row = getRowCol(rowCol)[0];
        int col = getRowCol(rowCol)[1];
        return matrix[row][col];
    }

}
