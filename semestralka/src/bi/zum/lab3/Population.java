package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.api.ga.AbstractPopulation;
import cz.cvut.fit.zum.data.StateSpace;
import cz.cvut.fit.zum.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.LinkedHashSet;

public class Population extends AbstractPopulation implements Runnable {
  //  private ArrayList<AbstractIndividual> tabuList;
    private LinkedHashSet<AbstractIndividual> betterTabuList;
    private Random rand;
    private int populationSize;
    private int MAX_LOAD;

    private int  notChangedFor;
    int  tempIncreasingFor;
    boolean loveringTemperature ;
 //   private Pair<Double, Double> avgFitness;


    
    
    
    Population(AbstractEvolution evolution, int size) {
        //tabuList = new ArrayList<>();
        betterTabuList = new LinkedHashSet<>();
        rand = new Random();
        individuals = new Individual[size];
        populationSize = evolution.getPopulationSize();
        MAX_LOAD = 10*populationSize;
        notChangedFor = 0;
        tempIncreasingFor = 0;
        loveringTemperature = false;

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
   void incTempIncreasing(){
       loveringTemperature = false;
         tempIncreasingFor++;
         if(tempIncreasingFor == 5)
             loveringTemperature = true;
     }
   
      void decTemIncreasing(){
         tempIncreasingFor--;
         loveringTemperature = true;
         if(tempIncreasingFor == 0)
             loveringTemperature = false;
     }
      
      boolean GetTemperatureStatus()
      {
          return loveringTemperature;
      }
     
     
     /// VYHLAD POPULACI NECH 1% NEJLEPSICH
     void Exterminate(AbstractEvolution evolution) {
         sortByFitness();
         for (int i = 0; i < (int)(individuals.length*0.9); i++) {
             individuals[i] = new Individual(evolution, true);
             individuals[i].computeFitness();
         }
     }
     
     
     Double GetWorstFitness() {
         sortByFitness();
         return individuals[0].getFitness();
     }
     
     Double GetAVGFitness() {
        sortByFitness();
        double sum = 0d;
        
         for (int i = 0; i < (int)(individuals.length); i++) 
             sum += individuals[i].getFitness() ; 
         
         return  sum / individuals.length  ;   
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
    
    
    ///VYBER TY LEPSEJSI JEDINCE ( VEDLO K EXTREMNI KONVERGENCI, NEPOUZIVAM )
    AbstractIndividual SelectElite() {
        sortByFitness();
        return individuals[populationSize-1-(rand.nextInt(31)%15)];
    }

    
    
    /// POLULACNI TABU ---- ( Vedlo to k EXTREMNE pomalemu , behu algoritmu /diky neustalemu porovanavani/ a ani neneslo ovoce. PRoto prakticky nevyuzivam
     void addToTabuList(AbstractIndividual other) {
        // tabuList.add(other);
         betterTabuList.add(other);
         if(betterTabuList.size() > MAX_LOAD){
             Iterator<AbstractIndividual> it = betterTabuList.iterator();
             it.next();
             it.remove();
          
         }
     }
     /// POLULACNI TABU
     boolean isInTabuList(AbstractIndividual other) {
        for( AbstractIndividual individual : betterTabuList){
            if( ((Individual)individual).distFromOther(other) < 50 ) {
                return true;
            }
        }
         return false;
     }

     
     
     /// roulette selection sucks for some reason
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
     
     
     ///tournamet  ve vetsine pripadu sice mozna o neco rychleji dokonverguje
     /// ale zato na lepsi vysledek
     /// pravdepodobne dano tim, ze v tournamentu se pozna lepsi x horsi i kdyz jsou jedinci odne podbni
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

