package uz.app.Anno.util;

import java.util.Map;

public class Pair<K, V> implements Map.Entry<K, V> {
    K key;
    V value;

    public Pair(K key, V value)
    {
        this.key = key;
        this.value = value;
    }
    public K getKey() {
        return key;
    }
    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        this.value = value;
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Pair))
            return false;
        Pair<K,V> other = (Pair<K,V>)obj;

        return other.key.equals(this.key) && other.value.equals(this.value);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        int mask = 65535; // 0B1111_1111_1111_1111
        int keyHash = key.hashCode();
        int valHash = value.hashCode();
        int postfix_keyHash = keyHash & mask;
        int postfix_valHash = valHash & mask;
        hash = hash | (postfix_keyHash << 16) | postfix_valHash;
        return hash;
    }
}
