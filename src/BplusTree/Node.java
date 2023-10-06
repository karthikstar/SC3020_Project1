package BplusTree;

import database.Address;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Node {
    private final int nodeSize = Tree.NODE_SIZE;
    private final int minLeafNodeSize = (int) (Math.floor((nodeSize + 1) / 2));
    private final int minNonLeafNodeSize = (int) (Math.floor(nodeSize / 2));

    private boolean isRoot;
    private boolean isLeaf;

    private NonLeafNode parent;

    public ArrayList<Integer> keys;

    public Node() {
        this.isRoot = false;
        this.isLeaf = false;
        this.keys = new ArrayList<Integer>();
    }

    public int getMinLeafNodeSize() {
        return minLeafNodeSize;
    }

    public int getMinNonLeafNodeSize() {
        return minNonLeafNodeSize;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setIsRoot(boolean root) {
        isRoot = root;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public NonLeafNode getParent() {
        return parent;
    }

    public void setParent(NonLeafNode parent) {
        if (this.isRoot()) {
            this.setIsRoot(false);
            parent.setIsRoot(true);
            parent.setIsLeaf(false);
            Tree.setRoot(parent);
        } else {
            parent.setIsLeaf(false);
        }
        this.parent = parent;
    }

    public int getKeyAtIndex(int index) {
        return this.keys.get(index);
    }
    public int getFirstKey () {
        return this.keys.get(0);
    }
    public int getLastKey () {
        return this.keys.get(this.keys.size() - 1);
    }
    public int getNumberOfKeys () {
        return this.keys.size();
    }

    public void setKeyAtIndex(int index, int key) {
        this.keys.set(index, key);
    }
    public void insertKeyAtIndex(int index, int key) {
        this.keys.add(index, key);
    }
    public void removeLastKey() {
        this.keys.remove(keys.size()-1);
    }
    public int removeKeyAtIndex (int index) {
        return this.keys.remove(index);
    }

    public int binarySearch(int l, int r, int key, boolean upperBound) {
        if (l > r) return l;

        int m = (l + r) / 2;
        int midKey = getKeyAtIndex(m);

        if (midKey < key) {
            return binarySearch(m + 1, r, key, upperBound);
        } else if (midKey > key) {
            return binarySearch(l, m - 1, key, upperBound);
        } else {
            while (m < getNumberOfKeys() && getKeyAtIndex(m) == key)
                m++;
            if (!upperBound)
                return m - 1;
            return m;
        }
    }

    public int binarySearchForKeyIndex (int key, boolean upperBound) {
        return binarySearch(0, getNumberOfKeys() - 1, key, upperBound);
    }

    public boolean isNotFull () {
        if (isRoot()) {
            return this.getNumberOfKeys() < 1;
        } else if (isLeaf()) {
            return this.getNumberOfKeys() < (nodeSize + 1)/2;
        } else {
            return this.getNumberOfKeys() < nodeSize/2;
        }
    }

    public boolean isAbleToGiveKey () {
        if (isLeaf()) return getNumberOfKeys() - 1 >= (nodeSize + 1)/2;
        else return getNumberOfKeys() - 1 >= nodeSize/2;
    }


    public void sortedInsert (ArrayList<Integer> keys, int key) {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i) >= key) {
                keys.add(i, key);
                return;
            }
        }
    }

    public void insertChildToNonLeafNode (NonLeafNode parent, NonLeafNode child) {
        int i = 0;
        while (i < parent.getNumberOfKeys() && parent.getKeyAtIndex(i) < child.getKeyAtIndex(0)) {
            i++;
        }
        parent.children.add(i + 1, child);
    }

    /**
     * Performs a key update with recursive key updates until the root.
     * @param index
     * @param newkey
     * @param leafUpdate
     */
    public void updateKeysAfterDeletion (int index, int newkey, boolean leafUpdate) {
        // Checker for Index Validity
        // Update keys for this index only if it is a non-leaf node.
        if (index >= 0 && index < this.keys.size() && !leafUpdate) this.keys.set(index, newkey);
        // Recursion occurs continuously as long as current node is not a root node.
        if (this.parent != null){
            int myIndex = parent.getChildren().indexOf(this);
            if (myIndex > 0) parent.setKeyAtIndex(myIndex-1, keys.get(0));
            parent.updateKeysAfterDeletion(myIndex-1, newkey, false);
        }
    }

    public void addNewLeafNodeToParent(LeafNode newLeaf) {
        // Used to iterate through parent's children indexes.
        int index = 0;
        boolean insertedNode = false;

        try {
            for (Node parentKey : this.getParent().getChildren()) {
                // Insert new leaf at spot where our new nodes biggest key is smaller than next nodes smallest key
                if (newLeaf.getKeyAtIndex(newLeaf.getNumberOfKeys() - 1) < parentKey.getKeyAtIndex(0)) {
                    this.getParent().getChildren().add(index, newLeaf);
                    this.getParent().keys.add(index - 1, newLeaf.getKeyAtIndex(0));
                    insertedNode = true;
                    break;
                }
                index++;
            }
            // If nothing is inserted, just insert to the end.
            if (insertedNode == false) {
                this.getParent().getChildren().add(newLeaf);
                this.getParent().keys.add(newLeaf.getKeyAtIndex(0));
            }

        } catch (Exception e) {
            this.getParent().getChildren().add(newLeaf);
            this.getParent().keys.add(newLeaf.getKeyAtIndex(0));
        }

        newLeaf.setParent(this.getParent());

        // If size exceeds what node allows, split the parent node!
        if (this.getParent().getNumberOfKeys() > nodeSize) { this.getParent().splitNonLeafNode();}

    }

    public void createNewParent (LeafNode newLeaf) {
        NonLeafNode newParent = new NonLeafNode();

        newParent.addNewChild(this);
        newParent.addNewChild(newLeaf);

        newParent.keys.add(newLeaf.getKeyAtIndex(0));

        this.setParent(newParent);
        newLeaf.setParent(newParent);

    }



    //////////////// BELOW METHODS ARE FOR SPLITTING OF FULL LEAF/NON-LEAF NODES! //////////////////
    public LeafNode leafNodeSplit(int key, Address newRecord) {
        LeafNode currentLeaf = (LeafNode) (this);
        LeafNode newLeaf = new LeafNode();
        currentLeaf.records = new ArrayList<Address>();
        currentLeaf.records.add(newRecord);
        currentLeaf.mapping = new TreeMap<Integer, ArrayList<Address>>();
        currentLeaf.mapping.put(key, currentLeaf.records);

        //// Leaving just enough nodes for the minimum leaf node size and moving the rest to new leaf node.
        int n = nodeSize - minLeafNodeSize + 1;
        int i = 0;
        int moveFromKey = 0;

        // Iterate through the key-value entries in the leaf node's mapping until the nth entry is found. This entry's key will be used as the starting point for moving keys to the new leaf node.
        for (Map.Entry<Integer, ArrayList<Address>> entry : currentLeaf.mapping.entrySet()) {
            if (i == n) {
                moveFromKey = entry.getKey();
                break;
            }
            i++;
        }

        // Use of sorted map to ensure that records inserted in new leaf node is sorted.
        SortedMap<Integer, ArrayList<Address>> newMap = currentLeaf.mapping.subMap(moveFromKey, true, currentLeaf.mapping.lastKey(), true);

        // Insert this new map into new leaf nodes mapping.
        newLeaf.mapping = new TreeMap<Integer, ArrayList<Address>>(newMap);
        newMap.clear();

        // Add new key into this node's key.
        sortedInsert(this.keys, key);
        // Adding moved keys into the newLeaf's key list.
        newLeaf.keys = new ArrayList<Integer>(this.keys.subList(n, this.keys.size()));// after nth index
        // Removing moved keys in this node.
        this.keys.subList(n, this.keys.size()).clear();

        // Used to allow both new leaf node and the current node's right node to correctly point at each other.
        if (currentLeaf.getRight() != null) {
            newLeaf.setRight(((LeafNode) this).getRight());
            currentLeaf.getRight().setLeft(newLeaf);
        }
        // ELse do this if this node is already the rightmost leaf node.
        currentLeaf.setRight(newLeaf);

        // Set new leaf nodes left node as this node.
        newLeaf.setLeft(((LeafNode) this));

        return newLeaf;
    }

    public NonLeafNode nonLeafSplit() {
        NonLeafNode currentNonLeaf = (NonLeafNode) (this);
        NonLeafNode newNonLeaf = new NonLeafNode();

        int moveFromKey = currentNonLeaf.getKeyAtIndex(minNonLeafNodeSize);
        for (int i = currentNonLeaf.getNumberOfKeys(); i > 0; i--) {
            if (currentNonLeaf.getKeyAtIndex(i - 1) < moveFromKey) break;
            int currentKey = currentNonLeaf.getKeyAtIndex(i - 1);
            Node currentChild = currentNonLeaf.getSingleChild(i);

            // Add children and keys to new non leaf node
            newNonLeaf.children.add(0, currentChild);
            newNonLeaf.keys.add(0, currentKey);
            currentChild.setParent(newNonLeaf);

            // Remove children and keys from current node
            currentNonLeaf.deleteChild(currentNonLeaf.getSingleChild(i));
            currentNonLeaf.keys.remove(i - 1);
        }
        return newNonLeaf;
    }

    public void splitLeafNode(int key, Address record) {

        LeafNode newNode = this.leafNodeSplit(key, record);

        // If current leaf node has parent, attempt to add this newNode to parent.
        if (this.getParent() != null) {
            this.addNewLeafNodeToParent(newNode);

            // If parent keys exceed size, split the parent node.
            if (this.getParent().getNumberOfKeys() > nodeSize) this.getParent().splitNonLeafNode();
        }

        // No parent for new leaf node, so create one.
        else {this.createNewParent(newNode);}
    }

    public void splitNonLeafNode() {
        NonLeafNode newNonLeaf = this.nonLeafSplit();

        if (this.getParent() != null) {
            insertChildToNonLeafNode(this.getParent(), newNonLeaf);
            newNonLeaf.setParent(this.getParent());

            // Remove first key from new non leaf node and add key to the parent.
            sortedInsert(this.getParent().keys, newNonLeaf.getKeyAtIndex(0));
            newNonLeaf.keys.remove(0);

            // If parent node full, split it.
            if (this.getParent().getNumberOfKeys() > nodeSize) {this.getParent().splitNonLeafNode();}

        } else {
            // Need to create new root!
            NonLeafNode newRoot = new NonLeafNode();

            // Remove first key from new non leaf node and add key to the parent.
            newRoot.keys.add(newNonLeaf.getKeyAtIndex(0));
            newNonLeaf.keys.remove(0);

            // Set parents to new root.
            this.setParent(newRoot);
            newNonLeaf.setParent(newRoot);

            // Make current non leaf node and new non leaf node the children of this new root.
            newRoot.addNewChild(this);
            newRoot.addNewChild(newNonLeaf);

            Tree.setRoot(newRoot);
        }
    }
}
