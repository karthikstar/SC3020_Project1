package database;

public class Block {
    // current no. of records being stored in the block
    private int currentNoOfRecords;
    // no. of records permitted per block
    private static int noOfRecordsPerBlock;
    // list of all the records in the block
    private Record[] recordsArray;



    public Block(int blockSize) {
        this.currentNoOfRecords = 0;
        this.noOfRecordsPerBlock = blockSize / Record.getSizeOfRecord();
        this.recordsArray = new Record[this.noOfRecordsPerBlock];
    }

    /**
     * Retrieve a Record Object from the block based on the offset passed in
     * @param offset the offset for the record in the block
     * @return a Record object representing the target record
     */
    public Record retrieveRecordFromBlock(int offset) {
        return recordsArray[offset];
    }

    /**
     * Returns the current number of records in the block
     * @return an integer representing the current number of records
     */
    public int getCurrentNoOfRecords() {
        return this.currentNoOfRecords;
    }

    /**
     * Returns the number of records that can be stored within a block
     * @return an integer representing the number of records that can be stored within a block
     */
    public static int getNoOfRecordsPerBlock() {
        return noOfRecordsPerBlock;
    }

    /**
     * Checks if block has capacity to take in another record
     * @return a boolean value representing whether the block can take in another record
     */
    public boolean isBlockAvail() {
        if(getCurrentNoOfRecords() < getNoOfRecordsPerBlock()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Takes in a Record object, stores it in the block and returns an offset that corresponds to the record's position in the block
     * @param record the Record object to be inserted
     * @return an integer representing the offset of the record within the block
     */
    public int addRecordToBlock(Record record) {
        for(int j = 0; j < recordsArray.length; j++) {
            if(recordsArray[j] == null) {
                recordsArray[j] = record;
                this.currentNoOfRecords++;
                return j;
            }
        }

        return -1; // if unable to find empty array slot to add record
    }

    /**
     * Get a record from the block based on the given offset
     * @param offset an integer representing the position of the record in the block
     * @return a Record object
     */
    public Record getRecord(int offset) {
        return recordsArray[offset];
    }

    /**
     * Remove a record from the block based on the given offset
     * @param offset an integer representing the position of the record in the block
     */
    public void removeRecord(int offset){
        recordsArray[offset] = null;
        currentNoOfRecords--;
    }
}
