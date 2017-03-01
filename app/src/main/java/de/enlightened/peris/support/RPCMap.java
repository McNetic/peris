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

import java.util.Date;
import java.util.Map;

public class RPCMap extends OptMap<String, Object> {

  public RPCMap(final Map<String, Object> map) {
    super(map);
  }

  public static RPCMap of(final Object obj) {
    return new RPCMap((Map) obj);
  }

  public Integer getInt(final String key) {
    return this.getType(key);
  }

  public Boolean getBool(final String key) {
    return this.getType(key);
  }

  public String getString(final String key) {
    return this.getType(key);
  }

  public Date getDate(final String key) {
    return this.getType(key);
  }

  public String getByteString(final String key) {
    final byte[] bytes = (byte[]) this.get(key);
    if (bytes != null) {
      return new String(bytes);
    } else {
      return null;
    }
  }

  public Boolean getBoolOrDefault(final String key, final Boolean defaultValue) {
    return this.getTypeOrDefault(key, defaultValue);
  }

  public boolean getBoolOrDefault(final String key) {
    return this.getBoolOrDefault(key, false);
  }

  public Integer getIntOrDefault(final String key, final Integer defaultValue) {
    return this.getTypeOrDefault(key, defaultValue);
  }

  public String getStringOrDefault(final String key, final String defaultValue) {
    return this.getTypeOrDefault(key, defaultValue);
  }

  public Date getDateOrDefault(final String key, final Date defaultValue) {
    return this.getTypeOrDefault(key, defaultValue);
  }

  public String getByteStringOrDefault(final String key, final String defaultValue) {
    if (this.containsKey(key)) {
      return this.getByteString(key);
    } else {
      return defaultValue;
    }
  }

  public RPCMap[] getRPCMapArray(final String key) {
    final Object[] srcArray = (Object[]) this.getOrDefault(key, new Object[0]);
    final RPCMap[] dstArray = new RPCMap[srcArray.length];
    for (int i = 0; i < srcArray.length; i++) {
      dstArray[i] = RPCMap.of(srcArray[i]);
    }
    return dstArray;
  }

  public int getCount(final String key) {
    return ((Object[]) this.getOrDefault(key, new Object[0])).length;
  }

  public Boolean getStringBoolOrDefault(final String key) {
    return "1".equals(this.getStringOrDefault(key, "0"));
  }
}
