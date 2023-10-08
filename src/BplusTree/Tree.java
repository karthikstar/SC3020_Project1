package BplusTree;

import database.Address;
import database.Disk;
import database.Record;
import utils.DataInitialiser;

import java.util.ArrayList;

/**
 * Class representing our B+ Tree Index
 */
public class Tree {
    static final int NODE_SIZE = (DataInitialiser.BLOCK_SIZE - DataInitialiser.OVERHEAD)/(DataInitialiser.POINTER_SIZE+DataInitialiser.KEY_SIZE);
    static Node root;

    public Tree() {
        root = createTreeNode();
    }

    public void printTree(Node node, String pre) {
        if (node == null) return;
        if (node.isLeaf()) {
            LeafNode leaf = (LeafNode) node;
            System.out.print(pre + "LeafNode: ");
            for (float key : leaf.getKeys()) {
                System.out.print(key + " ");
            }
            System.out.println();
        } else {
            NonLeafNode nonLeaf = (NonLeafNode) node;
            System.out.print(pre + "NonLeafNode: ");
            for (float key : nonLeaf.getKeys()) {
                System.out.print(key + " ");
            }
            System.out.println();
            for (Node child : nonLeaf.getChildren()) {
                printTree(child, pre + "  ");
            }

        }

    }
    public static void runExptTwo(Tree tree) {
        System.out.println("---------EXPERIMENT TWO---------");
        ExperimentStats stats = new ExperimentStats();
        System.out.println("Parameter n of the B+ tree: " + NODE_SIZE);
        System.out.printf("Number of nodes of the B+ tree: %d\n", stats.getTotalNumberOfNodes());
        tree.countNumberOfLevels(Tree.getRoot());
        System.out.printf("Number of Levels of the B+ tree: %d\n", stats.getTotalHeight());
        System.out.println("Contents Of the Root Node: " + Tree.getRoot().keys);
        System.out.println("B+ Tree Layout: ");
        tree.printTree(Tree.getRoot(), " ");
        System.out.println("-----END OF EXPERIMENT TWO------");
    }

    public static void runExptThree(Disk disk, Tree tree) {
        System.out.println("---------EXPERIMENT THREE---------");
        ExperimentStats stats = new ExperimentStats();

        long startTime = System.nanoTime();
        ArrayList<Address> resultAddress = tree.searchKey(root, 0.5f);
        long endTime = System.nanoTime();
        double totalFG3PCTHome = 0;
        int totalRecordCount = 0;
        if (resultAddress != null) {
            for (Address address : resultAddress) {
                Record record = disk.retrieveRecord(address);
                System.out.print("\n B+TreeSearch - Found " + record);
                totalFG3PCTHome += record.getFG3_PCT_home();
                totalRecordCount++;
            }
        }
        System.out.printf("\n\nNo. of Index Nodes the process accesses: %d\n", stats.getTotalNumberOfNodeReadQueries());
        System.out.printf("No. of Data Blocks the process accesses: %d\n", disk.getNoOfBlockAccess());
        System.out.printf("Average of 'FG3_PCT_home' of the records returned: %.2f\n",
                (double) totalFG3PCTHome / totalRecordCount);
        long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
        System.out.printf("Running time of retrieval process: %d nanoseconds\n", duration);
        startTime = System.nanoTime();
        int bruteForceAccessCount = disk.BFSearch(0.5f, 0.5f);
        endTime = System.nanoTime();
        System.out.printf("Number of Data Blocks Accessed by Brute Force: %d\n", bruteForceAccessCount);
        System.out.printf("Linear Time Accessed by Brute Force: %d nanoseconds\n", endTime - startTime);
        System.out.printf("Reduction In No. of Data Blocks accessed due to LRU Cache: %d\n ", disk.getNoOfBlockAccessReduced());

        System.out.println("-----END OF EXPERIMENT THREE------");
    }

    public static void runExptFour(Disk disk, Tree tree) {
        System.out.println("---------EXPERIMENT FOUR---------");
        ExperimentStats stats = new ExperimentStats();

        System.out.println("Records with the attribute 'FG_PCT_home' from 0.6 to 1.0, both inclusively: ");
        long startTime = System.nanoTime();
        ArrayList<Address> resultAdd = tree.searchValuesInRange(0.6f, 1.0f, root);
        long endTime = System.nanoTime();
        double totalFG3PCTHome = 0;
        int totalCount = 0;
        ArrayList<Record> results = new ArrayList<>();
        if (resultAdd != null) {
            for (Address add : resultAdd) {
                Record record = disk.retrieveRecord(add);
                System.out.print("\n B+TreeSearch - Found " + record);
                results.add(record);
                totalFG3PCTHome += record.getFG3_PCT_home();
                totalCount++;
            }
        }
        System.out.printf("\n\nNo. of Index Nodes the process accesses: %d\n", stats.getTotalNumberOfNodeReadQueries());
        System.out.printf("No. of Data Blocks the process accesses: %d\n", disk.getNoOfBlockAccess());
        System.out.printf("Average of 'FG3_PCT_home' of the records accessed: %.2f",
                (double) totalFG3PCTHome / totalCount);
        long duration = (endTime - startTime);
        System.out.printf("\nRunning time of retrieval process: %d nanoseconds\n", duration);
        startTime = System.nanoTime();
        int bruteForceAccessCount = disk.BFSearch(0.6f, 1);
        endTime = System.nanoTime();
        System.out.printf("Number of Data Blocks Accessed by Brute Force (0.6<=FG_PCT_home<=1): %d\n",
                bruteForceAccessCount);
        System.out.printf("Linear Time Accessed by Brute Force (0.6<=FG_PCT_home<=1): %d nanoseconds\n", endTime - startTime);
        System.out.printf("Reduction In No. of Data Blocks accessed due to LRU Cache: %d\n ", disk.getNoOfBlockAccessReduced());

        System.out.println("-----END OF EXPERIMENT FOUR------");
    }

    public static void runExptFive(Disk disk, Tree tree) {
        System.out.println("---------EXPERIMENT FIVE---------");
        ExperimentStats stats = new ExperimentStats();

        System.out.println("-- Deleting all records with 'FG_PCT_home' below 0.35 inclusively -- ");
        long startTime = System.nanoTime();
        ArrayList<Address> deletedAdd = tree.deleteKeysInRange(0.35f, root);

        disk.removeRecord(deletedAdd);
        long endTime = System.nanoTime();
        System.out.printf("No. of Nodes in updated B+ tree: %d\n", stats.getTotalNumberOfNodes());
        tree.countNumberOfLevels(tree.getRoot());
        System.out.printf("No. of Levels in updated B+ tree: %d\n", stats.getTotalHeight());
        System.out.printf("\nContent of the root node in updated B+ tree: %s\n", getRoot().keys);
        long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
        System.out.printf("Running time of retrieval process: %d nanoseconds\n", duration);
        System.out.println("Number of Data Blocks Accessed by Brute Force (FG_PCT_home <= 0.35):");
        startTime = System.nanoTime();
        int bruteForceAccessCount = disk.BFSearch(0.35f);
        endTime = System.nanoTime();
        System.out.printf("Number of Data Blocks Accessed by Brute Force (FG_PCT_home <= 0.35): %d\n", bruteForceAccessCount);
        System.out.printf("Linear Time Accessed by Brute Force (FG_PCT_home <= 0.35): %d nanoseconds\n", endTime - startTime);
        System.out.printf("Reduction In No. of Data Blocks accessed due to LRU Cache: %d\n ", disk.getNoOfBlockAccessReduced());

        System.out.println("-----END OF EXPERIMENT FIVE------");
    }

    public ArrayList<Address> deleteKeysInRange (float maxKey, Node nodeToSearchFrom) {
        ArrayList<Float> listOfKeysToDelete = searchValuesInRange2(0, maxKey, nodeToSearchFrom);
        System.out.println("Keys to be deleted: " + listOfKeysToDelete);
        ArrayList<Address> listOfAddresses = new ArrayList<>();
        ArrayList<Address> listOfAddressesToDelete = new ArrayList<>();
        for (float key : listOfKeysToDelete) {
            listOfAddresses.addAll(removeNode(root, null, -1, -1, key));
            if (listOfAddresses.size() > 0) {
                listOfAddressesToDelete.addAll(listOfAddresses);
                listOfAddresses.clear();
            }
        }
        return listOfAddressesToDelete;
    }

    public static Node getRoot() {
        return root;
    }
    public static void setRoot(Node rootNode) {
        root = rootNode;
        root.setIsRoot(true);
    }

    public LeafNode createTreeNode() {
        LeafNode newRoot = new LeafNode();
        ExperimentStats.addOneNode();
        newRoot.setIsRoot(true);
        newRoot.setIsLeaf(true);
        setRoot(newRoot);
        return newRoot;
    }

    public void addRecord (float key, Address record) {
        retrieveLeafToInsert(key).insertRecord(key, record);
    }

    public ArrayList<Address> searchKey (Node nodeToSearchFrom, float key) {
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

    public ArrayList<Address> searchValuesInRange(float minKey, float maxKey, Node nodeToSearchFrom) {
        ExperimentStats.addOneRangeQuery();
        ArrayList<Address> resultArray = new ArrayList<>();
        if (nodeToSearchFrom.isLeaf()) {
            int ptrIndex = nodeToSearchFrom.binarySearchUpperBound(minKey, false);
            LeafNode leaf = (LeafNode) nodeToSearchFrom;
            while (true) {
                if (ptrIndex == leaf.getNumberOfKeys()) {
                    // No more next node to load.
                    if (leaf.getRight() == null) break;
                    // Iterate through the leaf nodes to find all relevant keys.
                    leaf = leaf.getRight();
                    // No existing index error check.
                    //if (ptrIndex >= leaf.getNumberOfKeys()) throw new IllegalStateException("0 keys found due to invalid index.");
                    ExperimentStats.addOneRangeQuery();
                    ptrIndex = 0;
                }
                // If max key is reached. Stop adding to results.
                if (leaf.getKeyAtIndex(ptrIndex) > maxKey) break;

                // Add record addresses to results.
                float key = leaf.getKeyAtIndex(ptrIndex);
                ArrayList<Address> addresses = leaf.getAddressesPointedByKey(key);
                if (addresses != null) {
                    resultArray.addAll(addresses);
                }

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

    public ArrayList<Float> searchValuesInRange2(float minKey, float maxKey, Node nodeToSearchFrom) {
        ExperimentStats.addOneRangeQuery();
        ArrayList<Float> resultArray = new ArrayList<>();
        int ptrIndex = -1;
        if (nodeToSearchFrom.isLeaf()) {
            ptrIndex = nodeToSearchFrom.binarySearchUpperBound(minKey, false);
            if (ptrIndex == -1) ptrIndex = 0;
            LeafNode leaf = (LeafNode) nodeToSearchFrom;
            while (true) {
                if (ptrIndex == leaf.getNumberOfKeys()) {
                    // No more next node to load.
                    if (leaf.getRight() == null) break;
                    // Iterate through the leaf nodes to find all relevant keys.
                    leaf = leaf.getRight();
                    // No existing index error check.
                    //if (ptrIndex >= leaf.getNumberOfKeys()) throw new IllegalStateException("0 keys found due to invalid index.");
                    ExperimentStats.addOneRangeQuery();
                    ptrIndex = 0;
                }
                // If max key is reached. Stop adding to results.
                if (leaf.getKeyAtIndex(ptrIndex) > maxKey) break;

                // Add record addresses to results.
                float key = leaf.getKeyAtIndex(ptrIndex);
                //ArrayList<Address> addresses = leaf.getAddressesPointedByKey(key);
                resultArray.add(key);

                ptrIndex++;
            }
            return (resultArray.size() > 0 ? resultArray : null);
        } else {
            // Recursively go down tree until the leaf nodes.
            ptrIndex = nodeToSearchFrom.binarySearchUpperBound(minKey, true);
            Node childNode = ((NonLeafNode) nodeToSearchFrom).getSingleChild(ptrIndex);
            return (searchValuesInRange2(minKey, maxKey, childNode));
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
    public LeafNode retrieveLeafToInsert(float key) {
        if (root.isLeaf()) {
            setRoot(root);
            return (LeafNode)root;
        }
        // Initialise
        ArrayList<Float> keys;
        Node nodeToInsert = getRoot();

        // While node isn't a leaf, keeping following the pointers to find correct node to insert
        while (!((NonLeafNode) nodeToInsert).getSingleChild(0).isLeaf()) {
            keys = nodeToInsert.getKeys();
            for (int i = keys.size() - 1; i >= 0; i--) {
                if (nodeToInsert.getKeyAtIndex(i) <= key) {
                    nodeToInsert = ((NonLeafNode) nodeToInsert).getSingleChild(i + 1);
                    break;
                } else if (i == 0) {
                    nodeToInsert = ((NonLeafNode) nodeToInsert).getSingleChild(0);
                }
            }
            if (nodeToInsert.isLeaf()) {
                break;
            }
        }
        keys = nodeToInsert.getKeys();

        for (int i = keys.size() - 1; i >= 0; i--) {
            if (keys.get(i) <= key) {
                return (LeafNode) ((NonLeafNode) nodeToInsert).getSingleChild(i + 1);
            }
        }
        return (LeafNode) ((NonLeafNode) nodeToInsert).getSingleChild(0);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    public float findLowerBoundKey (float key) {
        NonLeafNode node = (NonLeafNode) root;
        int lowerBoundIndex = 0;
        while (!node.getSingleChild(0).isLeaf()) {
            // Binary Search for Key Upper Bound Index
            lowerBoundIndex = checkForLowerBound(key);
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

    public int checkForLowerBound(float key) {
        Node node = root;
        float lowerbound = 0;

        while (!node.isLeaf()) {
            if (node instanceof NonLeafNode) {
                NonLeafNode nonLeafNode = (NonLeafNode) node;
                int i;
                for (i = nonLeafNode.getNumberOfKeys() - 1; i >= 0; i--) {
                    if (key >= nonLeafNode.getKeyAtIndex(i)) {
                        break;
                    }
                }
                node = nonLeafNode.getSingleChild(i + 1);
            } else if (node instanceof LeafNode) {
                break;
            }
        }

        if (node.isLeaf()) {
            LeafNode leafNode = (LeafNode) node;

            while (!leafNode.isLeaf()) {
                // Handle LeafNode here (if needed)
                break;
            }

            lowerbound = leafNode.getKeyAtIndex(0);
        }

        return (int) lowerbound;
    }




    public ArrayList<Address> removeRecord (float key) {
        float lowerBoundKey = findLowerBoundKey(key);
        return removeNode(root, null, -99, -99, key);
    }

    public ArrayList<Address> removeNode (Node nodeToSearchFrom, NonLeafNode parent, int parentPointerIndex, int parentKeyIndex, float key) {
        ArrayList<Address> recordToDelete = new ArrayList<>();
            if (!nodeToSearchFrom.isLeaf()) {
                // Find leaf node to delete from
                NonLeafNode nonLeafToSearchFrom = (NonLeafNode) nodeToSearchFrom;

                int pointerIndex = nodeToSearchFrom.binarySearchUpperBound(key, true);
                int keyIndex = pointerIndex - 1;

                Node node = nonLeafToSearchFrom.getSingleChild(pointerIndex);
                recordToDelete = removeNode(node, nonLeafToSearchFrom, pointerIndex, keyIndex, key);

            } else {
                LeafNode leafNode = (LeafNode) nodeToSearchFrom;
                if (leafNode.getAddressesPointedByKey(key) != null) recordToDelete.addAll(leafNode.getAddressesPointedByKey(key));

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
        float key;

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
        int lowerBound = checkForLowerBound(key);
        int newLowerBound;

        if ((keyIndex + 1) <= receiver.getNumberOfKeys()) newLowerBound = lowerBound;
        else {
            newLowerBound = checkForLowerBound(receiver.getKeyAtIndex(keyIndex + 1));
            parent.updateKeysAfterDeletion(inBetweenKeyIdx - 1, key, false);
        }
        parent.setKeyAtIndex(inBetweenKeyIdx, newLowerBound);
    }

    private void borrowOneKeyLeaf (LeafNode giver, LeafNode receiver, boolean giverOnLeft, NonLeafNode parent, int inBetweenKeyIdx) {
        float key;

        //// Take from left
        if (giverOnLeft) {
            float giverKey = giver.getLastKey();

            // Move pointers
            receiver.insertAddressesOfKey(giverKey, giver.getAddressesPointedByKey(giverKey));
            giver.deleteAddressesOfKey(giverKey);

            receiver.insertKeyAtIndex(0, giverKey);
            giver.removeLastKey();
            key = receiver.getKeyAtIndex(0);

        //// Take from right
        } else {
            float giverKey = giver.getFirstKey();
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
                float lastParentChildKey = receiver.getParent().getSingleChild(receiver.getParent().getKeys().size())
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
        int lowerbound = checkForLowerBound(key);
        int newLowerBound = 0;

        if (LeafNode.getNumberOfKeys() >= (keyIdx + 1)) {
            newLowerBound = lowerbound;
        } else {
            newLowerBound = checkForLowerBound(LeafNode.getKeyAtIndex(keyIdx + 1));
            parent.updateKeysAfterDeletion(inBetweenKeyIdx - 1, parent.getSingleChild(inBetweenKeyIdx).getFirstKey(), false);
        }

    }

    private void mergeNonLeaf(NonLeafNode nodeToMergeTo, NonLeafNode current, NonLeafNode parent,
                                   int rightPointerIdx,
                                   int inBetweenKeyIdx, boolean mergeWithLeft) {
        float keyToRemove;

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

        ExperimentStats.deleteOneNode();

        int ptrIdx = nodeToMergeTo.binarySearchUpperBound(keyToRemove, true);
        int keyIdx = ptrIdx - 1;

        NonLeafNode LeafNode = nodeToMergeTo;
        int lowerbound = checkForLowerBound(keyToRemove);
        int newLowerBound = 0;

        if (LeafNode.getNumberOfKeys() >= (keyIdx + 1)) {
            newLowerBound = lowerbound;
        } else {
            newLowerBound = checkForLowerBound(LeafNode.getKeyAtIndex(keyIdx + 1)); // Get new lowerbound
            parent.updateKeysAfterDeletion(inBetweenKeyIdx - 1, keyToRemove, false);

        }
    }

    private void mergeLeafNodes(LeafNode nodeToMergeTo, LeafNode current, NonLeafNode parent,
                                int rightPointerIdx, int inBetweenKeyIdx, boolean mergetoright) {
        float removedKey = 0;
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
        ExperimentStats.deleteOneNode();

        int lowerbound = checkForLowerBound(removedKey);
        float newLowerBound = 0;
        if (current.getParent().getNumberOfKeys() >= NoOfChildren) {
            newLowerBound = lowerbound;
        } else {
            newLowerBound = current.getParent().getSingleChild(0).getFirstKey();

            if (inBetweenKeyIdx == 0) {
            } else {
                current.getParent().updateKeysAfterDeletion(inBetweenKeyIdx - 1, newLowerBound, true);
            }
        }

    }

}
