package ga;

import inputOutput.LoadImage;
import inputOutput.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

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
        Scanner reader = new Scanner(System.in);
        Individual bestInd = findBestInd();
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
            if ((i)%10 == 0) {
                bestInd = findBestInd();
                output(loadImage);
                System.out.println("Number of segments: "
                        +bestInd.findNumberOfSegments());
                System.out.println("press c to continue and q to quit");
                //if (reader.next().equals("q")) { break; }
            }
        }
        System.out.println("DONE");
        return findBestInd();
    }

    private void output(LoadImage loadImage) {
        List<Individual> sortedJoined = getSortedJoinedPopulation();
        ArrayList<Individual> front = new ArrayList<>(5);
        int counter = 0;
        Individual a = sortedJoined.get(counter);
        front.add(a);
        while (counter < 5 && a.getFitness() < 1) {
            counter++;
            a = sortedJoined.get(counter);
            front.add(a);
        }

        Individual bestInd = sortedJoined.get(0);
        writeToFile(front);
        Main.colorBigAndWrite(loadImage, 1, bestInd);
        Main.colorBigAndWrite(loadImage, 2, bestInd);
        try {
            String filePath = new File("").getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder(
                    filePath+"/myshellScript.sh",
                    ""+Main.IMAGE_NUMBER, ""+bestInd.findNumberOfSegments());
            System.out.println("starting shell script");
            Process p = pb.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFitness() {
        this.population.updateScores();
        this.archivePopulation.updateScores();
        this.population.updateFitnesses(archivePopulation);
        this.archivePopulation.updateFitnesses(population);
    }

    private List<Individual> getSortedJoinedPopulation() {
        List<Individual> joined = getJoinedPopulations();
        Collections.sort(joined, new FitnessComparator());
        return joined;
    }


    private Individual findBestInd() {
        List<Individual> joined = getSortedJoinedPopulation();
        Individual bestInd = joined.get(0);
        return bestInd;
    }

    private void writeToFile(ArrayList<Individual> individuals) {
        BufferedWriter writer = null;
        try {
            //create a temporary file
            String s = ""+Main.IMAGE_NUMBER;
            String[] r = new String[4];
            if (Main.WHICH_SCORES[0]) {
                s += "-" + "dev";
                r[0] = "overallDeviation";
            }
            if (Main.WHICH_SCORES[1]) {
                s += "-" + "edge";
                r[1] = "edgeValues";
            }
            if (Main.WHICH_SCORES[2]) {
                s += "-" + "con";
                r[2] = "connection";
            }
            r[3] = "number of segments";
            File f = new File(s+".txt");
            writer = new BufferedWriter(new FileWriter(f));
            s = "";
            for (int i = 0; i < Main.WHICH_SCORES.length; i++) {
                if (Main.WHICH_SCORES[i]) {
                    s += r[i] +"    ";
                }
            }
            s = s.trim();
            for (int i = 0; i < individuals.size(); i++) {
                s += "\n";
                for (int j = 0; j < Main.WHICH_SCORES.length; j++) {
                    if (Main.WHICH_SCORES[j]) {
                        s += individuals.get(i).getScores()[j];
                        s += "  ";
                    }
                }
                s += individuals.get(i).getNumberOfSegments();
            }

            writer.write(s);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
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

    private void mutation(Population population) {
        ArrayList<Individual> inds = population.getPopulation();
        for (int i = 0; i < inds.size(); i++) {
            mutateIndividual(inds.get(i));
        }
    }

    private void mutateIndividual(Individual individual) {
        int estimatedMutations = (int) (genoTypeSize*Main.MUTATION_RATE);
        int counter = 0;
        while (Math.random() > 1.0/estimatedMutations) {
            int geneIndex = (int) (Math.random()*genoTypeSize);
            individual.mutateGene(geneIndex);
            counter ++;
        }
    }
    
    private ArrayList<Individual> crossover(Population population) {
        ArrayList<Individual> inds = population.getPopulation();
        ArrayList<Individual> children = new ArrayList<>(inds.size());
        for (int i = 0; i < inds.size(); i++) {
            Individual father = inds.get(i);
            int motherIndex = (int) (Math.random()*inds.size());
            Individual mother = inds.get(motherIndex);
            if (mother != father) {
                Individual child = getChild(father, mother);
                children.add(child);
            }
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

    private boolean isSame(Individual a, Individual b) {
        for (int i = 0; i < a.getGenoType().length; i++) {
            int aGene = a.getGenoType()[i];
            int bGene = b.getGenoType()[i];
            if (aGene != bGene) {
                return false;
            }
        }
        return true;
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
