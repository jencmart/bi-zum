package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.api.ga.AbstractPopulation;
import cz.cvut.fit.zum.data.StateSpace;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Your name
 */
public class Population extends AbstractPopulation {

    public Population(AbstractEvolution evolution, int size) {
        individuals = new Individual[size];
        
        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual(evolution, true);
            individuals[i].computeFitness();
        }
    }
  
    
    
    @Override
     public AbstractIndividual getIndividual(int idx){
        return individuals[idx];  
     }

    /**
     * Method to select individuals from population
     *
     * @param count The number of individuals to be selected
     * @return List of selected individuals
     */
    public List<AbstractIndividual> selectIndividuals(int count) { // ROULETTE SELECTION? probbably..
        ArrayList<AbstractIndividual> selected = new ArrayList<AbstractIndividual>();
        long fitnessSum = 0;
        
        for(int i=0; i < individuals.length ; i++) {
            Individual tmp = (Individual)individuals[i];
            fitnessSum += tmp.getFitness();
        }
        	
        Random r = new Random();
        this.sortByFitness(); 
       
        for(int i = 0; i < count ; ++i) {
            double randValtoSubstract = r.nextDouble() * fitnessSum;
            
            for(int j=0; j < individuals.length; ++j) {	
                Individual tmp = (Individual)individuals[j];
		randValtoSubstract -= tmp.getFitness();	
                
		if(randValtoSubstract <= 0){
                    selected.add(individuals[j]);
                    break;
                }
            }
           
        }
        
        long avgFitness = fitnessSum/individuals.length;
        
      /**  for(AbstractIndividual x : selected){
            
            if(   ( (Individual)x).getFitness() >= avgFitness ) {
               
                for(AbstractIndividual y : selected) {
                    
                     int diff = 0;
                    for(int i = 0; i <  StateSpace.nodesCount() ; ++i ){
                        if( (  ((Individual)x).isNodeSelected(i) != ((Individual)y).isNodeSelected(i)  )  )
                            diff++;
                    }
                    
                    if (diff > ( StateSpace.nodesCount()%10.0) )
                        ((Individual)x).addToFittness(((Individual)x).getFitness()*0.);
                        * 
                        * 
                    if (diff < ( StateSpace.nodesCount()%100.0) )
                        ((Individual)x).addToFittness(((Individual)x).getFitness()*0.9);
                    else if(diff > ( StateSpace.nodesCount()%10.0)){
                        ((Individual)x).addToFittness(((Individual)x).getFitness()*1.1);
                }
                
            }
        }*/
            
       
        return selected;
    }
}
