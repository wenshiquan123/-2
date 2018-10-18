package com.hlzx.wenutil.http.tools;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by alan on 2016/3/12.
 */
public interface MultiValueMap<K,V> {

    /**
     * Add a value for a key.
     *
     * @param key   key.
     * @param value value.
     */
    void add(K key, V value);

    /**
     * Add more value to a key.
     *
     * @param key    key.
     * @param values values.
     */
    void add(K key, List<V> values);

    /**
     * Set the value for a key, if the key has the value, delete all of the old value, then the new value added.
     *
     * @param key   key.
     * @param value values.
     */
    void set(K key, V value);

    /**
     * @param key    key.
     * @param values values.
     * @see #set(Object, Object)
     */
    void set(K key, List<V> values);

    /**
     * The removal of all key/value pair, add new keys to enter.
     *
     * @param values values.
     */
    void set(Map<K, List<V>> values);

    /**
     * Delete a key-value.
     *
     * @param key key.
     */
    void remove(K key);

    /**
     * Remove all key-value.
     */
    void clear();

    /**
     * Get the key set.
     *
     * @return Set.
     */
    Set<K> keySet();

    /**
     * To get all key of all values.
     *
     * @return List.
     */
    List<V> values();

    /**
     * To get the key of the at index value.
     *
     * @param key   key.
     * @param index index value.
     * @return The value.
     */
    V getValue(K key, int index);

    /**
     * To get key of all values.
     *
     * @param key key.
     * @return values.
     */
    List<V> getValues(K key);

    /**
     * The size of the map.
     *
     * @return size.
     */
    int size();

    /**
     * If the map has no value.
     *
     * @return True: empty, false: not empty.
     */
    boolean isEmpty();

    /**
     * Whether the map with a key.
     *
     * @param key key.
     * @return True: contain, false: none.
     */
    boolean containsKey(K key);
}
