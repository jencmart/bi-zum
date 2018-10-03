package bi.zum.lab3;

import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.util.Pair;
import java.util.*;
import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.data.StateSpace;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = AbstractEvolution.class)


public class Evolution extends AbstractEvolution<Individual>  implements  Runnable {
    private AbstractIndividual [] bestFromEachIsle;
    private AbstractIndividual bestGlobal;
    private Population [] islesList;
    private int numOfIsles;
    private Pair<Long, Long> time;
    private Random rand;
    private Set<Integer> selected;
    private Set<Integer> closed;

    public Evolution() {
        isFinished = false;
        time = new Pair<>();
        rand = new Random();
        numOfIsles = 1;
        selected = new HashSet<>();
        closed = new HashSet<>();
    }

    // MUTATION
    // + STEEPEST ASCEND HILL CLIMBING
    // + SIMMULATED ANNEALING ---- velikost okoli X pravdepodobnost prijeti zhorsujiciho se reseni
    // + TABU SEARCH

    private void simmulatedAnnealing(int isleNum, AbstractIndividual offspring) {
        double temperature = mutationProbability;
        int numOfSamples = 10;

        if ( islesList[isleNum].getNotChangedFor() > 10 )
            temperature += temperature;

        offspring.mutate(temperature);
        offspring.computeFitness();

        List<AbstractIndividual> surrounding = ((Individual)offspring).getValidSurrounding(temperature, numOfSamples);

        AbstractIndividual best = offspring;
        for (AbstractIndividual current : surrounding) {
            if(current.getFitness() > best.getFitness() && rand.nextDouble() < temperature) {
                if(rand.nextDouble() < temperature) //todo to nevim jestli je the best reseni....
                    continue;
                best = current;
            }
        }
    }

    private void evolvePopulation(int isleNum, int generatin) {
        ArrayList<AbstractIndividual> newIndividuals = new ArrayList<>();

        // ISLE MIGRATION
        if (generatin % 20 == 0) { // migrant is then on both isles. not ideal
            //newIndividuals.add(islesList[(isleNum + 1) % numOfIsles].SelectRandIdnividual().deepCopy());
            newIndividuals.add(  islesList[(isleNum + 1) % numOfIsles].WithdrawIndividual()  );
        }
        while (newIndividuals.size() < populationSize) {
            Pair<AbstractIndividual, AbstractIndividual> offspring;
            List<AbstractIndividual> parents;

            // SELECTION
            // parents = islesList[isleNum].selectRoulette(2);
            parents = islesList[isleNum].selectTournament(2);

            // CROSSOVER
            if (crossoverProbability < rand.nextDouble())
                offspring = parents.get(0).deepCopy().crossover(parents.get(1).deepCopy());
            else {
                offspring = new Pair<>();
                offspring.a = parents.get(0).deepCopy();
                offspring.b = parents.get(1).deepCopy();
            }

            // MUTATION
            simmulatedAnnealing(isleNum,offspring.a);
            simmulatedAnnealing(isleNum,offspring.b);

            // OCENIT NOVATORSTVI
            // MOC PODOBNE PENALIZOVAT

            //DETERMINISTIC CROWDING
            offspringSelection(offspring, parents, newIndividuals);
        }

        // EXCHANGE POPULATION
        for (int i = 0; i < newIndividuals.size(); i++)
            islesList[isleNum].setIndividualAt(i, newIndividuals.get(i));
    }
/**
    @Override
    public void run() {
        initializeEvolution();
        for (int g = 0; g < generations; g++) {
            if (isFinished)
                break;
            for (int i = 0; i < islesList.length; ++i)
                evolvePopulation(i, g);
            updateEvolutionStatus();
            updateMapGUI(g);
        }
        terminateEvolution();
    }
*/

    @Override
    public void run(){
        test r = new test();

        Thread thread1 = new Thread(r, "Thread 1");
        thread1.start();

        Thread thread2 = new Thread(r, "Thread 2");
        thread2.start();


    }
    private void initializeEvolution(){
        //predzpracovani problemu
        for(Node nod : StateSpace.getNodes()){
            List<Node> expandedNodes = nod.expand();
            if(expandedNodes.size() == 1) {
                closed.add(nod.getId());
                selected.add(expandedNodes.get(0).getId());
            }
        }
        //DFS  for num of expanded nodes == 2

        islesList = new Population[numOfIsles];
        bestFromEachIsle = new AbstractIndividual[numOfIsles];

        for (int i = 0; i< islesList.length; ++i){
            islesList[i] = new Population(this, populationSize);
        }

        time.a = System.currentTimeMillis();
        //avgFitness.a = islesList[0].getAvgFitness();
        bestGlobal = islesList[0].getBestIndividual();

        for(int i = 0; i < islesList.length ; ++i) {
            bestFromEachIsle[i] = islesList[i].getBestIndividual().deepCopy();
        }
        for(AbstractIndividual x : bestFromEachIsle)
            bestGlobal = x.getFitness() > bestGlobal.getFitness() ? x : bestGlobal;
        // bestFitness.a = bestGlobal.getFitness();

        updateMap(bestGlobal);
    }
    private void terminateEvolution(){
        for(Population x : islesList)
            bestGlobal = x.getBestIndividual().getFitness() > bestGlobal.getFitness() ? x.getBestIndividual() : bestGlobal;

        ((Individual)bestGlobal).clearFinish();
        ((Individual)bestGlobal).clearFinish();
        System.out.print(bestGlobal);

        time.b = System.currentTimeMillis();
        //  population.sortByFitness();
        //  avgFitness.b = population.getAvgFitness();
        updateMap(bestGlobal);
        System.out.println("Evolution has finished after " + ((time.b - time.a) / 1000.0) + " s...");
        // System.out.println("avgFit(G:0)= " + avgFitness.a + " avgFit(G:" + (generations - 1) + ")= " + avgFitness.b + " -> " + ((avgFitness.b / avgFitness.a) * 100) + " %");
        // System.out.println("bstFit(G:0)= " + bestFitness.a + " bstFit(G:" + (generations - 1) + ")= " + bestFitness.b + " -> " + ((bestFitness.b / bestFitness.a) * 100) + " %");
        //  System.out.println("bestIndividual=" +bestGlobal);
        //System.out.println(pop);
        isFinished = true;
        System.out.println("========== Evolution finished =============");
    }
    private void updateEvolutionStatus() {
        for (int i = 0; i < islesList.length; ++i) {
            if (islesList[i].getNotChangedFor() > 1000) {
                islesList[i].resetNotChangedFor();
                islesList[i].Exterminate(this);
            }
            if (bestFromEachIsle[i].getFitness() > islesList[i].getBestIndividual().getFitness())
                islesList[i].addNotChangedFor();
            else {
                bestFromEachIsle[i] = islesList[i].getBestIndividual().deepCopy();
                islesList[i].resetNotChangedFor();
            }
        }
        for (AbstractIndividual x : bestFromEachIsle)
            bestGlobal = x.getFitness() > bestGlobal.getFitness() ? x : bestGlobal;
    }
    private void updateMapGUI(int g) {
        //if (g % 10 == 0) {
         //   updateMap(bestGlobal);
            System.out.println("--------------------");
            System.out.println("Generation : " + g);
            System.out.printf("Isles stagnation : \n");
            for (int i = 0 ; i < numOfIsles ; ++i)
                System.out.println("Isle ["+i+"] : "+ islesList[i].getNotChangedFor()+" generations. Best indiv.: "+ islesList[i].getBestIndividual().getFitness());

            System.out.println("fintess: " + bestGlobal.getFitness());
            System.out.println("");
          //  if (g %10 == 0)
            //    System.out.print(bestGlobal);
            if (g %5 == 0) {
                AbstractIndividual tmp = bestGlobal.deepCopy();
                ((Individual)tmp).clearFinish();
                System.out.println("TMP CLEARFINISH");
              //  System.out.print(tmp);
              System.out.println("Verticies: "+ ( StateSpace.nodesCount()- tmp.getFitness() ));
                updateMap(tmp);
            }
            System.out.println("--------------------");
      //  }
        updateGenerationNumber(g);
    }
    @Override
    public String getName() {
        return "Skynet";
    }
    private void offspringSelection(Pair<AbstractIndividual, AbstractIndividual> offspring, List<AbstractIndividual> parents, ArrayList<AbstractIndividual> newIndividuals){
        //offspring1 is closer to first parrent1,  -> fight ofs1 parent1
        if (   ((Individual)offspring.a).distFromOther(parents.get(0)) > ((Individual)offspring.a).distFromOther(parents.get(1))) {
            //the one with higher fitness wins
            if (offspring.a.getFitness() > parents.get(0).getFitness())
                newIndividuals.add(offspring.a);
            else
                newIndividuals.add(parents.get(0));

            //if still make second match
            if (newIndividuals.size() < populationSize) {
                if (offspring.b.getFitness() > parents.get(1).getFitness())
                    newIndividuals.add(offspring.b);
                else
                    newIndividuals.add(parents.get(1));
            }
        }

        //else figt offspring1 and parent2
        else {
            //the one with higher fitness wins
            if (offspring.a.getFitness() > parents.get(1).getFitness())
                newIndividuals.add(offspring.a);
            else
                newIndividuals.add(parents.get(1));

            //if still make second match
            if (newIndividuals.size() < populationSize) {
                if (offspring.b.getFitness() > parents.get(0).getFitness())
                    newIndividuals.add(offspring.b);
                else
                    newIndividuals.add(parents.get(0));
            }
        }
    }
    boolean isInSelected(int i){
        return selected.contains(i);
    }boolean isInClosed(int i) {
        return closed.contains(i);
    }
}




class test implements  Runnable {

    @Override
    public void run() {
        System.out.println("saddsfsd");
    }
}