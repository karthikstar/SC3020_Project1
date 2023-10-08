package database;

import java.util.LinkedHashMap;

/**
 * This Cache class implements the LRU (least recently used) cache, which stores a certain number of Block objects.
 * This LRU Cache is implemented using a LinkedHashMap.
 * When cache full, least recently used block is removed from cache so that another block can be put into the cache
 */
public class Cache {
    // implement the LRU (least recently used) cache, which stores a certain number of block objects

    private int CAPACITY;

    private LinkedHashMap<Integer, Block> store; // to store the blocks in this cache

    /**
     * Creates the cache with the given capacity
     * @param capacity the max no. of blocks that the cache can hold
     */
    public Cache(int capacity) {
        this.store = new LinkedHashMap<>();
        this.CAPACITY = capacity;
    }

    /**
     * Handles placing block into cache, and associates the block with the specified key in the cache.
     * If key already present in cache, we will replace that existing block with the new block
     * If cache is full, the least recently used block will be evicted in order to insert the new block
     * @param block the block being put into the cache
     * @param key the key associated with the block
     */
    public void putBlockInCache(Block block, int key) {
        if (store.containsKey(key)) {
            store.remove(key);
        } else if (store.size() + 1 > CAPACITY) {
            store.remove(store.keySet().iterator().next());
        }

        store.put(key, block);
    }

    /**
     * Retrieves the block from the LRU Cache, based on the given key.
     * If block is found it will be moved to the end of the LinkedHashNap to indicate that it has been recently used.
     * @param key to obtain the block from the cache
     * @return the Block which has the key, or null if not present in the LRU cache
     */
    public Block retrieveBlock(int key) {
        if (store.containsKey(key)) {
            Block target = store.remove(key);
            store.put(key, target);
            return target;
        } else {
            return null;
        }
    }

}
