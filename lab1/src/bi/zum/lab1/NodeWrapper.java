package bi.zum.lab1;
 
import cz.cvut.fit.zum.api.Node;
import java.util.List;
 
/**
 * NodeWrapper is used for extending functionality of provided Node, we can
 * track here how we got to this Node, which simplifies the process of
 * backtracking
 *
 * @author Tomas Barton
 */
public class NodeWrapper implements Node {
 
    private Node node;
    private NodeWrapper prev = null;
 
    public NodeWrapper(Node n, NodeWrapper p) {
        this.node = n;
        this.prev = p;
    }
 
 
    public Node getNode() {
        return node;
    }
 
    public void setNode(Node node) {
        this.node = node;
    }
 
    public NodeWrapper getPrev() {
        return prev;
    }
 
    public void setPrev(NodeWrapper prev) {
        this.prev = prev;
    }
 
    @Override
    public double getX() {
        return node.getX();
    }
 
    @Override
    public double getY() {
        return node.getY();
    }
 
    @Override
    public List<Node> expand() {
        return node.expand();
    }
 
    @Override
    public boolean isTarget() {
        return node.isTarget();
    }
 
    @Override
    public int hashCode(){
        return node.hashCode();
    }
 
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NodeWrapper other = (NodeWrapper) obj;
        if (this.node != other.node && (this.node == null || !this.node.equals(other.node))) {
            return false;
        }
        return true;
    }
}
