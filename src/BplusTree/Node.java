package BplusTree;

import database.Address;

import java.util.*;

import static BplusTree.Tree.NODE_SIZE;

public class Node {
    private final int nodeSize = NODE_SIZE ; // Tree.NODE_SIZE;
    private final int minLeafNodeSize = (int) (Math.floor((nodeSize + 1) / 2));
    private final int minNonLeafNodeSize = (int) (Math.floor(nodeSize / 2));
    private final Node root = Tree.getRoot();

    private boolean isRoot;
    private boolean isLeaf;

    private NonLeafNode parent;

    public ArrayList<Float> keys;

    public Node() {
        this.isRoot = false;
        this.isLeaf = false;
        this.keys = new ArrayList<Float>();
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
        return this.parent;
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

    public ArrayList<Float> getKeys() {return this.keys;}
    public Float getKeyAtIndex(int index) {
        return this.keys.get(index);
    }
    public Float getFirstKey () {
        return this.keys.get(0);
    }
    public Float getLastKey () {
        return this.keys.get(this.keys.size() - 1);
    }
    public int getNumberOfKeys () {
        return this.keys.size();
    }

    public void setKeyAtIndex(int index, float key) {
        this.keys.set(index, key);
    }
    public void insertKeyAtIndex(int index, float key) {
        this.keys.add(index, key);
    }
    public void removeLastKey() {
        this.keys.remove(keys.size()-1);
    }
    public float removeKeyAtIndex (int index) {
        return this.keys.remove(index);
    }

    public int binarySearchUpperBound(int l, int r, float key, boolean upperBound) {
        if (l > r) return l;

        int m = (l + r) / 2;
        float midKey = getKeyAtIndex(m);

        if (midKey < key) {
            return binarySearchUpperBound(m + 1, r, key, upperBound);
        } else if (midKey > key) {
            return binarySearchUpperBound(l, m - 1, key, upperBound);
        } else {
            while (m < getNumberOfKeys() && getKeyAtIndex(m) == key) m++;
            if (!upperBound) return m - 1;
            return m;
        }
    }

//    public int checkForLowerBound(float key) {
//        return binarySearchLowerBound(0, getNumberOfKeys() - 1, key);
//    }

//    public int binarySearchLowerBound(int l, int r, float key) {
//        if (l > r) return l;
//        int m = (l + r) / 2;
//        float midKey = getKeyAtIndex(m);
//
//        if (midKey < key) {
//            return binarySearchLowerBound(m + 1, r, key);
//        } else if (midKey > key) {
//            return binarySearchLowerBound(l, m - 1, key);
//        } else {
//            while (m >= 0 && getKeyAtIndex(m) == key) m--;
//            return m;
//        }
//    }

    public int binarySearchUpperBound(float key, boolean upperBound) {
        return binarySearchUpperBound(0, getNumberOfKeys() - 1, key, upperBound);
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


    public void sortedInsert (ArrayList<Float> keys, float key) {
        int i = 0;

        while (i < keys.size() && keys.get(i) < key) i++;
        keys.add(i, key);
        ensureSorted(keys);
    }

    public void ensureSorted(ArrayList<Float> keys) {
        ArrayList<Float> copiedKeys = new ArrayList<>(keys);
        Collections.sort(copiedKeys);
        if (!keys.equals(copiedKeys)) {
            System.out.println("Keys are not sorted!");
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
     * @param newKey
     * @param leafUpdate
     */
    public void updateKeysAfterDeletion (int index, float newKey, boolean leafUpdate) {
        // Checker for Index Validity
        // Update keys for this index only if it is a non-leaf node.
        if (index >= 0 && index < this.keys.size() && !leafUpdate) this.keys.set(index, newKey);
        // Recursion occurs continuously as long as current node is not a root node.
        if (parent != null && !parent.isLeaf()) {
            int childIndex = parent.getChildren().indexOf(this);

            if (childIndex >= 0) {
                if (childIndex > 0) {
                    parent.setKeyAtIndex(childIndex - 1, keys.get(0));

                }
                parent.updateKeysAfterDeletion(childIndex - 1, newKey, false);
            }
        } else if (parent != null && parent.isLeaf()) {
            parent.updateKeysAfterDeletion(index, newKey, false);
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
            if (!insertedNode) {
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
        ExperimentStats.addOneNode();

        newParent.addNewChild(this);
        newParent.addNewChild(newLeaf);

        newParent.keys = new ArrayList<Float>();
        newParent.keys.add(newLeaf.getKeyAtIndex(0));

        this.setParent(newParent);
        newLeaf.setParent(newParent);

    }



    //////////////// BELOW METHODS ARE FOR SPLITTING OF FULL LEAF/NON-LEAF NODES! //////////////////
    public LeafNode leafNodeSplit(float key, Address newRecord) {
        LeafNode currentLeaf = (LeafNode) (this);
        LeafNode newLeaf = new LeafNode();
        ExperimentStats.addOneNode();
        currentLeaf.records = new ArrayList<Address>();
        currentLeaf.records.add(newRecord);
        currentLeaf.mapping = new TreeMap<Float, ArrayList<Address>>();
        currentLeaf.mapping.put(key, currentLeaf.records);

        //// Leaving just enough nodes for the minimum leaf node size and moving the rest to new leaf node.
        int n = nodeSize - minLeafNodeSize + 1;
        int i = 0;
        float moveFromKey = 0;

        // Iterate through the key-value entries in the leaf node's mapping until the nth entry is found. This entry's key will be used as the starting point for moving keys to the new leaf node.
        for (Map.Entry<Float, ArrayList<Address>> entry : currentLeaf.mapping.entrySet()) {
            if (i == n) {
                moveFromKey = entry.getKey();
                break;
            }
            i++;
        }

        // Use of sorted map to ensure that records inserted in new leaf node is sorted.
        SortedMap<Float, ArrayList<Address>> newMap = currentLeaf.mapping.subMap(moveFromKey, true, currentLeaf.mapping.lastKey(), true);

        // Insert this new map into new leaf nodes mapping.
        newLeaf.mapping = new TreeMap<Float, ArrayList<Address>>(newMap);
        newMap.clear();

        // Add new key into this node's key.
        sortedInsert(this.keys, key);
        // Adding moved keys into the newLeaf's key list.
        newLeaf.keys = new ArrayList<Float>(this.keys.subList(n, this.keys.size()));// after nth index
        // Removing moved keys in this node.
        this.keys.subList(n, this.keys.size()).clear();

        // Used to allow both new leaf node and the current node's right node to correctly point at each other.
        if (currentLeaf.getRight() != null) {
            newLeaf.setRight(currentLeaf.getRight());
            currentLeaf.getRight().setLeft(newLeaf);
        }
        // ELse do this if this node is already the rightmost leaf node.
        currentLeaf.setRight(newLeaf);

        // Set new leaf nodes left node as this node.
        newLeaf.setLeft(((LeafNode) this));

        return newLeaf;
    }

    public NonLeafNode nonLeafSplit() {
        NonLeafNode currentNonLeaf = (NonLeafNode) this;
        NonLeafNode newNonLeaf = new NonLeafNode();
        ExperimentStats.addOneNode();

        float moveFromKey = currentNonLeaf.getKeyAtIndex(minNonLeafNodeSize);
        for (int i = currentNonLeaf.getNumberOfKeys(); i > 0; i--) {
            if (currentNonLeaf.getKeyAtIndex(i - 1) < moveFromKey) break;
            float currentKey = currentNonLeaf.getKeyAtIndex(i - 1);
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

    public void splitLeafNode(float key, Address record) {

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
            ExperimentStats.addOneNode();

            // Remove first key from new non leaf node and add key to the parent.
            newRoot.keys = new ArrayList<Float>();
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
