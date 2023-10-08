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

    /**
     * Handles instantiation of the Disk, Block and Cache
     * @param blockSize an integer representing the size of the block
     * @param diskCapacity an integer representing the capacity of disk
     */
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

    /**
     * Handles the writing of record to the disk
     * @param record the record to be written to the disk
     * @return the Address of the given record in this Disk
     */
    public Address writeRecToDisk(Record record) {
        totalNoOfRecords++;
        int blockId = getFirstAvailBlockId();

        Address recordAddress = this.insertRecIntoBlock(blockId, record);

        return recordAddress;
    }

    /**
     * Find the first available block that can accept a new record that is being inserted in
     * @return an integer representing blockId of the block
     */
    private int getFirstAvailBlockId() {
        if(availBlocks.isEmpty()) {
            return -1;
        } else {
            int blockId = availBlocks.iterator().next();
            return blockId;
        }
    }

    /**
     * Insert a record into the given block
     * @param blockId the id of the block that the record is being inserted in
     * @param record the Record object to be inserted in
     * @return the address of the block that was inserted into the block
     */
    private Address insertRecIntoBlock(int blockId, Record record) {
        int offset = blocks[blockId].addRecordToBlock(record);

        usedBlocks.add(blockId);

        // Check if block still have available slots left
        if(!blocks[blockId].isBlockAvail()) {
            availBlocks.remove(blockId); // if no slots left, take out from avail blocks
        }

        return new Address(offset, blockId);
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

    /**
     * Retrieves the record based on the given address
     * @param address the address of record to be retrieved
     * @return a Record object corresponding to the record to be retrieved
     */
    public Record retrieveRecord(Address address) {
        Block block = getBlock(address.getBlockID());
        Record record = block.getRecord(address.getOffset());

        return record;
    }

    /**
     * Remove record at an address on the Disk
     * @param addressArrayList a list of addresses whose corresponding records are to be removed
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
     * For Expts 3 and 4
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
            System.out.printf("BFSearch - Found %s\n", result);
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
            System.out.printf("BFSearch - Found %s\n", result);
        }

        return noOfBlocksAccessed;
    }

    /**
     * Runs experiment one and print required output
     */
    public void runExptOne() {
        System.out.println("---------EXPERIMENT ONE---------");
        System.out.printf("No. Of Records: %d\n" , this.getTotalNoOfRecords());
        System.out.printf("Size of a record: %d B\n", Record.getSizeOfRecord());
        System.out.printf("No. of records stored in a block: %d\n", Block.getNoOfRecordsPerBlock());
        System.out.printf("No. of Blocks Used For Storing The Data: %d\n", this.getNoOfUsedBlocks());
        System.out.println("-----END OF EXPERIMENT ONE------");

    }


}
