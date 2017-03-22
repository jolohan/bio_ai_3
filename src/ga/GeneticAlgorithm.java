package ga;

import inputOutput.LoadImage;
import inputOutput.Main;

import java.util.*;

public class GeneticAlgorithm {

    private Population population;
    private Population archivePopulation;
    private final int genoTypeSize;
    private final LoadImage loadImage;

    public GeneticAlgorithm(LoadImage loadedImage) {
        loadImage = loadedImage;
        this.population = new Population(
                loadImage, true);
        this.archivePopulation = new Population(
                loadImage, false);
        genoTypeSize = getIndividual(
                archivePopulation, 0).getGenoType().length;
    }

    public Individual mainLoop() {
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        for (int i = 0; i < Main.NUMBER_OF_GENERATIONS; i++) {


            // check if copy works


            ArrayList<Individual> children = crossover(archivePopulation);
            //System.out.println("crossover finished # "+ i);
            this.population = new Population(loadImage, true);
            this.population.copyIndividualsToNewPopulation(children);
            mutation(population);
            //System.out.println("mutated # "+ i);
            updateFitness();
            System.out.println(i+"  "+archivePopulation);
            selection();
            if ((i+1)%10 == 0) {
                System.out.println("press space to continue and q to quit");
                if (reader.next().equals("q")) {
                    break;
                }
            }
        }
        System.out.println("DONE");
        return this.archivePopulation.getPopulation().get(0);
    }

    private void updateFitness() {
        this.population.updateScores();
        this.archivePopulation.updateScores();
        this.population.updateFitnesses(archivePopulation);
        this.archivePopulation.updateFitnesses(population);
    }

    private void selection() {
        Population nextArchivedPopulation =
                new Population(loadImage,true);
        List<Individual> newIndividuals = getJoinedPopulations();
        Collections.sort(newIndividuals, new FitnessComparator());
        newIndividuals = newIndividuals.subList(0, Main.POPULATION_SIZE);
        ArrayList<Individual> inds = new ArrayList<>(Main.POPULATION_SIZE);
        inds.addAll(newIndividuals);
        nextArchivedPopulation.copyIndividualsToNewPopulation(inds);
        this.archivePopulation = nextArchivedPopulation;
    }

    private ArrayList<Individual> getNonDominatedIndividuals() {
        ArrayList<Individual> nonDominated = new ArrayList<>();
        nonDominated.addAll(this.population.getNonDominatedIndividuals());
        nonDominated.addAll(this.archivePopulation.getNonDominatedIndividuals());
        return nonDominated;
    }

    private void mutation(Population population) {
        ArrayList<Individual> inds = population.getPopulation();
        for (int i = 0; i < inds.size(); i++) {
            mutateIndividual(inds.get(i));
        }
    }

    private void mutateIndividual(Individual individual) {
        int estimatedMutations = (int) (genoTypeSize*Main.MUTATION_RATE);
        while (Math.random() > 1.0/estimatedMutations) {
            int geneIndex = (int) (Math.random()*genoTypeSize);
            individual.mutateGene(geneIndex);
        }
    }
    
    private ArrayList<Individual> crossover(Population population) {
        ArrayList<Individual> inds = population.getPopulation();
        ArrayList<Individual> children = new ArrayList<>(inds.size());
        for (int i = 0; i < inds.size(); i++) {
            Individual father = inds.get(i);
            int motherIndex = (int) (Math.random()*inds.size());
            Individual mother = inds.get(motherIndex);
            Individual child = getChild(father, mother);
            children.add(child);
        }
        return children;
    }

    private Individual getChild(Individual father, Individual mother) {
        Individual child = father.makeCopy(loadImage.getImageArray());
        for (int i = 0; i < genoTypeSize; i++) {
            if (Math.random() > Main.CROSSOVER_RATE) {
                child.editGeneType(i, mother.getGenoType()[i]);
            }
        }
        return child;    
    }

    private Individual getIndividual(Population pop, int index) {
        return pop.getPopulation().get(index);
    }

    private List<Individual> getJoinedPopulations() {
        List<Individual> joinedPop = new ArrayList<>(
                population.getPopulation().size()
                        +archivePopulation.getPopulation().size());
        joinedPop.addAll(population.getPopulation());
        joinedPop.addAll(archivePopulation.getPopulation());
        return joinedPop;
    }

}
