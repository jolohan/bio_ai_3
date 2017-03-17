package ga;

import main.Main;

import java.util.ArrayList;

/**
 * Created by johan on 14/03/17.
 */
public class Individual {

    int[] genoType;
    int[][][] imageMatrix;
    int imageHeight;
    int imageWidth;

    public Individual(int[][][] imageMatrix) {
        this.imageMatrix = imageMatrix;
        this.imageHeight = imageMatrix.length;
        this.imageWidth = imageMatrix[0].length;
        int genoTypeSize = imageHeight*imageWidth;
        this.genoType = new int[genoTypeSize];
        initIndividual();
    }

    private void initIndividual() {
        regionGrowing();
    }

    void regionGrowing() {
        int[][] visited = new int[imageMatrix.length][imageMatrix[0].length];
        int i = 0;
        int[] lastSeedPoint = null;
        int[] seedPoint;
        while (true) {
            seedPoint = getSeedPoint(visited, lastSeedPoint);
            if (seedPoint[0] == -1) {
                System.out.println("Done with all regions");
                break;
            }

            lastSeedPoint = seedPoint;
            growSingleRegion(visited, seedPoint);

            //if (i > 1000) { break; }
            //else { i ++; }
        }
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
            ArrayList<Integer> neighbours = getAdjacentNeighbours(pixel, visited);
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

    }

    private void addToVisited(int[][] visited, int pixel) {
        int[] nRowCol = getRowCol(pixel);
        visited[nRowCol[0]][nRowCol[1]] = 1;
    }

    private void editGenoType(int from, int to) {
        this.genoType[from] = to;
    }

    private boolean isSimilar(int pixel1, int pixel2) {
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
            double colorDelta = Math.pow(pixel1[i] - pixel2[i], 2);
            answer += colorDelta;
        }
        return Math.sqrt(answer);
    }

    private ArrayList<Integer> getAdjacentNeighbours(int pixel, int[][] visited) {
        ArrayList<Integer> neighbours = new ArrayList<>();
        int row = getRowCol(pixel)[0];
        int col = getRowCol(pixel)[1];
        int newPixel;
        if (row != 0) {
            int newRow = row -1;
            newPixel = getRowCol(newRow, col);
            if (! isVisited(newPixel, visited)) {
                neighbours.add(newPixel);
            }
        }
        if (row != imageHeight -1) {
            int newRow = row +1;
            newPixel = getRowCol(newRow, col);
            if (! isVisited(newPixel, visited)) {
                neighbours.add(newPixel);
            }
        }
        if (col != 0) {
            int newCol = col -1;
            newPixel = getRowCol(row, newCol);
            if (! isVisited(newPixel, visited)) {
                neighbours.add(newPixel);
            }
        }
        if (col != imageWidth -1) {
            int newCol = col +1;
            newPixel = getRowCol(row, newCol);
            if (! isVisited(newPixel, visited)) {
                neighbours.add(newPixel);
            }
        }
        return neighbours;
    }

    private boolean isVisited(int rowCol, int[][] visited) {
        int[] coordinates = getRowCol(rowCol);
        int row = coordinates[0];
        int col = coordinates[1];
        if (visited[row][col] == 1) { return true; }
        else { return false; }
    }

    int getRowCol(int row, int col) {
        return row*imageWidth + col;
    }

    int[] getRowCol(int rowCol) {
        int[] coordinates = new int[2];
        coordinates[0] = rowCol/imageWidth;
        coordinates[1] = rowCol%imageWidth;
        return coordinates;
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < imageHeight; i ++) {
            String r = "";
            for (int j = 0; j < imageWidth; j++) {
                int index = getRowCol(i, j);
                r += genoType[index] + ", ";
            }
            s += r + "\n";
        }
        return s;
    }

}
