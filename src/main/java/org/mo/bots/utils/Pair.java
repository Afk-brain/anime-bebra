package org.mo.bots.utils;

public class Pair<K, V> {

    public K key;
    public V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object object) {
       Pair pair2 = (Pair) object;
       return this.key.equals(pair2.key);
    }

}
