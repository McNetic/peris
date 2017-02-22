/*
 * Copyright (C) 2017 Nicolai Ehemann
 *
 * This file is part of Peris.
 *
 * Peris is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Peris is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Peris.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.enlightened.peris.support;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
