package ga;

import java.util.Comparator;

/**
 * Created by johan on 22/03/17.
 */
public class IntComp implements Comparator<Integer>{

    public int compare(Integer a, Integer b) {
        return a-b;
    }
}