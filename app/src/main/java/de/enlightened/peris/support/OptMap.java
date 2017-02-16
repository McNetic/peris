package de.enlightened.peris.support;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nicolai Ehemann on 15.02.2017.
 */

public class OptMap<K, V> implements Map<K, V> {
  private final Map<K, V> map;

  public OptMap(final Map<K, V> map) {
    this.map = map;
  }

  public static OptMap of(final Map map) {
    return new OptMap(map);
  }

  @Override
  public int size() {
    return this.map.size();
  }

  @Override
  public boolean isEmpty() {
    return this.map.isEmpty();
  }

  @Override
  public boolean containsKey(final Object key) {
    return this.map.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return this.map.containsValue(value);
  }

  @Override
  public V get(final Object key) {
    return this.map.get(key);
  }

  public <T extends V> T getType(final Object key) {
    return (T) this.get(key);
  }

  public V getOrDefault(final Object key, final V defaultValue) {
    if (this.map.containsKey(key)) {
      return this.map.get(key);
    } else {
      return defaultValue;
    }
  }

  public <T extends V> T getTypeOrDefault(final Object key, final T defaultValue) {
    return (T) this.getOrDefault(key, defaultValue);
  }

  @Override
  public V put(final K key, final V value) {
    return this.map.put(key, value);
  }

  @Override
  public V remove(final Object key) {
    return this.map.remove(key);
  }

  @Override
  public void putAll(@NonNull final Map<? extends K, ? extends V> addMap) {
    this.map.putAll(addMap);
  }

  @Override
  public void clear() {
    this.map.clear();
  }

  @NonNull
  @Override
  public Set<K> keySet() {
    return this.map.keySet();
  }

  @NonNull
  @Override
  public Collection<V> values() {
    return this.map.values();
  }

  @NonNull
  @Override
  public Set<Entry<K, V>> entrySet() {
    return this.map.entrySet();
  }
}
