package database;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Disk {
    private Cache cache;

    private Block[] blocks;

    private Set<Integer> availBlocks;
    private Set<Integer> usedBlocks;


    private static int noOfBlockAccess = 0;
    private int noOfBlockAccessReduced = 0;


    private int blockSize;

    private int diskCapacity;

    private int totalNoOfRecords = 0; // no. of records stored in disk

    public Disk(int blockSize, int diskCapacity) {
        this.blockSize = blockSize;
        this.diskCapacity = diskCapacity;
        this.availBlocks = new HashSet<>();
        this.usedBlocks = new HashSet<>();

        // init blocks
        this.blocks = new Block[diskCapacity / blockSize];
        for (int j = 0; j < blocks.length; j++) {
            blocks[j] = new Block(blockSize);
            availBlocks.add(j);
        }

        // TODO: check this code agn
        int cacheSize = (int) (256.0 / 500000.0 * diskCapacity / blockSize);
        this.cache = new Cache(cacheSize);

    }

    public Address writeRecToDisk(Record record) {
        totalNoOfRecords++;
        int blockId = getFirstAvailableBlockId();

        Address recordAddress = this.insertRecIntoBlock(blockId, record);

        return recordAddress;
    }

    private int getFirstAvailableBlockId() {
        if(availBlocks.isEmpty()) {
            return -1;
        } else {
            int blockId = availBlocks.iterator().next();
            return blockId;
        }
    }

    private Address insertRecIntoBlock(int blockId, Record record) {
        int offset = blocks[blockId].addRecordToBlock(record);

        usedBlocks.add(blockId);

        // Check if block still have available slots left
        if(!blocks[blockId].isBlockAvail()) {
            availBlocks.remove(blockId);
        }

        return new Address(blockId, offset);
    }

    public int getTotalNoOfRecords() {
        return totalNoOfRecords;
    }

    public int getNoOfBlockAccess() {
        return noOfBlockAccess;
    }

    public int getNoOfBlockAccessReduced() {
        return noOfBlockAccessReduced;
    }

    public int getNoOfUsedBlocks() {
        return usedBlocks.size();
    }

    /**
     * Checks cache first for a particular block
     * If block is found in cache, return it
     * Else access the Disk and load the block into the cache
     * @param blockNo
     * @return Target Block
     */
    private Block getBlock(int blockNo) {
        Block targetBlock = cache.retrieveBlock(blockNo);

        // if block in cache
        if(targetBlock != null) {
            noOfBlockAccessReduced++;
        }

        // if block not in cache
        if (targetBlock == null && blockNo >= 0) {
            targetBlock = blocks[blockNo];

            noOfBlockAccess++;

            // put into cache
            cache.putBlockInCache(targetBlock, blockNo);
        }
        return targetBlock;
    }

    public Record retrieveRecord(Address address) {
        Block block = getBlock(address.getBlockID());
        Record record = block.getRecord(address.getOffset());

        return record;
    }

    /**
     * Remove record at an address in the Disk
     * @param addressArrayList
     */
    public void removeRecord(ArrayList<Address> addressArrayList) {
        for(Address address : addressArrayList) {
            int blockId = address.getBlockID();
            int offset = address.getOffset();

            Block block = getBlock(blockId);

            block.removeRecord(offset);

            //remove from used blocks
            if (usedBlocks.contains(blockId)) {

                usedBlocks.remove(blockId);
            }
            availBlocks.add(blockId);
        }
    }

    /**
     * Perform a brute-force (BF) linear scan method, by scanning data blocks 1 by one within the given lower limit and upper limit for FG_PCT_HOME
     * For Expt.s 3 and 4
     * @param FG_PCT_HOME_LOWER_LIMIT lower limit of FG_PCT_home
     * @param FG_PCT_HOME_UPPER_LIMIT upper limit of FG_PCT_home
     * @return the number of data blocks accessed during the brute-force search
     */
    public int BFSearch(float FG_PCT_HOME_LOWER_LIMIT, float FG_PCT_HOME_UPPER_LIMIT) {
        Record record;
        float FG_PCT_HOME_VALUE;

        ArrayList<Record> results = new ArrayList<>();

        int noOfBlocksAccessed = 0;
        for (int blockPtr: usedBlocks) {
            noOfBlocksAccessed++;
            Block block = blocks[blockPtr];

            int currentNoOfRecordsInBlock = block.getCurrentNoOfRecords();
            for (int j = 0 ; j < currentNoOfRecordsInBlock; j++) {
                record = block.retrieveRecordFromBlock(j);
                FG_PCT_HOME_VALUE = record.getFG_PCT_home();

                // if within range, add to results arraylist
                if(FG_PCT_HOME_VALUE >= FG_PCT_HOME_LOWER_LIMIT && FG_PCT_HOME_VALUE <= FG_PCT_HOME_UPPER_LIMIT) {
                    results.add(record);
                }
            }
        }

        if(results.size() == 0) {
            System.out.printf("BFSearch - Records are not found for the given range: %f to %f\n",
                    FG_PCT_HOME_LOWER_LIMIT, FG_PCT_HOME_UPPER_LIMIT);
        }

        for(Record result: results){
            System.out.printf("BFSearch - Found Record: %s\n", result);
        }

        return noOfBlocksAccessed;
    }

    /**
     * Perform a brute-force (BF) linear scan method, by scanning data blocks 1 that satisfy the given lower limit
     * For Expt 5
     * @param FG_PCT_HOME_LOWER_LIMIT lower limit of FG_PCT_home
     * @return the number of data blocks accessed during the brute-force search
     */
    public int BFSearch(float FG_PCT_HOME_LOWER_LIMIT) {
        Record record;
        float FG_PCT_HOME_VALUE;

        ArrayList<Record> results = new ArrayList<>();

        int noOfBlocksAccessed = 0;
        for (int blockPtr: usedBlocks) {
            noOfBlocksAccessed++;
            Block block = blocks[blockPtr];

            int currentNoOfRecordsInBlock = block.getCurrentNoOfRecords();
            for (int j = 0 ; j < currentNoOfRecordsInBlock; j++) {
                record = block.retrieveRecordFromBlock(j);
                FG_PCT_HOME_VALUE = record.getFG_PCT_home();

                // if within range, add to results arraylist
                if(FG_PCT_HOME_VALUE <= FG_PCT_HOME_LOWER_LIMIT) {
                    results.add(record);
                }
            }
        }

        if(results.size() == 0) {
            System.out.printf("BFSearch - Records are not found for the given range <= %f\n",
                    FG_PCT_HOME_LOWER_LIMIT);
        }

        for(Record result: results){
            System.out.printf("BFSearch - Found Record: %s\n", result);
        }

        return noOfBlocksAccessed;
    }

    public void runExptOne() {
        System.out.println("---------EXPERIMENT ONE---------");
        System.out.printf("No. Of Records: %d\n" , this.getTotalNoOfRecords());
        System.out.printf("Size of a record: %d B\n", Record.getSizeOfRecord());
        System.out.printf("No. of records stored in a block: %d\n", Block.getNoOfRecordsPerBlock());
        System.out.printf("No. of Blocks Used For Storing The Data: %d\n", this.getNoOfUsedBlocks());
        System.out.println("-----END OF EXPERIMENT ONE------");

    }


}
