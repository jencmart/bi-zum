package bi.zum.lab3;

import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.util.Pair;
import java.util.*;
import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.data.StateSpace;
import org.openide.util.lookup.ServiceProvider;
import java.util.LinkedList;
import java.util.HashSet;

@ServiceProvider(service = AbstractEvolution.class)


public class Evolution extends AbstractEvolution<Individual>  implements  Runnable {
    private AbstractIndividual [] bestFromEachIsle;
    private AbstractIndividual bestGlobal;
    private Population [] islesList;
     final private int numOfIsles;
    final private Pair<Long, Long> time;
    final private Random rand;
    final private Set<Integer> selected;
    final private Set<Integer> closed;
    final private List<Double> fitnessProgress;
    
    final private List<Double> worstFitnessProgress;
    final private List<Double> avgFitnessProgress;

    public Evolution() {
        isFinished = false;
        time = new Pair<>();
        rand = new Random();
        numOfIsles = 10;
        selected = new HashSet<>();
        closed = new HashSet<>();
        fitnessProgress = new ArrayList<>();
        worstFitnessProgress = new ArrayList<>();
        avgFitnessProgress= new ArrayList<>();
    }

    // MUTATION
    // + STEEPEST ASCEND HILL CLIMBING
    // + SIMMULATED ANNEALING ---- velikost okoli X pravdepodobnost prijeti zhorsujiciho se reseni
    // -- TABU SEARCH

    private void simmulatedAnnealing(int isleNum, AbstractIndividual offspring) {
        double temperature = mutationProbability;

        
        int numOfSamples = 10;

        ///POKUD SE NA OSTROVE ZE KTEREHO JE JEDINEC NEZMENILA BEST FITNES PO 10 GENEERACI .... ZVYS O NECO TEPLOTU
        
        if ( islesList[isleNum].GetTemperatureStatus() )
                mutationProbability /= 1.25 ;
        
        else if ( islesList[isleNum].getNotChangedFor() > 10 ) {
            mutationProbability *= 1.25;
        }
        

        ///ZMUTUJ JEDINCE, SPOCITEJ JEHO FITNESS
        offspring.mutate(temperature);
        offspring.computeFitness();

        ///NECH VYGENEROVAT OKOLO (10 JEDINCU; MUTACE OKOLI ZAVISLA NA TEPLOTE)
        List<AbstractIndividual> surrounding = ((Individual)offspring).getValidSurrounding(temperature, numOfSamples);

        ///VYBER NEJLEPSIHO Z OKOLI
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
            
       //   do {  
            Pair<AbstractIndividual, AbstractIndividual> offspring;
            List<AbstractIndividual> parents;

            // SELECTION
            // parents = islesList[isleNum].selectRoulette(2); For some reason roulette selection sucks.
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
            
       //   }while( islesList[isleNum].isInTabuList( newIndividuals.get(newIndividuals.size() - 1) )    );  //// GENERUJ NOVEHO POTOMKA DOKUD NEBUDE MIMO TABULIST
        }

        // EXCHANGE POPULATION
        for (int i = 0; i < newIndividuals.size(); i++) {
            islesList[isleNum].setIndividualAt(i, newIndividuals.get(i));
            islesList[isleNum].addToTabuList(newIndividuals.get(i));                  //// NASYPEJ JE DO TABULISTU
        }
    }

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

/**
    @Override
    public void run(){
        test r = new test();

        Thread thread1 = new Thread(r, "Thread 1");
        thread1.start();

        Thread thread2 = new Thread(r, "Thread 2");
        thread2.start();


    }
    */
    
    private void initializeEvolution(){
        
        
        //------PREDZPRACOVANI PROBLEMU-------
        
        ///ODREZANI 1. LEVELU (NEDOCHAZI MI, ZE BYCH PAK MOHL ODREZAT DLASI )
        for(Node nod : StateSpace.getNodes()){
            List<Node> expandedNodes = nod.expand();

            if(expandedNodes.size() == 1) {
                closed.add(nod.getId());
                selected.add(expandedNodes.get(0).getId());
            }
        }
        
        
        
        
        
        
        ///pro odkladani prohledanych.... (bude se hodit po cely proces kontrakce seknenci )
        HashSet<Node> closedSides = new HashSet<>();
                
        ///KONTRAKCE SEKVENCI
        for(Node nod : StateSpace.getNodes()){
            
            if(closedSides.contains(nod))
                continue;
            
            List<Node> expandedNodes = nod.expand();
            if(expandedNodes.size() == 2 ) {
                Node SuspiciousNode = nod;               
                
                closedSides.add(SuspiciousNode);
                
                /// naka inicializace pro to nase hledani ....
                List<Node> oneSide = new ArrayList<>();
                List<Node> otherSide = new ArrayList<>();
                oneSide.add(0,SuspiciousNode);
                
                ///dva "stacky" pro prohledani obou stran uzlu ktery mel 2 potomky....
                LinkedList<Node> openedOneSide = new LinkedList<>();
                LinkedList<Node> openedOtherSide = new LinkedList<>();                              
                

                
                
                //// --------------------- PROHLEDAVEJ JEDNU STRANU------------------------
                ///vem jeden uzel z tech dvou a prohledej dokud nenarazis na uzel, ktery ma vic jak 2 potomky
                ///prvky sazej vzdy na pozici 0
                openedOneSide.add(expandedNodes.get(0));
                
                while( ! openedOneSide.isEmpty() ){
                    List<Node> tmpExpanded = openedOneSide.getLast().expand();

                    ///pokud narazis na uzel co sousedi s vice nez dveni, je to jeden konec nasi sekvence...
                    if(tmpExpanded.size() != 2 ){
                        oneSide.add(0, openedOneSide.getLast());
                        
                        closedSides.add(openedOneSide.getLast());
                        openedOneSide.pop();
                        break;
                    }
                    
                    
                    ///pokud sousedi jen se dvema nebo jednim uzlem.... pridej ty co jeste nejsou v nasem one side listu (aka. nejsou closed)
                    /// plus pridej ten co se jakoby expandoval
                    else {
                        if( closedSides.contains(tmpExpanded.get(0)) && closedSides.contains(tmpExpanded.get(1))  )
                            break;
                        for(Node expanded : tmpExpanded)
                        {
                         if( ! closedSides.contains(expanded)  )   {
                             oneSide.add(0, expanded);  /// pridej jako soucat te nasi posloupnosti one side 
                             closedSides.add(openedOneSide.getLast()); /// ten co byl expandovanej pridej do closed
                             openedOneSide.pop();
                             openedOneSide.addLast(expanded);  ///a zaroven ho prdej do stacku s tim, ze ho chceme expandovat dal...
                             
                             
                         }
                        }
                    }
                    
                    tmpExpanded.clear();
                }
                /// ------------------------------------------------------------
                
               //// --------------------- PROHLEDAVEJ DRUHOU STRANU------------------------
                ///vem druhy uzel a prohledavej dokud nenarazis na uzel co ma vic jak 2 potomky
                /// prvku sazej vzdy nakonec
                openedOtherSide.add(expandedNodes.get(1));
                
                while( ! openedOtherSide.isEmpty() ){
                    List<Node> tmpExpanded = openedOtherSide.getLast().expand();
                    ///pokud narazis na uzel co sousedi s vice nez dveni, je to jeden konec nasi sekvence...
                    if(tmpExpanded.size() != 2 ){
                        otherSide.add(openedOtherSide.getLast());
                        closedSides.add(openedOtherSide.getLast());
                        openedOtherSide.pop();
                        break;
                    }

                    ///pokud sousedi jen se dvema nebo jednim uzlem.... pridej ty co jeste nejsou v nasem one side listu (aka. nejsou closed)
                    /// plus pridej ten co se jakoby expandoval
                    else {
                        if( closedSides.contains(tmpExpanded.get(0)) && closedSides.contains(tmpExpanded.get(1))  )
                            break;
                        for(Node expanded : tmpExpanded)
                        {
                         if( ! closedSides.contains(expanded)  )   {
                             otherSide.add(expanded);  /// pridej jako soucat te nasi posloupnosti one side 
                            closedSides.add(openedOtherSide.getLast()); /// ten co byl expandovanej pridej do closed
                             openedOtherSide.pop();
                             openedOtherSide.addLast(expanded);  ///a zaroven ho prdej do stacku s tim, ze ho chceme expandovat dal...
                             
                             
                         }
                        }
                    }
                    
                    
                    tmpExpanded.clear();
                }
                /// ------------------------------------------------------------
          
                ///spoj tyhle listy stylem: vem prnvi a na jeho konec nasypej druhy
                /// vsechny tyto uzle spocitej. 
                oneSide.addAll(otherSide);
                
                /// POKUD LICHY POCET (a vic jak 3)-> NASYP LICHY JAKO VYBRANY
                ///pokud jich je lichy pocet a je jich vic jak 3 (tj 5 a vic) tak vyber ob jeden od prvniho do posledniho 
                /// pokud jsou 3neres to ( tezko najdes neco optimalniho )....
                if(( oneSide.size() % 2 == 1) && ( oneSide.size() > 3 ))
                {
                    boolean odd = true; 
                    for ( Node x : oneSide)
                    {
                     
                        if( odd){
                            selected.add(x.getId()); 
                            odd = !odd;
                        }
                        else{
                            closed.add(x.getId());
                            odd = !odd;
                        }
                    }
                }
                
                
                /**
                /// pokud jich je sudy pocet.... hod si kostkou a vyber to ob jeden od jednoho kraje.... {VEDE NA NEOPTIMALNI RESENI !!!}
                if(oneSide.size() % 2 == 0)
                {
                    if(rand.nextBoolean())
                    {
                        boolean odd = true; 
                        for ( Node x : oneSide)
                        {

                            if( odd){
                                selected.add(x.getId()); 
                                odd = !odd;
                            }
                            else{
                                closed.add(x.getId());
                                odd = !odd;
                            }
                        }     
                    }
            
                    else 
                    {
                        boolean odd = true; 
                        
                        ListIterator<Node> it = oneSide.listIterator(oneSide.size());
                        while(it.hasPrevious())
                        {

                            if( odd){
                                selected.add( ( it.previous() ) .getId()); 
                                odd = !odd;
                            }
                            else{
                                closed.add(  ( it.previous() ) .getId()  );
                                odd = !odd;
                            }
                        }
                        
                        
                    }
                }
                   
                
                **/
                
            //    closed.add(nod.getId());
            //    selected.add(expandedNodes.get(0).getId());
            }
        }
        
        ///--------------------------------------

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
       
        int i = 0;
 
      for ( double x :  fitnessProgress ){
          System.out.print((int)x + ", ");
          i++;
          if ( i == 500)
              System.out.println("");
      }
      System.out.println("");
      System.out.println("");
      for ( double x :  avgFitnessProgress ){
          System.out.print((int)x + ", ");
          i++;
          if ( i == 500)
              System.out.println("");
      }
      System.out.println("");
      System.out.println("");
      for ( double x :  worstFitnessProgress ){
          System.out.print((int)x + ", ");
          i++;
          if ( i == 500)
              System.out.println("");
      }
          
    }
    private void updateEvolutionStatus() {
        /// ------------- KATASTROFA !!!!!!!!!!!!!
        for (int i = 0; i < islesList.length; ++i) {
            if (islesList[i].getNotChangedFor() > 100) { /// POKUD SE PO 100 GENERACI NEVYLEPSI FITNESS, VYHLAD 99% POPULACE (ZANECH NEJSILNEJSI )
                islesList[i].resetNotChangedFor();
                islesList[i].Exterminate(this);
            }
            if (bestFromEachIsle[i].getFitness() > islesList[i].getBestIndividual().getFitness())
                islesList[i].addNotChangedFor();
            else {
                bestFromEachIsle[i] = islesList[i].getBestIndividual().deepCopy();
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
            
            fitnessProgress.add(bestGlobal.getFitness());
            avgFitnessProgress.add(islesList[0].GetAVGFitness() );
            worstFitnessProgress.add(islesList[0].GetWorstFitness() );
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