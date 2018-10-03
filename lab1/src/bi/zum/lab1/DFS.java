package bi.zum.lab1;

import cz.cvut.fit.zum.api.AbstractAlgorithm;

import cz.cvut.fit.zum.api.UninformedSearch;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.LinkedList;

import org.openide.util.lookup.ServiceProvider;
import java.util.List;
import cz.cvut.fit.zum.api.Node;
/**
 * Depth-first search
 *
 * @see http://en.wikipedia.org/wiki/Depth-first_search
 */
@ServiceProvider(service = AbstractAlgorithm.class, position = 10)
public class DFS extends AbstractAlgorithm implements UninformedSearch {

    private LinkedList<NodeWrapper> opened;
    private HashSet<NodeWrapper> closed;
    private List<Node> path;
    private NodeWrapper tmp;
    //private Map<Node, Node> prev;
    
    @Override
    public String getName() {
        return "DFS";
    }

    @Override
    public List<Node> findPath(Node startNode) {
        opened = new LinkedList<NodeWrapper>();
        closed = new HashSet<NodeWrapper>();
        tmp = new NodeWrapper(startNode, null);
        opened.add(tmp);
        NodeWrapper current;
        
        while(! opened.isEmpty()) {
            current = opened.getLast();
            
            if (current.isTarget())
                return backTrack(current);          
            
            for(Node y : current.expand()){
                tmp = new NodeWrapper(y, null);
                tmp.setPrev(current);
                
                if( !opened.contains(tmp) && !closed.contains(tmp))
                    opened.add(tmp);
             
            }
            
            closed.add(current);
            opened.remove(current);
        }
        return path;
    }
    
    public List<Node> backTrack(NodeWrapper current) {
        path = new ArrayList<Node>();
        
        while( current.getPrev() != null ) {
            path.add(current.getPrev().getNode());
            current = current.getPrev();
        }
        return path;
    }
}
