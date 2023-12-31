package BplusTree;

import database.Address;

import java.util.ArrayList;
import java.util.TreeMap;

import static BplusTree.Tree.NODE_SIZE;

public class LeafNode extends Node{
    /**
     * Simulates the mapping of each key (or pointer) to its respective set of records. An arraylist is used for each mapping to handle duplicate records for each key to point to. With no duplicates, array list will only contain 1 address.
     */
    public TreeMap<Float, ArrayList<Address>> mapping;
    /**
     * Represents a block of records on the disk
     */
    public ArrayList<Address> records;
    private LeafNode rightNode;

    /**
     * Used for ease of merging nodes with nodes on the left in the case where minimum number of keys in the leaf node is not met, although B+ Trees do not usually hold a left node pointer.
     */
    private LeafNode leftNode;

    public LeafNode() {
        super();
        setIsLeaf(true);
        setRight(null);
        setLeft(null);
    }

    public ArrayList<Address> getAddressesPointedByKey (float key) {
        return this.mapping.get(key);
    }
    public void insertAddressesOfKey (float key, ArrayList <Address> block) {
        mapping.put(key,block);
    }
    public void deleteAddressesOfKey (float key) {
        mapping.remove(key);
    }

    public void insertRecord (float key, Address record) {
        //Empty Leaf Node
        if (this.keys == null || this.records == null || this.mapping == null) {
            this.records = new ArrayList<Address>();
            this.records.add (record);

            this.mapping = new TreeMap<Float, ArrayList<Address>>();
            this.mapping.put(key, records);

            this.keys = new ArrayList <Float>();
            sortedInsert(this.keys, key);

        // If key exists in LeafNode, just attach new address to existing key address list.
        } else if (this.mapping.containsKey(key) && this.keys.contains(key)) {
            ArrayList <Address> existingBlock = mapping.get(key);
            existingBlock.add(record);
            mapping.put(key, existingBlock);
        }

        // Below: Cases where keys dont exist in partially filled leaf node.

        //This is for the case where leafnode is not full.
        else if (this.keys.size() < NODE_SIZE) { // NODE_SIZE
            this.records = new ArrayList<Address> ();
            this.records.add(record);

            this.mapping.put(key, records);
            sortedInsert(this.keys, key);
        }

        //For case where leafnode is full - Splitting of leaf node is necessary.
        else {
            this.splitLeafNode(key, record);
        }
    }

    public Node findNodeFromRoot (float key, Node node) {
        if (node == null) {
            return null;
        } else {
            for (Node child : ((NonLeafNode) node).getChildren()) {
                Node answer = findNodeFromRoot (key, child);
                if (answer != null) {
                    return answer;
                }
            }
        }
        return null;
    }

    public LeafNode getRight () {
        return this.rightNode;
    }
    public void setRight (LeafNode right) {
        this.rightNode = right;
    }
    public LeafNode getLeft() {
        return this.leftNode;
    }
    public void setLeft(LeafNode left) {
        this.leftNode = left;
    }


    public void deleteLeafNodeContents () {
        keys.clear();
        records.clear();
    }

    @Override
    public String toString() {
        return String.format("\nLeaf Node Contents - \nBlocks: %s\nRecords: %s\nLeft Node: %s\nRight Node: %s", mapping.toString(), records, leftNode, rightNode);
    }


}
