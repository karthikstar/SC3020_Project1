package database;

public class Address {
    // Offset for record stored in the block
    private int offset;

    // Id Of Block
    private int blockID;

    public Address(int offset, int blockId) {
        this.offset = offset;
        this.blockID = blockId;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getBlockID() {
        return this.blockID;
    }

    @Override
    public String toString() {
        return String.format("Block %d, Offset %d", blockID, offset);
    }

}
