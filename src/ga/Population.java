package ga;

import inputOutput.LoadImage;
import inputOutput.Main;

import java.util.ArrayList;

/**
 * Created by johan on 14/03/17.
 */
public class Population {

    ArrayList<Individual> population;

    public Population(LoadImage loadedImage) {
        population = new ArrayList<>(Main.POPULATION_SIZE);
        generateRandomPopulation(Main.POPULATION_SIZE, loadedImage);
    }

    void updateFitness() {
        for (int i = 0; i < population.size(); i++) {
            Individual ind = population.get(i);
            //ind.updateFitness();
        }
    }

    private void generateRandomPopulation(int populationSize,
                                          LoadImage loadedImage) {
        for (int i = 0; i < populationSize; i++) {
            String s = String.format("Generating individual # %d", i+1);
            System.out.println(s);
            Individual a = new Individual(loadedImage);
            population.add(a);
        }
    }

    public ArrayList<Individual> getPopulation() {
        return this.population;
    }

    public void addIndividuals(ArrayList<Individual> inds) {
        population.addAll(inds);
    }

}
