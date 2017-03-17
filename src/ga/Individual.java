package ga;

/**
 * Created by johan on 14/03/17.
 */
public class Individual {

    int[] genoType;
    int[][][] imageMatrix;

    public Individual(int[][][] imageMatrix, int numberOfSegments) {
        this.imageMatrix = imageMatrix;
        int genoTypeSize = imageMatrix.length*imageMatrix[0].length;
        genoType = new int[genoTypeSize];

    }

}
