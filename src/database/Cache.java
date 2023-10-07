package database;

import java.util.LinkedHashMap;

public class Cache {
    // implement the LRU (least recently used) cache, which stores a certain number of block objects

    private int CAPACITY;

    private LinkedHashMap<Integer, Block> store; // to store the blocks in this cache

    public Cache(int capacity) {
        this.store = new LinkedHashMap<>();
        this.CAPACITY = capacity;
    }

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
