package bi.zum.lab1;

import cz.cvut.fit.zum.api.AbstractAlgorithm;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import cz.cvut.fit.zum.api.Node;
import java.util.List;
import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.api.UninformedSearch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = AbstractAlgorithm.class, position = 5)
public class BFS extends AbstractAlgorithm implements UninformedSearch {
    private LinkedList<Node> opened;
    private HashSet<Node> closed;
    private Map<Node, Node> prev;
    private List<Node> path;
    private Map<Node, Integer> expandNumber;
    private Map<Node, Long> expandTime;
    private long startTime, endTime, totalTime;
    private int numberOfVert;
    
    
    @Override
    public String getName() {
        return "BFS";
    }
    
    @Override
    public List<Node> findPath(Node startNode) {
        startTime = System.currentTimeMillis();
        opened = new LinkedList<Node>();
        closed = new HashSet<Node>();
        prev = new HashMap<Node, Node>();
       // expandNumber = new HashMap<Node, Integer>();
       // expandTime = new HashMap<Node, Long>();
        
        path = null;
        numberOfVert = 0;

        opened.add(startNode);   
 
        while(!opened.isEmpty()) {
            if ( opened.getFirst().isTarget()) {
                endTime = System.currentTimeMillis();
                totalTime = endTime - startTime;
                
                return backTrack(startNode, opened.getFirst());
            }
            numberOfVert++;
            
            for( Node y : opened.getFirst().expand()){
                if(!opened.contains(y) && !closed.contains(y)){
                prev.put(y, opened.getFirst());
                opened.add(y);
                }
            }
          //  expandNumber.put(opened.getFirst(), numberOfVert);
           // expandTime.put(opened.getFirst(), System.currentTimeMillis());
            closed.add(opened.removeFirst());   
        }
        return path;
    }
    
    public List<Node> backTrack(Node startNode, Node finalNode) {
        
        path = new ArrayList<Node>();
        path.add(finalNode);
        
        Node tmp = finalNode;
        while( tmp != startNode ){
            path.add(prev.get(tmp));
            tmp = prev.get(tmp);
        }
        
        System.out.println("time = "+totalTime+" ms");
        System.out.println("number expanded Vertices = "+numberOfVert);
        
        return path;
    }
}