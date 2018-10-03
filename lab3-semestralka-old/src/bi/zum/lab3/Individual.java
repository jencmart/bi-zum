package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.data.Edge;
import cz.cvut.fit.zum.data.StateSpace;
import cz.cvut.fit.zum.util.Pair;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Martin Jenc
 */
public class Individual extends AbstractIndividual {
    private double fitness = Double.NaN;
    private AbstractEvolution evolution;
    private boolean[] genotype ;

    public Individual(AbstractEvolution evolution, boolean randomInit) {
        this.evolution = evolution;
        Random  rand = new Random(); 
        genotype = new boolean[StateSpace.nodesCount()];
        
        if(randomInit){
            for(int i = 0; i < genotype.length ; ++i)
                genotype[i] = rand.nextBoolean();
            repair();
                 //  clearFinish();
        }
    }
    
    public void printSelectedVerticies(){
        for (int i = 0; i < genotype.length ; ++i){
            if( genotype[i]){
                System.out.print(i+",");
                
            }
            if(  i %1000 == 0 && i != 0)
                System.out.println("");
        }
        System.out.println("");
        System.out.println("----------------------------------------");
        System.out.println("----------------------------------------");
    }

    @Override
    public boolean isNodeSelected(int j) {
        return genotype[j];
    }

    @Override
    public void computeFitness() {
        repair();
        int numOfVertices = 0;
        for(int i = 0 ; i < this.genotype.length ; ++i)
            if( genotype[i] )
                numOfVertices++;
        
        
        this.fitness = StateSpace.nodesCount() - numOfVertices;
    }
    
    
    boolean isSelected(int i) {
        return genotype[i];
    }
    
    
    public void clearFinish() {
        for(int i = 0; i < genotype.length; ++i){
            if(genotype[i])
                genotype[i] = false;
            
            for(Edge e : StateSpace.getEdges())   
                if( ! genotype[e.getFromId()]  && ! genotype[e.getToId()] )
                        genotype[i] = true;
                
            
        }
    }

    private void repair() {
        Random  rand = new Random();
        for(Edge e : StateSpace.getEdges()) {           
            if( ! genotype[e.getFromId()]  && ! genotype[e.getToId()] ){
                if(rand.nextBoolean())
                    genotype[e.getFromId()] = true;
                else
                    genotype[e.getToId()] = true;
            }
        }
        
 
    }
    
    public boolean[] getGenotype(){
        return genotype;
    }
    
    @Override
    public double getFitness() {
        return this.fitness;
    }

    public void addToFittness(double a){
        this.fitness+=a;
    }
    
    @Override
    public void mutate(double mutationRate) {
        Random  rand = new Random(); 
        
        for(int i = 0; i < this.genotype.length ; ++i) 
            if( rand.nextFloat() < mutationRate)
                this.genotype[i] =  ! this.genotype[i];
    }
    
    @Override
    public Pair crossover(AbstractIndividual other) { //two point mutation
        
        Individual niceOther = (Individual)other;
        Pair<Individual,Individual> result = new Pair();
        result.a = new Individual(evolution, false);
        result.b = new Individual(evolution, false);
        
        int  firstCut = ThreadLocalRandom.current().nextInt(1,StateSpace.nodesCount());
        int secondCut =  ThreadLocalRandom.current().nextInt(1,StateSpace.nodesCount());

        while(secondCut == firstCut)
            secondCut = ThreadLocalRandom.current().nextInt(1,StateSpace.nodesCount());
        
        if(secondCut < firstCut){
            int tmp = firstCut;
            firstCut = secondCut;
            secondCut = tmp;
        }   
        
        int i = 0;
        for(  ; i < firstCut ; ++i) {
            result.a.genotype[i] = this.genotype[i];
            result.b.genotype[i] = niceOther.genotype[i];
        }
        for( ; i < secondCut ; ++i) {
            result.a.genotype[i] = niceOther.genotype[i]; 
            result.b.genotype[i] = this.genotype[i];
        }
        for(; i < StateSpace.nodesCount() ; ++i ) {
            result.a.genotype[i] = this.genotype[i];
            result.b.genotype[i] = niceOther.genotype[i];
        }
        return result;
    }

    @Override
    public Individual deepCopy() {
        Individual newOne = new Individual(evolution, false);
       
        for(int i = 0 ; i < StateSpace.nodesCount() ; ++i){
         newOne.genotype[i] = this.genotype[i];   
        }
        
        // TODO: at least you should copy your representation of search-space state

        // for primitive types int, double, ...
        // newOne.val = this.val;

        // for objects (String, ...)
        // for your own objects you have to implement clone (override original inherited from Objcet)
        // newOne.infoObj = thi.infoObj.clone();

        // for arrays and collections (ArrayList, int[], Node[]...)
        /*
         // new array of the same length
         newOne.pole = new MyObjects[this.pole.length];		
         // clone all items
         for (int i = 0; i < this.pole.length; i++) {
         newOne.pole[i] = this.pole[i].clone(); // object
         // in case of array of primitive types - direct assign
         //newOne.pole[i] = this.pole[i]; 
         }
         // for collections -> make new instance and clone in for/foreach cycle all members from old to new
         */
        newOne.fitness = this.fitness;
        return newOne;
    }


    @Override //todo
    public String toString() {
        StringBuilder sb = new StringBuilder();
        /* TODO: implement own string representation, such as a comma-separated
         * list of indices of nodes in the vertex cover
         */
        sb.append(super.toString());

        return sb.toString();
    }
}
