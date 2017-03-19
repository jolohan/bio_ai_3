package ga;

import inputOutput.Main;

import java.util.ArrayList;

/**
 * Created by johan on 14/03/17.
 */
public class Population {

    ArrayList<Individual> population;

    public Population(int[][][] imageMatrix) {
        population = new ArrayList<>(Main.POPULATION_SIZE);
        generateRandomPopulation(Main.POPULATION_SIZE, imageMatrix);
    }

    private void generateRandomPopulation(int populationSize,
                                          int[][][] imageMatrix) {
        for (int i = 0; i < populationSize; i++) {
            String s = String.format("Generating individual # %d", i+1);
            System.out.println(s);
            Individual a = new Individual(imageMatrix);
            population.add(a);
        }
    }

    public ArrayList<Individual> getPopulation() {
        return this.population;
    }

}
