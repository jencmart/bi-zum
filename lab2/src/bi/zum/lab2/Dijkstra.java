package bi.zum.lab2;

import bi.zum.lab1.PathRecSearch;
import bi.zum.lab2.util.Euclidean;
import bi.zum.lab2.util.ZumPriorityQueue;
import cz.cvut.fit.zum.api.AbstractAlgorithm;
import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.api.UninformedSearch;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = AbstractAlgorithm.class)
public class Dijkstra extends PathRecSearch implements UninformedSearch {
    private ZumPriorityQueue<Node> open;
    private HashSet<Node> closed;
    private Map<Node, Double> dist;
    private Node current;
    private List<Node> path;
    
    @Override
    public List<Node> findPath(Node startNode) {
        open = new ZumPriorityQueue<Node>();
        closed = new HashSet<Node>();
        prev = new HashMap<Node, Node>();
        dist = new HashMap<Node, Double>();
       
        open.enqueue(startNode, 0);
        dist.put(startNode, 0.0);
        
        while( ! open.isEmpty()) {
            
            current = open.dequeue();
            
            if(current.isTarget())
                    return backtrackDaShit(current);
            
            for ( Node expanded : current.expand()) {                
                double distFromCurrent = Euclidean.distance(current, expanded);
                double totalDistance = dist.get(current) + distFromCurrent;
                
                if( !open.contains(expanded) && !closed.contains(expanded)) {    
                    open.enqueue(expanded, totalDistance);
                    prev.put(expanded, current);
                    dist.put(expanded, totalDistance);
                }
                
                else if (open.contains(expanded) && !closed.contains(expanded)) {
                    if(totalDistance < dist.get(expanded)) {
                        dist.put(expanded, totalDistance);
                        prev.put(expanded, current);
                        open.updateKey(expanded, totalDistance);
                    }                    
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
        return "Dijk$tra BRO$$";
    }
         
}
