package android.util;

import java.util.HashMap;

public class SparseArray<E> {

    private HashMap<Integer, E> mHashMap;

    public SparseArray() {
        mHashMap = new HashMap<>();
    }

    public void put(int key, E value) {
        mHashMap.put(key, value);
    }

    public E get(int key) {
        return mHashMap.get(key);
    }

    public void clear() {
        mHashMap.clear();
    }
}