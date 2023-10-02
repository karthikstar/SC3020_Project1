package database;

public class Block {
    private int currentNoOfRecords; // current no. of records being stored in the block

    private static int noOfRecordsPerBlock; // no. of records permitted per block

    private Record[] recordsArray; // list of all the records in the block



    public Block(int blockSize) {
        this.currentNoOfRecords = 0;
        this.noOfRecordsPerBlock = blockSize / Record.getSizeOfRecord();
        this.recordsArray = new Record[this.noOfRecordsPerBlock];
    }

    public Record retrieveRecordFromBlock(int offset) {
        return recordsArray[offset];
    }

    public int getCurrentNoOfRecords() {
        return this.currentNoOfRecords;
    }

    public static int getNoOfRecordsPerBlock() {
        return noOfRecordsPerBlock;
    }

    public boolean isBlockAvail() {
        if(getCurrentNoOfRecords() < getNoOfRecordsPerBlock()) {
            return true;
        } else {
            return false;
        }
    }

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

    public Record getRecord(int offset) {
        return recordsArray[offset];
    }

    public void removeRecord(int offset){
        recordsArray[offset] = null;
        currentNoOfRecords--;
    }
}
