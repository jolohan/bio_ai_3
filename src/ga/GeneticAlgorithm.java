package ga;

import inputOutput.Main;

import java.util.ArrayList;

public class GeneticAlgorithm {

    Population population;
    private final int genoTypeSize;

    public GeneticAlgorithm(int[][][] imageMatrix) {
        this.population = new Population(imageMatrix);
        genoTypeSize = getIndividual(0).getGenoType().length;
    }

    public Individual mainLoop() {

        for (int i = 0; i < Main.NUMBER_OF_GENERATIONS; i++) {
            population.updateFitness();

            ArrayList<Individual> children = crossover();
            this.population.addIndividuals(children);
            mutation();
        }
        return getIndividual(0);
    }

    private void mutation() {
        int numberOfIndividualsToMutate = getIndividuals().size();
        for (int i = 0; i < numberOfIndividualsToMutate; i++) {
            mutateIndividual(getIndividual(i));
        }
    }

    private void mutateIndividual(Individual individual) {
        int estimatedMutations = (int) (genoTypeSize*Main.MUTATION_RATE);
        while (Math.random() > 1.0/estimatedMutations) {
            int geneIndex = (int) (Math.random()*genoTypeSize);
            individual.mutateGene(geneIndex);
        }
    }
    
    private ArrayList<Individual> crossover() {
        int numberOfIndividuals = getIndividuals().size();
        ArrayList<Individual> children = new ArrayList<>(numberOfIndividuals);
        for (int i = 0; i < numberOfIndividuals; i++) {
            Individual father = getIndividual(i);
            int motherIndex = (int) (Math.random()*numberOfIndividuals);
            Individual mother = getIndividual(motherIndex);
            Individual child = getChild(father, mother);
            children.add(child);
        }
        return children;
    }

    private Individual getChild(Individual father, Individual mother) {
        int[] childGenoType = new int[genoTypeSize];
        for (int i = 0; i < genoTypeSize; i++) {
            if (Math.random() < Main.CROSSOVER_RATE) {
                childGenoType[i] = father.getGenoType()[i];
            }
            else { childGenoType[i] = mother.getGenoType()[i]; }
        }
        Individual child = new Individual(father.getImageMatrix(), childGenoType);
        return child;    
    }

    private ArrayList<Individual> getIndividuals() {
        return this.population.getPopulation();
    }

    private Individual getIndividual(int index) {
        return getIndividuals().get(index);
    }

}
