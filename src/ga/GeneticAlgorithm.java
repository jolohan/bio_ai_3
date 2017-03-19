package ga;

import java.util.ArrayList;

/**
 * Created by johan on 14/03/17.
 */
public class GeneticAlgorithm {

    Population population;

    public GeneticAlgorithm(int[][][] imageMatrix) {
        this.population = new Population(imageMatrix);
    }

    public Individual mainLoop() {

        return getIndividual(0);
    }

    public ArrayList<Individual> getIndividuals() {
        return this.population.getPopulation();
    }

    public Individual getIndividual(int index) {
        return getIndividuals().get(index);
    }

}
