package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.api.ga.AbstractPopulation;
import cz.cvut.fit.zum.data.StateSpace;
import cz.cvut.fit.zum.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Population extends AbstractPopulation implements Runnable {
    private ArrayList<AbstractIndividual> tabuList;
    private Random rand;
    private int populationSize;
    private int MAX_LOAD;

    private int  notChangedFor;
 //   private Pair<Double, Double> avgFitness;


    Population(AbstractEvolution evolution, int size) {
        tabuList = new ArrayList<>();
        rand = new Random();
        individuals = new Individual[size];
        populationSize = evolution.getPopulationSize();
        MAX_LOAD = 10*populationSize;
        notChangedFor = 0;

        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual(evolution, true);
            individuals[i].computeFitness();
        }
    }

    @Override
    public void run(){

    }

    @Override
     public AbstractIndividual getIndividual(int idx){
        return individuals[idx];  
     }

     void addNotChangedFor(){
         notChangedFor++;
     }
     int getNotChangedFor(){
         return notChangedFor;
     }
     void resetNotChangedFor(){
         notChangedFor = 0;
     }

     void Exterminate(AbstractEvolution evolution) {
         sortByFitness();
         for (int i = 0; i < (int)(individuals.length*0.9); i++) {
             individuals[i] = new Individual(evolution, true);
             individuals[i].computeFitness();
         }
     }



     AbstractIndividual SelectRandIdnividual() {
         return individuals[rand.nextInt(populationSize)];
     }

    AbstractIndividual WithdrawIndividual() {
        int random = rand.nextInt(populationSize);
        AbstractIndividual x = individuals[random].deepCopy();
        ((Individual)individuals[random]).SubstitueWithNewRandom();
        return x;
    }
    AbstractIndividual SelectElite() {
        sortByFitness();
        return individuals[populationSize-1-(rand.nextInt(31)%15)];
    }

     void addToTabuList(AbstractIndividual other) {
         tabuList.add(other);
         if(tabuList.size() > MAX_LOAD)
             tabuList.remove(0);
     }

     boolean isInTabuList(AbstractIndividual other) {
        for( AbstractIndividual individual : tabuList){
            if( ((Individual)individual).distFromOther(other) < 50 ) {
                return true;
            }
        }
         return false;
     }

     List<AbstractIndividual> selectRoulette(int count) {
        ArrayList<AbstractIndividual> selected = new ArrayList<>();
        long fitnessSum = 0;
        for (AbstractIndividual individual : individuals)
            fitnessSum += individual.getFitness();
        for (int i = 0; i < count; ++i) {
            double value = rand.nextDouble() * fitnessSum;
            for (AbstractIndividual individual : individuals) {
                value -= individual.getFitness();
                if (value <= 0) {
                    selected.add(individual);
                    break;
                }
            }
        }
        return selected;
    }

    List<AbstractIndividual> selectTournament(int count) {
        ArrayList<AbstractIndividual> selected = new ArrayList<>();
        int tournamentSensitivity = 2;
        for(int i=0; i<count;++i){
            AbstractIndividual best = SelectRandIdnividual();
            for(int j=0; j < tournamentSensitivity; ++j){
                AbstractIndividual other = SelectRandIdnividual();
                if(other.getFitness() > best.getFitness())
                    best = other;
            }
            selected.add(best);
        }
        return selected;
    }
}

