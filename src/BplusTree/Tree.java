package BplusTree;

import database.Address;
import utils.DataInitialiser;

import java.util.ArrayList;

/**
 * Class representing our B+ Tree Index
 */
public class Tree {
    static final int NODE_SIZE = (DataInitialiser.BLOCK_SIZE - DataInitialiser.OVERHEAD)/(DataInitialiser.POINTER_SIZE+DataInitialiser.KEY_SIZE);
    private static Node root;

    public Tree() {
        root = createTreeNode();
    }

    public Node getRoot() {
        return Tree.root;
    }
    public static void setRoot(Node root) {
        Tree.root = root;
        root.setIsRoot(true);
    }

    public LeafNode createTreeNode() {
        LeafNode newRoot = new LeafNode();
        newRoot.setIsRoot(true);
        newRoot.setIsLeaf(true);
        setRoot(newRoot);
        return newRoot;
    }

    public void insertRecord (int key, Address record) {
        retrieveLeafToInsert(key).insertRecord(key, record);
    }

    public ArrayList<Address> searchKey (Node nodeToSearchFrom, int key) {
        ExperimentStats.addOneNodeReadQuery();

        // Search if key exists within the leaf node.
        if (nodeToSearchFrom.isLeaf()){
            int keyIndex = nodeToSearchFrom.binarySearchUpperBound(key, false);
            if (keyIndex >= 0 && keyIndex <= nodeToSearchFrom.getNumberOfKeys() - 1 && key == nodeToSearchFrom.getKeyAtIndex(keyIndex)) return ((LeafNode)nodeToSearchFrom).getAddressesPointedByKey(key);
            return null;
        }

        // Recursively go down the tree till we find the leaf node.
        else {
            int ptrIndex = nodeToSearchFrom.binarySearchUpperBound(key, true);
            Node childNode = ((NonLeafNode) nodeToSearchFrom).getSingleChild(ptrIndex);
            return (searchKey(childNode, key));
        }

    }

    public ArrayList<Address> searchValuesInRange(int minKey, int maxKey, Node nodeToSearchFrom) {
        ExperimentStats.addOneRangeQuery();
        ArrayList<Address> resultArray = new ArrayList<>();
        if (nodeToSearchFrom.isLeaf()) {
            int ptrIndex = nodeToSearchFrom.binarySearchUpperBound(minKey, false);
            LeafNode leaf = (LeafNode) nodeToSearchFrom;
            while (true) {
                if (ptrIndex == leaf.getNumberOfKeys()) {
                    // No existing index error check.
                    if (ptrIndex >= leaf.getNumberOfKeys()) throw new IllegalStateException("0 keys found due to invalid index.");
                    // No more next node to load.
                    if (leaf.getRight() == null) break;
                    // Iterate through the leaf nodes to find all relevant keys.
                    leaf = leaf.getRight();
                    ExperimentStats.addOneRangeQuery();
                    ptrIndex = 0;
                }
                // If max key is reached. Stop adding to results.
                if (leaf.getKeyAtIndex(ptrIndex) > maxKey) break;

                // Add record addresses to results.
                int key = leaf.getKeyAtIndex(ptrIndex);
                resultArray.addAll(leaf.getAddressesPointedByKey(key));

                ptrIndex++;
            }
            return (resultArray.size() > 0 ? resultArray : null);
        } else {
            // Recursively go down tree until the leaf nodes.
            int ptrIndex = nodeToSearchFrom.binarySearchUpperBound(minKey, true);
            Node childNode = ((NonLeafNode) nodeToSearchFrom).getSingleChild(ptrIndex);
            return (searchValuesInRange(minKey, maxKey, childNode));
        }
    }

    public void countNumberOfLevels(Node nodeToSearchFrom) {
        // Iterates down the height of the tree by going down the left side (since nodes are flushed to the left).
        while (!nodeToSearchFrom.isLeaf()) {
            NonLeafNode nonLeaf = (NonLeafNode) nodeToSearchFrom;
            nodeToSearchFrom = nonLeaf.getSingleChild(0);
            ExperimentStats.addOneLevel();
        }
        ExperimentStats.addOneLevel();
    }

    /////////////////////////////////////////////////////////////////
    public LeafNode retrieveLeafToInsert(int key) {
        if (Tree.root.isLeaf()) return (LeafNode)root;
        // Initialise
        NonLeafNode nodeToInsert = (NonLeafNode) getRoot();

        // While node isn't a leaf, keeping following the pointers to find correct node to insert
        int upperBoundIndex = 0;
        while (!nodeToInsert.getSingleChild(0).isLeaf()) {
            // Binary Search for Key Upper Bound Index
            upperBoundIndex = nodeToInsert.binarySearchUpperBound(key, true);
            if (upperBoundIndex >= nodeToInsert.getNumberOfKeys()) {
                upperBoundIndex = nodeToInsert.getNumberOfKeys()-1;
                nodeToInsert = (NonLeafNode) nodeToInsert.getSingleChild(nodeToInsert.getNumberOfKeys()-1);
            }
            else if (upperBoundIndex <= 0) {
                upperBoundIndex = 0;
                nodeToInsert = (NonLeafNode) nodeToInsert.getSingleChild(0);
            }
            else nodeToInsert = (NonLeafNode) nodeToInsert.getSingleChild(upperBoundIndex);
            if (nodeToInsert.isLeaf()) break;
        }

        return (LeafNode) ((NonLeafNode) nodeToInsert).getSingleChild(upperBoundIndex);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    public int findLowerBoundKey (int key) {
        NonLeafNode node = (NonLeafNode) root;
        int lowerBoundIndex = 0;
        while (!node.getSingleChild(0).isLeaf()) {
            // Binary Search for Key Upper Bound Index
            lowerBoundIndex = node.binarySearchLowerBound(key);
            if (lowerBoundIndex >= node.getNumberOfKeys()) {
                lowerBoundIndex = node.getNumberOfKeys()-1;
                node = (NonLeafNode) node.getSingleChild(node.getNumberOfKeys()-1);
            }
            else if (lowerBoundIndex <= 0) {
                lowerBoundIndex = 0;
                node = (NonLeafNode) node.getSingleChild(0);
            }
            else node = (NonLeafNode) node.getSingleChild(lowerBoundIndex+1);
            if (node.isLeaf()) break;
        }

        return node.getSingleChild(lowerBoundIndex).getFirstKey();
    }

    public ArrayList<Address> removeRecord (int key) {
        int lowerBoundKey = findLowerBoundKey(key);
        return removeNode(root, null, -99, -99, key, lowerBoundKey);
    }

    public ArrayList<Address> removeNode (Node nodeToSearchFrom, NonLeafNode parent, int parentPointerIndex, int parentKeyIndex, int key, int lowerBoundKey) {
        ArrayList<Address> recordToDelete = new ArrayList<>();
            if (!nodeToSearchFrom.isLeaf()) {
                // Find leaf node to delete from
                NonLeafNode nonLeafToSearchFrom = (NonLeafNode) nodeToSearchFrom;

                int pointerIndex = nodeToSearchFrom.binarySearchUpperBound(key, true);
                int keyIndex = pointerIndex - 1;

                Node node = nonLeafToSearchFrom.getSingleChild(pointerIndex);
                recordToDelete = removeNode(node, nonLeafToSearchFrom, pointerIndex, keyIndex, key, lowerBoundKey);

            } else {
                LeafNode leafNode = (LeafNode) nodeToSearchFrom;
                recordToDelete.addAll(leafNode.getAddressesPointedByKey(key));

                // Search for key index for error checking
                int keyIndex = nodeToSearchFrom.binarySearchUpperBound(key, false);
                // Error checking for non-existing key/index
                if ((keyIndex >= leafNode.getNumberOfKeys()) || (key != leafNode.getKeyAtIndex(keyIndex))) { return null;}

                // Remove key
                leafNode.removeKeyAtIndex(keyIndex);
                // Remove pointers
                leafNode.deleteAddressesOfKey(key);

                // Find ptr index and reassign key index.
                int ptrIndex = nodeToSearchFrom.binarySearchUpperBound(key, true);
                keyIndex = ptrIndex - 1;

                //// Recursive update of parents after key is removed.
                if ((keyIndex + 1) <= leafNode.getNumberOfKeys()) {
                    leafNode.updateKeysAfterDeletion(keyIndex, leafNode.getFirstKey(), false);
                } else {
                    leafNode.updateKeysAfterDeletion(keyIndex, leafNode.getFirstKey(), true);
                }
            }

            if (nodeToSearchFrom.isNotFull()) {
                fixInvalidTree(nodeToSearchFrom, parent, parentPointerIndex, parentKeyIndex);
            }

            return recordToDelete;
    }

    public void fixInvalidTree (Node nonFullNode, NonLeafNode parent, int parentPointerIndex, int parentKeyIndex) throws IllegalStateException {
        // If is root
        if (parent == null) {
            fixInvalidRoot(nonFullNode);
        // If is non-leaf
        } else if (!nonFullNode.isLeaf()){
            fixInvalidNonLeaf(nonFullNode, parent, parentPointerIndex, parentKeyIndex);
        } else if (nonFullNode.isLeaf()) {
            fixInvalidLeaf(nonFullNode, parent, parentPointerIndex, parentKeyIndex);
        } else {
            throw new IllegalStateException();
        }

    }

    private void fixInvalidRoot(Node nonFullNode) {
        // Is only node - clear as no tree needed
        if (nonFullNode.isLeaf()) ((LeafNode) nonFullNode).deleteLeafNodeContents();
        // Make 1st child the new root
        else {
            NonLeafNode nonLeafRoot = (NonLeafNode) nonFullNode;
            Node newRoot = nonLeafRoot.getSingleChild(0);
            root = newRoot;
            newRoot.setParent(null);
        }
    }

    private void fixInvalidLeaf (Node nonFullNode, NonLeafNode parent, int parentPointerIndex, int parentKeyIndex) throws IllegalStateException {
        int numChildrenOfNextParent = 0;
        int numChildrenOfThisParent = 0;

        LeafNode nonFullLeaf = (LeafNode) nonFullNode;
        LeafNode leftNode = nonFullLeaf.getLeft();
        LeafNode rightNode = nonFullLeaf.getRight();

        if (rightNode != null) numChildrenOfNextParent = rightNode.getParent().getNumOfChildren();
        if (nonFullNode.getParent() != null) numChildrenOfThisParent = nonFullNode.getParent().getNumOfChildren();

        //// Case 2: Attempt to borrow key from sibling node
        if (leftNode != null && leftNode.isAbleToGiveKey()) {
            borrowOneKeyLeaf(leftNode, nonFullLeaf, true, parent, parentKeyIndex);
        }
        else if (rightNode != null && rightNode.isAbleToGiveKey()) {
            borrowOneKeyLeaf(rightNode, nonFullLeaf, false, parent, parentKeyIndex + 1);
        }


        //// Case 3: Not possible to borrow key from sibling node. merge!

        // Merge with left node. Check if merging 2 nodes would make new node too full, and also check if removing 1 child from parent would cause it to be too empty.
        else if ((leftNode != null && (leftNode.getNumberOfKeys() + nonFullLeaf.getNumberOfKeys()) <= NODE_SIZE && (numChildrenOfThisParent > nonFullNode.getParent().getMinNonLeafNodeSize()))) {
            mergeLeafNodes(leftNode, nonFullLeaf, parent, parentPointerIndex, parentKeyIndex, false);

        // Merge with right node. Do the same checks.
        } else if (rightNode != null && (rightNode.getNumberOfKeys() + nonFullLeaf.getNumberOfKeys()) <= NODE_SIZE && (numChildrenOfNextParent > nonFullNode.getParent().getMinNonLeafNodeSize())) {
            mergeLeafNodes(nonFullLeaf, rightNode, parent, parentPointerIndex + 1, parentKeyIndex + 1, true);

        } else {
            throw new IllegalStateException("Can't fix invalid leaf, all cases fail!");
        }
    }

    private void fixInvalidNonLeaf(Node nonFullNode, NonLeafNode parent, int parentPointerIndex, int parentKeyIndex) throws IllegalStateException {

        // Attempt to load adjacent nodes (if any)
        NonLeafNode leftNonLeaf = null;
        NonLeafNode rightNonLeaf = null;
        try {
            rightNonLeaf = (NonLeafNode) parent.getSingleChild(parentPointerIndex + 1);
        } catch (Exception e) {
            System.out.print(e);
        }
        try {
            leftNonLeaf = (NonLeafNode) parent.getSingleChild(parentPointerIndex - 1);
        } catch (Exception e) {
            System.out.print(e);
        }


        //// Case 2: Attempt to borrow keys from adjacent nodes, left before right.
        if (rightNonLeaf == null && leftNonLeaf == null)
            throw new IllegalStateException("No adjacent nodes available to borrow/merge.");

        if (leftNonLeaf != null && leftNonLeaf.isAbleToGiveKey()) {
            borrowOneKeyNonLeaf(leftNonLeaf, (NonLeafNode) nonFullNode, true, parent, parentKeyIndex);

        } else if (rightNonLeaf != null && rightNonLeaf.isAbleToGiveKey()) {
            borrowOneKeyNonLeaf(rightNonLeaf, (NonLeafNode) nonFullNode, false, parent, parentKeyIndex + 1);
        }


        //// Case 3: Check if merging is possible.

        // Attempt to merge with left with the same checks as the leaf node checks. No checks for parent in case it is a root node.
        else if (leftNonLeaf != null &&
                (nonFullNode.getNumberOfKeys() + leftNonLeaf.getNumberOfKeys()) <= NODE_SIZE) {
            mergeNonLeaf(leftNonLeaf, (NonLeafNode) nonFullNode, parent,
                    parentPointerIndex, parentKeyIndex, true);
        }
        // Attempt to merge with right with the same checks as the leaf node checks.
        else if (rightNonLeaf != null &&
                (nonFullNode.getNumberOfKeys() + rightNonLeaf.getNumberOfKeys()) <= NODE_SIZE) {
            mergeNonLeaf((NonLeafNode) nonFullNode, rightNonLeaf, parent,
                    parentPointerIndex + 1, parentKeyIndex + 1, false);
        } else {
            throw new IllegalStateException("Can't borrow or merge from adjacent non-leaf nodes.");
        }
    }

    private void borrowOneKeyNonLeaf (NonLeafNode giver, NonLeafNode receiver, boolean giverOnLeft, NonLeafNode parent, int inBetweenKeyIdx) {
        int key;

        //// Take from left
        if (giverOnLeft) {
            // Remove last key
            giver.removeKeyAtIndex(giver.getNumberOfKeys() - 1);

            // Move last child
            Node nodeToMove = giver.getSingleChild(giver.getNumberOfKeys());
            giver.deleteChild(nodeToMove);
            receiver.addNewChild(nodeToMove);

        //// Take from right
        } else {
            // Remove 1st key
            giver.removeKeyAtIndex(0);

            // Move 1st child
            Node nodeToMove = giver.getSingleChild(0);
            giver.deleteChild(nodeToMove);
            receiver.addNewChild(nodeToMove);
        }

        receiver.getKeys().add(receiver.getNumberOfKeys(), receiver.getSingleChild(1).getFirstKey());
        key = receiver.getKeyAtIndex(0);

        // Find pointer and key indexers for receiver.
        int ptrIndex = receiver.binarySearchUpperBound(key, true);
        int keyIndex = ptrIndex - 1;

        // Update lower bound
        int lowerBound = root.binarySearchLowerBound(key);
        int newLowerBound;

        if ((keyIndex + 1) <= receiver.getNumberOfKeys()) newLowerBound = lowerBound;
        else {
            newLowerBound = root.binarySearchLowerBound(receiver.getKeyAtIndex(keyIndex + 1));
            parent.updateKeysAfterDeletion(inBetweenKeyIdx - 1, key, false);
        }
        parent.setKeyAtIndex(inBetweenKeyIdx, newLowerBound);
    }

    private void borrowOneKeyLeaf (LeafNode giver, LeafNode receiver, boolean giverOnLeft, NonLeafNode parent, int inBetweenKeyIdx) {
        int key;

        //// Take from left
        if (giverOnLeft) {
            int giverKey = giver.getLastKey();
            receiver.insertAddressesOfKey(giverKey, giver.getAddressesPointedByKey(giverKey));
            giver.deleteAddressesOfKey(giverKey);

            receiver.insertKeyAtIndex(0, giverKey);
            giver.removeLastKey();
            key = receiver.getKeyAtIndex(0);
            //// Take from right
        } else {
            int giverKey = giver.getFirstKey();
            receiver.insertAddressesOfKey(giverKey, giver.getAddressesPointedByKey(giverKey));
            giver.deleteAddressesOfKey(giverKey);

            giver.removeKeyAtIndex(0);
            receiver.insertKeyAtIndex(receiver.getNumberOfKeys(), giverKey);
            key = giver.getKeyAtIndex(0);
        }

        if (inBetweenKeyIdx >= 0) {
            if (parent.getNumberOfKeys() == inBetweenKeyIdx) {
                parent.setKeyAtIndex(inBetweenKeyIdx - 1, key);

                int lastParentChild = receiver.getParent().getKeys().size() - 1;// point to last child
                int lastParentChildKey = receiver.getParent().getSingleChild(receiver.getParent().getKeys().size())
                        .getFirstKey();
                if (giver.getParent().getSingleChild(giver.getParent().getChildren().size() - 1).getFirstKey() != key) {
                    receiver.getParent().setKeyAtIndex(lastParentChild, lastParentChildKey);
                }
            } else {
                parent.setKeyAtIndex(inBetweenKeyIdx, key);

                if (giver.getParent().getSingleChild(inBetweenKeyIdx + 1).getFirstKey() != key) {
                    giver.getParent().setKeyAtIndex(inBetweenKeyIdx,
                            giver.getParent().getSingleChild(inBetweenKeyIdx + 1).getFirstKey());
                }
            }

        } else {
            parent.setKeyAtIndex(inBetweenKeyIdx - 1, key);
        }

        int ptrIdx = receiver.binarySearchUpperBound(key, true);
        int keyIdx = ptrIdx - 1;

        LeafNode LeafNode = receiver;
        int lowerbound = root.binarySearchLowerBound(key);
        int newLowerBound = 0;

        if (LeafNode.getNumberOfKeys() >= (keyIdx + 1)) {
            newLowerBound = lowerbound;
        } else {
            newLowerBound = root.binarySearchLowerBound(LeafNode.getKeyAtIndex(keyIdx + 1));
            parent.updateKeysAfterDeletion(inBetweenKeyIdx - 1, parent.getSingleChild(inBetweenKeyIdx).getFirstKey(), false);
        }

    }

    private void mergeNonLeaf(NonLeafNode nodeToMergeTo, NonLeafNode current, NonLeafNode parent,
                                   int rightPointerIdx,
                                   int inBetweenKeyIdx, boolean mergeWithLeft) {
        int keyToRemove;

        if (mergeWithLeft) {

            int moveKeyCount = current.getNumberOfKeys();
            keyToRemove = nodeToMergeTo.getSingleChild(nodeToMergeTo.getNumberOfKeys()).getLastKey();

            for (int i = 0; i < moveKeyCount; i++) {
                nodeToMergeTo.getKeys().add(nodeToMergeTo.getNumberOfKeys(), current.getKeyAtIndex(i));
            }

            for (int i = 0; i < current.getChildren().size(); i++) {
                nodeToMergeTo.getChildren().add(current.getSingleChild(i));
            }

            nodeToMergeTo.getKeys().add(nodeToMergeTo.getNumberOfKeys(),
                    nodeToMergeTo.getSingleChild(nodeToMergeTo.getNumberOfKeys() + 1).getFirstKey());
            current.getParent().deleteChild(current);

        }

        else {
            int moveKeyCount = current.getNumberOfKeys();

            keyToRemove = current.getFirstKey();

            for (int i = 0; i < moveKeyCount; i++) {
                nodeToMergeTo.getKeys().add(0, current.getKeyAtIndex(i));
            }
            for (int i = 0; i < current.getChildren().size(); i++) {
                nodeToMergeTo.getChildren().add(current.getSingleChild(i));
            }
            nodeToMergeTo.getKeys().add(0, nodeToMergeTo.getSingleChild(1).getFirstKey());
            current.getParent().deleteChild(current);

        }

        int ptrIdx = nodeToMergeTo.binarySearchUpperBound(keyToRemove, true);
        int keyIdx = ptrIdx - 1;

        NonLeafNode LeafNode = nodeToMergeTo;
        int lowerbound = root.binarySearchLowerBound(keyToRemove);
        int newLowerBound = 0;

        if (LeafNode.getNumberOfKeys() >= (keyIdx + 1)) {
            newLowerBound = lowerbound;
        } else {
            newLowerBound = root.binarySearchLowerBound(LeafNode.getKeyAtIndex(keyIdx + 1)); // Get new lowerbound
            parent.updateKeysAfterDeletion(inBetweenKeyIdx - 1, keyToRemove, false);

        }
    }

    private void mergeLeafNodes(LeafNode nodeToMergeTo, LeafNode current, NonLeafNode parent,
                                int rightPointerIdx, int inBetweenKeyIdx, boolean mergetoright) {
        int removedKey = 0;
        int moveKeyCount = current.getNumberOfKeys();
        int NoOfChildren = current.getParent().getChildren().size();
        for (int i = 0; i < moveKeyCount; i++) {
            removedKey = current.removeKeyAtIndex(0);
            int leftLastIdx = nodeToMergeTo.getNumberOfKeys() - 1;
            nodeToMergeTo.insertKeyAtIndex(leftLastIdx + 1, removedKey);
            nodeToMergeTo.insertAddressesOfKey(removedKey, current.getAddressesPointedByKey(removedKey));
            current.deleteAddressesOfKey(removedKey);

        }

        parent.deleteChild(current);
        if ((parent.getChildren().size()) == (parent.getNumberOfKeys())) {
        } else {
            parent.removeKeyAtIndex(inBetweenKeyIdx);
        }

        if (mergetoright == true) {
            if (current.getRight() != null) {
                LeafNode currentNext = current.getRight();
                currentNext.setLeft(current.getLeft());
            }

            nodeToMergeTo.setRight(current.getRight());
            if (current.getNumberOfKeys() == 0) {

                NonLeafNode currParent = current.getParent();
                currParent.deleteChild(current);
                currParent.removeKeyAtIndex(0);
            }
        } else {

            if (current.getLeft() != null) {
                LeafNode currentPrev = current.getLeft();
                if (currentPrev != null && (currentPrev.getLeft() != null)) {
                    currentPrev.getLeft().setLeft(current.getLeft());
                }

            }

            if (current.getRight() != null) {
                nodeToMergeTo.setRight(current.getRight());
                current.getRight().setLeft(nodeToMergeTo);
            }
            if (current.getNumberOfKeys() == 0) {

                NonLeafNode currParent = current.getParent();
                currParent.deleteChild(current);
                if (inBetweenKeyIdx < 0) {
                    currParent.removeKeyAtIndex(inBetweenKeyIdx + 1);

                } else if (currParent.getNumberOfKeys() > 0) {

                    currParent.removeKeyAtIndex(inBetweenKeyIdx);
                } else {
                    currParent.removeKeyAtIndex(0);
                }

            } else {

                NonLeafNode currParent = current.getRight().getParent();
                currParent.deleteChild(current);

                if ((currParent.getNumberOfKeys() > currParent.getMinNonLeafNodeSize())
                        && (currParent.getChildren().size() > current.getMinNonLeafNodeSize())) {
                    currParent.removeKeyAtIndex(0);

                }
            }
        }

        int lowerbound = root.binarySearchLowerBound(removedKey);
        int newLowerBound = 0;
        if (current.getParent().getNumberOfKeys() >= NoOfChildren) {
            newLowerBound = lowerbound;
        } else {
            newLowerBound = current.getParent().getSingleChild(0).getFirstKey();

            if (inBetweenKeyIdx == 0) {
                // inBetweenKeyIdx is 0
            } else {
                current.getParent().updateKeysAfterDeletion(inBetweenKeyIdx - 1, newLowerBound, true);
            }
        }

    }

}
