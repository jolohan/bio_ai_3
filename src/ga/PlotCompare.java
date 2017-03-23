package ga;

import inputOutput.Main;

import java.util.Comparator;

/**
 * Created by johan on 23/03/17.
 */
public class PlotCompare implements Comparator<Individual> {

    public int compare(Individual a, Individual b) {
        for (int i = 0; i < Main.WHICH_SCORES.length; i++) {
            int value = (int) ((b.getScores()[i] -
                    a.getScores()[i])*100);
            if (Main.WHICH_SCORES[i]) {
                return value;
            }
        }
        return 0;
    }
}
