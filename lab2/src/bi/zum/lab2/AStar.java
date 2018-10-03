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
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = AbstractAlgorithm.class)

public class AStar extends PathRecSearch implements InformedSearch  {
    private ZumPriorityQueue<Node> open;
    private HashSet<Node> closed;
    private Map<Node, Double> dist;
    private List<Node> path;
    
    @Override
    public String getName() {
        return "A*";
    }
    
    @Override
    public List<Node> findPath(Node startNode, Node endNode) {
        open = new ZumPriorityQueue<Node>();
        closed = new HashSet<Node>();
        prev = new HashMap<Node, Node>();
        dist = new HashMap<Node, Double>();
        Node current;
        
        open.enqueue(startNode, 0.0 + Euclidean.distance(startNode, endNode) );
        dist.put(startNode, 0.0);
        
        while( ! open.isEmpty()) {
            current = open.dequeue();
            
            if (current.isTarget())
                return backtrackDaShit(current);
            
            for(Node expanded : current.expand()) {
                double distFromStart = dist.get(current) + Euclidean.distance(expanded, current);
                
                if( !open.contains(expanded) && !closed.contains(expanded)) {
                    dist.put(expanded, distFromStart);
                    prev.put(expanded, current);
                    open.enqueue(expanded, distFromStart + Euclidean.distance(expanded, endNode));
                }
                
                else if (open.contains(expanded) && !closed.contains(expanded)) {
                    if(distFromStart < dist.get(expanded)){
                        dist.put(expanded, distFromStart);
                        prev.put(expanded, current);
                        open.updateKey(expanded, distFromStart + Euclidean.distance(expanded, endNode));
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
    
}
