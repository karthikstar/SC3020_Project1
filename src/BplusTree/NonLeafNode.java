package BplusTree;

import java.util.ArrayList;

/**
 * Class to implement the additional attributes and methods for non leaf nodes in our B+ Tree.
 */
public class NonLeafNode extends Node{
    ArrayList<Node> children;

    public NonLeafNode() {
        super();
        children = new ArrayList<Node> ();
        setIsLeaf(false);
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public Node getSingleChild(int index) {
        return children.get(index);
    }

    public void addNewChild (Node newchild) {
        this.children.add(newchild);
    }

    public void deleteChild (Node deletingchild) {
        this.children.remove(deletingchild);
    }

    public int getNumOfChildren () {return this.children.size();}
}
