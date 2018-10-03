package bi.zum.lab2;

import bi.zum.lab1.PathRecSearch;
import bi.zum.lab2.util.Euclidean;
import bi.zum.lab2.util.ZumPriorityQueue;
import cz.cvut.fit.zum.api.AbstractAlgorithm;
import cz.cvut.fit.zum.api.InformedSearch;
import cz.cvut.fit.zum.api.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;




@ServiceProvider(service = AbstractAlgorithm.class)

public class GreedySearch extends PathRecSearch implements InformedSearch {
    private ZumPriorityQueue<Node> open;
    private HashSet<Node> closed;
    private Node current;
    private List<Node> path;
    
    @Override
    public List<Node> findPath(Node startNode, Node endNode){
        open = new ZumPriorityQueue<Node>();
        closed = new HashSet<Node>();
        prev = new HashMap<Node, Node>();
        
        open.enqueue(startNode,Euclidean.distance(startNode, endNode));
        
        while(!open.isEmpty()) {
            current = open.dequeue();
            
            for(Node expanded : current.expand()) {
                if(expanded.isTarget())
                    return backtrackDaShit(current);
                
                if ( ! open.contains(expanded) && ! closed.contains(expanded)) {
                    open.enqueue(expanded, Euclidean.distance(expanded, endNode));
                    prev.put(expanded, current);
                }
            }
            closed.add(current);
        }
        
        
        return null;
    }
    
    List<Node> backtrackDaShit(Node target){
        path = new ArrayList<Node>();
        Node previous = target;
    
        path.add(target);
        while( prev.containsKey(previous) ) {
            previous = prev.get(previous);
            path.add(previous);
        }
        return path;
    }
    
    @Override
    public String getName() {
        return "Greedy fakin search";
    }    
}
