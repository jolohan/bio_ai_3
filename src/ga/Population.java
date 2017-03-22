package ga;

import inputOutput.LoadImage;
import inputOutput.Main;

import java.util.*;

/**
 * Created by johan on 14/03/17.
 */
public class Population {

    ArrayList<Individual> population;

    public Population(LoadImage loadedImage, boolean emptyPopulation) {
        population = new ArrayList<>(Main.POPULATION_SIZE);
        if (emptyPopulation) {

        }
        else {
            generateRandomPopulation(Main.POPULATION_SIZE, loadedImage);
        }
    }

    void copyIndividualsToNewPopulation(ArrayList<Individual> individuals) {
        population = new ArrayList<>(individuals.size());
        for (int i = 0; i < individuals.size(); i++) {
            Individual ind = individuals.get(i);
            population.add(ind);
        }
    }

    ArrayList<Individual> getNonDominatedIndividuals() {
        ArrayList<Individual> nonDominated = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            Individual a = population.get(i);
            if (a.getFitness() < 1) {
                nonDominated.add(a);
            }
        }
        return nonDominated;
    }

    void updateScores() {
        for (int i = 0; i < population.size(); i++) {
            Individual ind = population.get(i);
            ind.updateScores();
        }
    }

    double getAverageFitness() {
        double totalFitness = 0;
        for (int i = 0; i < getPopulation().size(); i++) {
            totalFitness += getPopulation().get(i).getFitness();
        }
        double fitness = totalFitness/getPopulation().size();
        return fitness;
    }

    void updateFitnesses(Population otherPopulation) {
        for (int i = 0; i < population.size(); i++) {
            Individual a = population.get(i);
            double density1 = getDistanceToKthNearestNeighbour(
                    a, Main.Kth_NEAREST_NEIGHBOUR);
            double density2 = otherPopulation.getDistanceToKthNearestNeighbour(
                    a, Main.Kth_NEAREST_NEIGHBOUR);
            double density = (density1+density2)/2;
            density = 1 / (density + 2);
            int numberOfDominators = getNumberOfDominators(a);
            numberOfDominators += otherPopulation.getNumberOfDominators(a);
            double fitnessScore = density + numberOfDominators;
            a.updateFitness(fitnessScore);
            //System.out.println(a.getFitness());
        }
    }

    double getDistanceToKthNearestNeighbour(Individual a, int k) {
        List<Double> distances = getDistanceInObjectiveSpace(a);
        Collections.sort(distances);
        return distances.get(k);
    }

    private ArrayList<Double> getDistanceInObjectiveSpace(Individual a) {
        ArrayList<Double> distances = new ArrayList<>(population.size());
        for (int i = 0; i < population.size(); i++) {
            distances.add(getDistancesInObjectiveSpace(a, population.get(i)));
        }
        if (distances.size() < Main.Kth_NEAREST_NEIGHBOUR) {
            System.out.println("couldnt find object space neighbours");
        }
        return distances;
    }

    private double getDistancesInObjectiveSpace(Individual a, Individual b) {
        double distance = 0;
        for (int i = 0; i < Main.WHICH_SCORES.length; i++) {
            if (Main.WHICH_SCORES[i]) {
                distance += Math.pow(a.getScores()[i]-b.getScores()[i], 2);
            }
        }
        return Math.sqrt(distance);
    }

    int getNumberOfDominators(Individual a) {
        int numberOfDominators = 0;
        double[] aScores = a.getScores();
        boolean dominated;
        for (int i = 0; i < population.size(); i++) {
            Individual b = population.get(i);
            double[] bScores = b.getScores();
            dominated = true;
            for (int j = 0; j < Main.WHICH_SCORES.length; j++) {
                if (Main.WHICH_SCORES[j]){
                    if (aScores[j] < bScores[j]) {
                        dominated = false;
                        break;
                    }
                }
            }
            if (dominated) { numberOfDominators ++; }
        }
        return numberOfDominators;
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

    private double getAverageScore(int whichScore) {
        double averageScore = 0;
        for (int i = 0; i < population.size(); i++) {
            Individual a = population.get(i);
            averageScore += a.getScores()[whichScore];
        }
        return averageScore/population.size();
    }

    // print average fitness, and average scores
    public String toString() {
        String s = "\nAverage fitness: " + getAverageFitness();
        s += "\nAverage overall deviation: " + getAverageScore(0);
        s += "\nAverage edge value: " + getAverageScore(1);
        s += "\nAverage connection: " + getAverageScore(2);
        s += "\n";

        return s;
    }

}
