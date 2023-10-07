package database;

import java.util.HashSet;
import java.util.Set;

public class Disk {
    private Cache cache;

    private Block[] blocks;

    private Set<Integer> availBlocks;
    private Set<Integer> fullBlocks;


    private static int noOfBlockAccess = 0;
    private int noOfBlockAccessReduced = 0;


    private int blockSize;

    private int diskCapacity;

    private int totalNoOfRecords = 0; // no. of records stored in disk

    public Disk(int blockSize, int diskCapacity) {
        this.blockSize = blockSize;
        this.diskCapacity = diskCapacity;
        this.blocks = new Block[diskCapacity / blockSize];
        this.availBlocks = new HashSet<>();
        this.fullBlocks = new HashSet<>();

        // init blocks
        for (int j = 0; j < blocks.length; j++) {
            blocks[j] = new Block(blockSize);
            availBlocks.add(j);
        }

        // TODO: check this code agn
        int cacheSize = (int) (256.0 / 500000.0 * diskCapacity / blockSize);
        this.cache = new Cache(cacheSize);

    }

//    public Address writeRecToDisk(Record record) {
//        totalNoOfRecords++;
//        int blockAddress = getFirstAvailableBlockId();
//
//
//    }
//
//    private int getFirstAvailableBlockId() {
//        if(availBlocks.isEmpty()) {
//
//        }
//    }

    public int getTotalNoOfRecords() {
        return totalNoOfRecords;
    }

    public int getNoOfBlockAccess() {
        return noOfBlockAccess;
    }

    public int getNoOfBlockAccessReduced() {
        return noOfBlockAccessReduced;
    }



}
