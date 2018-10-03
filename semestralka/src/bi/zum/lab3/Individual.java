package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.data.Edge;
import cz.cvut.fit.zum.data.StateSpace;
import cz.cvut.fit.zum.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Individual extends AbstractIndividual {
    private double fitness = Double.NaN;
    private AbstractEvolution evolution;
    private boolean[] genotype;
    private Random rand;

    Individual(AbstractEvolution evolution, boolean randomInit) {
        this.evolution = evolution;
        rand = new Random();
        genotype = new boolean[StateSpace.nodesCount()];
        if(randomInit){
            for(int i = 0; i < genotype.length ; ++i) {
                genotype[i] = rand.nextBoolean();
                if( ((Evolution)evolution).isInSelected(i) )
                    genotype[i] = true;
                if( ((Evolution)evolution).isInClosed(i) )
                    genotype[i] = false;
            }
            repair();
           // clearRandom();
        }
    }


    int distFromOther( AbstractIndividual other ) {
        int distance = 0;
        for (int i = 0; i <  StateSpace.nodesCount(); ++i)
            if ((this.genotype[i] ^ other.isNodeSelected(i)))
                distance++;
        return distance;
    }

    @Override
    public boolean isNodeSelected(int j) {
        return genotype[j];
    }

    @Override
    public void computeFitness() {
        repair();

        Individual tmp = this.deepCopy();
        tmp.clearFinish();

        int numOfVertices = 0;
        for (boolean aGenotype : tmp.genotype)
            if (aGenotype)
                numOfVertices++;

        this.fitness = StateSpace.nodesCount() - numOfVertices;
    }

    public void SubstitueWithNewRandom(){
        for(int i = 0; i < genotype.length ; ++i) {
            genotype[i] = rand.nextBoolean();
            if( ((Evolution)evolution).isInSelected(i) )
                genotype[i] = true;
            if( ((Evolution)evolution).isInClosed(i) )
                genotype[i] = false;
        }
        computeFitness();
    }

    void clearRandom() {
        for( int i = rand.nextInt(genotype.length); i < genotype.length; ++i){
            if(genotype[i])
                genotype[i] = false;
            for(Edge e : StateSpace.getEdges())
                if( ! genotype[e.getFromId()]  && ! genotype[e.getToId()] )
                    genotype[i] = true;
        }
    }

    void clearFinish() {
        for(int i = 0; i < genotype.length; ++i){
            if(genotype[i] && ! ((Evolution)evolution).isInSelected(i) )
                genotype[i] = false;
            for(Edge e : StateSpace.getNode(i).getEdges())
                if( ! genotype[e.getFromId()]  && ! genotype[e.getToId()] ) {
                    genotype[i] = true;
                    break;
                }
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
    
    @Override
    public double getFitness() {
        return this.fitness;
    }
    
    @Override
    public void mutate(double mutationRate) {
        Random  rand = new Random(); 
        
        for(int i = 0; i < this.genotype.length ; ++i)
            if (rand.nextFloat() < mutationRate && !((Evolution)evolution).isInSelected(i) && !((Evolution)evolution).isInClosed(i))
                this.genotype[i] = !this.genotype[i];
    }


    public List<AbstractIndividual> getValidSurrounding(double mutationRate, int numOfSamples){
        ArrayList<AbstractIndividual> surrounding = new ArrayList<>();

        for (int i = 0; i < numOfSamples; i++) {
            Individual tmp = this.deepCopy();
            tmp.mutate(mutationRate);
            tmp.computeFitness();
            surrounding.add(tmp);
        }
        return surrounding;

    }
    
    
    
    ///TWO POINT CROSSOVER
    @Override
    public Pair crossover(AbstractIndividual other) { //two point crossower
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

        System.arraycopy(this.genotype, 0, newOne.genotype, 0, StateSpace.nodesCount());
        newOne.fitness = this.fitness;

        return newOne;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int cnt=0;
        sb.append("=== INDIVIDUAL ===\n");
        for (int i = 0; i < genotype.length ; ++i){
            if( genotype[i]){
                cnt++;
                sb.append(i);
                if(i+1<genotype.length)
                    sb.append(',');
            }
            if(i %1500 == 0 && i != 0)
                sb.append('\n');
        }
        sb.append('\n').append("Verticies: ").append(cnt).append("\n================\n");
        return sb.toString();
    }

}
