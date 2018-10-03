package bi.zum.lab3;

import cz.cvut.fit.zum.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.data.StateSpace;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Your name
 */
@ServiceProvider(service = AbstractEvolution.class)
public class Evolution extends AbstractEvolution<Individual> implements Runnable {

    /**
     * start and final average fitness
     */
    private Pair<Double, Double> avgFitness;
    /**
     * start and final best fitness in whole population
     */
    private Pair<Double, Double> bestFitness;
    /**
     * start and final time
     */
    private Pair<Long, Long> time;
    /**
     * How often to print status of evolution
     */
    private int debugLimit = 100;
    private Random rand = new Random();
    
    
    Population [] listOfPopulations;
    
    private Set<boolean[]> blackList = new HashSet<>();
    /**
     * The population to be used in the evolution
     */
    Population population;

    public Evolution() {
        isFinished = false;
        avgFitness = new Pair<>();
        bestFitness = new Pair<>();
        time = new Pair<>();     
        
        
    }

    @Override
    public String getName() {
        return "Skynet ver.666";
    }
    
    
    int compareDifference( AbstractIndividual offspring, AbstractIndividual parrent ) {
        int numMatches = 0;
        for(int i = 0 ; i < StateSpace.nodesCount() ; ++i)
            if( (offspring.isNodeSelected(i) &&  parrent.isNodeSelected(i)) ||  ( ! offspring.isNodeSelected(i) &&  ! parrent.isNodeSelected(i) ))
                   numMatches++;
        return numMatches;
    }
    
    
    void processOnePopulation(int populNum, int g) {  
         Random random = new Random();
        
        // initialize the next generation's population
            ArrayList<AbstractIndividual> newInds = new ArrayList<AbstractIndividual>();
            
            // elitism: Preserve the best individual
   //         // (this is quite exploatory and may lead to premature convergence!)
            newInds.add(listOfPopulations[populNum].getBestIndividual().deepCopy());

            
            // keep filling the new population while not enough individuals in there
            while(newInds.size() < populationSize) {
                
                // select 2 parents
                //with some probabily get parents from diffenrent isle
                List<AbstractIndividual> parents = null;
                
       
                parents = listOfPopulations[populNum].selectIndividuals(2);
                
                //
                if( crossoverProbability < random.nextDouble() && 0 == g%30) {
                    int tmp = ((int)(rand.nextDouble()*100) )%listOfPopulations.length;
                    listOfPopulations[ ( tmp ) ].sortByFitness();
                    parents.set(0, listOfPopulations[ ( tmp ) ].getIndividual(populationSize -1 - (rand.nextInt(32)%10))) ;            
                    tmp = ((int)(rand.nextDouble()*100) + 1 )%listOfPopulations.length;
                    listOfPopulations[ ( tmp ) ].sortByFitness();
                    //parents.set(1, listOfPopulations[ ( tmp ) ].getIndividual(tmp%  populationSize )) ;
                     parents.set(1,listOfPopulations[ ( tmp ) ].getIndividual(populationSize -1 -(rand.nextInt(32)%10))) ;
                }
                
                
                Pair<AbstractIndividual,AbstractIndividual> offspring;
                
                // with some probability, perform crossover
                if(crossoverProbability < random.nextDouble()) {
                    offspring = parents.get(0).deepCopy().crossover(parents.get(1).deepCopy());
                }
                // otherwise, only copy the parents
                else {
                    offspring = new Pair<AbstractIndividual, AbstractIndividual>();
                    offspring.a = parents.get(0).deepCopy();
                    offspring.b = parents.get(1).deepCopy();
                }
                
                
                offspring.a.mutate(mutationProbability);
                offspring.a.computeFitness();
                
                offspring.b.mutate(mutationProbability);
                offspring.b.computeFitness();
                
                
                while(  blackList.contains (( (Individual) offspring.a ).getGenotype()  )  ) {
                    offspring.a.mutate(mutationProbability*3);
                offspring.a.computeFitness();
                }
                
                
                   
                while(  blackList.contains (( (Individual) offspring.b ).getGenotype()  )  ) {
                    offspring.b.mutate(mutationProbability*3);
                offspring.b.computeFitness();
                }
                
                //offspring1 is closer to first parrent1,  -> fight ofs1 parent1
                if( compareDifference(offspring.a ,  parents.get(0) )  > compareDifference(offspring.a ,  parents.get(1) )) {
                    
                    //the one with higher fitness wins
                    if(offspring.a.getFitness() > parents.get(0).getFitness())
                        newInds.add(offspring.a);
                    else 
                        newInds.add(parents.get(0));
                    
                    //if still make second match
                    if(newInds.size() < populationSize) {
                         if(offspring.b.getFitness() > parents.get(1).getFitness())
                            newInds.add(offspring.b);
                        else 
                            newInds.add(parents.get(1));
                    }
                }
                
                //else figt offspring1 and parent2
                else {                  
                    //the one with higher fitness wins
                    if(offspring.a.getFitness() > parents.get(1).getFitness())
                        newInds.add(offspring.a);
                    else 
                        newInds.add(parents.get(1));
                    
                    //if still make second match
                    if(newInds.size() < populationSize) {
                         if(offspring.b.getFitness() > parents.get(0).getFitness())
                            newInds.add(offspring.b);
                        else 
                            newInds.add(parents.get(0));
                    }
                }
            }
            
            for(int i = 0 ; i < newInds.size();i++){
                
                
               blackList.add  (  ((Individual) listOfPopulations[populNum].getIndividual(i) ).getGenotype()  );
            }
            
            // replace the current population with the new one
            for(int i=0; i<newInds.size(); i++) {
                listOfPopulations[populNum].setIndividualAt(i, newInds.get(i));
            }

    }

    @Override
    public void run() {
        Individual BestOne;
        // Initialize the population
       // population = new Population(this, populationSize);
        
        listOfPopulations = new Population[5];
        for (int i = 0; i< listOfPopulations.length; ++i){
            listOfPopulations[i] = new Population(this, populationSize);
        }

        // Collect initial system time, average fitness, and the best fitness
        time.a = System.currentTimeMillis();
       
        avgFitness.a = listOfPopulations[0].getAvgFitness();
        AbstractIndividual best = listOfPopulations[0].getBestIndividual();
        for(Population x : listOfPopulations)
          best = x.getBestIndividual().getFitness() > best.getFitness() ? x.getBestIndividual() : best ;
        bestFitness.a = best.getFitness();        
        
        // Show on map
        updateMap(best);
        System.out.println(listOfPopulations[0]);

        // Run evolution cycle for the number of generations set in GUI
        generations = 10000000;
        
        int notChanged = 0;
        boolean hightMutation = false;
        int highMutateTime = 0;
        for(int g=0; g < generations; g++) {
            
            // the evolution may be terminate from the outside using GUI button
            if (isFinished) {
                break;
            }
            
            for(int i = 0 ; i < listOfPopulations.length ; ++i)
                processOnePopulation(i, g);
                
          
            //map updater
            AbstractIndividual bestOld = best;
            if (g % debugLimit == 0) {
               for(Population x : listOfPopulations)
                    best = x.getBestIndividual().getFitness() > best.getFitness() ? x.getBestIndividual() : best ;
               updateMap(best);
               if(bestOld == best && !hightMutation){
                   notChanged++;
               }
               if(notChanged > 100) {
                   if(!hightMutation ) {
                   mutationProbability*=3;
                   hightMutation = true;
                   notChanged = 0;
                   }
                   if(hightMutation){
                       highMutateTime++;
                   }
                   if(highMutateTime == 100) {
                         mutationProbability/=3;
                   hightMutation = false;
                   highMutateTime = 0;
                   
                   }
               }
               System.out.println("--------------------");
                
                BestOne = (Individual)best;
               
                System.out.println("Generation : " + g);
                System.out.println("fintess: " + BestOne.getFitness() );
                System.out.println("");
                BestOne.printSelectedVerticies();
                System.out.println("--------------------");
               
            }
            
            updateGenerationNumber(g);
            
            
        }
        
       
        // === END ===
       
        
         for(Population x : listOfPopulations)
                    best = x.getBestIndividual().getFitness() > best.getFitness() ? x.getBestIndividual() : best ;
               
       // AbstractIndividual bestOne = population.getBestIndividual();
      //  Individual firendlyBestOne = (Individual)bestOne;
        ((Individual)best).clearFinish();
        
        ((Individual)best).printSelectedVerticies();
 
        time.b = System.currentTimeMillis();
      //  population.sortByFitness();
      //  avgFitness.b = population.getAvgFitness();
      //  best = population.getBestIndividual();
        bestFitness.b = best.getFitness();
        updateMap(best);
        

        System.out.println("Evolution has finished after " + ((time.b - time.a) / 1000.0) + " s...");
       // System.out.println("avgFit(G:0)= " + avgFitness.a + " avgFit(G:" + (generations - 1) + ")= " + avgFitness.b + " -> " + ((avgFitness.b / avgFitness.a) * 100) + " %");
       // System.out.println("bstFit(G:0)= " + bestFitness.a + " bstFit(G:" + (generations - 1) + ")= " + bestFitness.b + " -> " + ((bestFitness.b / bestFitness.a) * 100) + " %");
        System.out.println("bestIndividual= " +best);
        //System.out.println(pop);

        isFinished = true;
        System.out.println("========== Evolution finished =============");
    }
}
