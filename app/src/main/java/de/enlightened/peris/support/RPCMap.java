package de.enlightened.peris.support;

import java.util.Date;
import java.util.Map;

/**
 * Created by Nicolai Ehemann on 15.02.2017.
 */

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
    return new String((byte[]) this.get(key));
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

  public RPCMap[] getRPCMap(final String key) {
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
}
