package ga;

import java.util.Comparator;

/**
 * Created by johan on 21/03/17.
 */
public class FitnessComparator implements Comparator<Individual>{

    public int compare(Individual a, Individual b) {
        double value = a.getFitness() - b.getFitness();
        if (value > 0) { return 1; }
        else if (value < 0) { return -1; }
        else { return 0; }
    }
}
