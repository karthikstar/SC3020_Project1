package database;

public class Disk {
    private int blockSize;

    private int diskCapacity;

    private static int totalNoOfRecords = 0; // no. of records stored in disk

    public Disk(int blockSize, int diskCapacity) {
        this.blockSize = blockSize;
        this.diskCapacity = diskCapacity;
    }

//    public static Address writeRecToDisk(Record record) {
//        totalNoOfRecords++;

        // TODO: finish this implementation to get block id of first avail block, and inserting record into block and getting a address.
//    }
}
