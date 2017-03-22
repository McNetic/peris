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

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.timroes.axmlrpc.XMLRPCClient;

public class XMLRPCCall {
  private static final String TAG = XMLRPCCall.class.getName();;
  private final XMLRPCClient xmlRPCClient;
  private final String method;
  private final ArrayList<Object> params = new ArrayList<>();

  public XMLRPCCall(final XMLRPCClient xmlRPCClient, final String method) {
    this.xmlRPCClient = xmlRPCClient;
    this.method = method;
  }

  public XMLRPCCall param(final Object param) {
    this.params.add(param);
    return this;
  }

  public XMLRPCCall optionalParam(final Object param) {
    if (param != null) {
      this.params.add(param);
    }
    return this;
  }

  private Object rpcCall() throws XMLRPCCallException {
    Log.d(TAG, String.format("XMLRPCCall: %s(%s)", this.method, this.params.toArray().toString()));
    try {
      return this.xmlRPCClient.call(this.method, this.params.toArray());
    } catch (Exception ex) {
      Log.e(TAG, String.format("Tapatalk call error (%s) : %s", ex.getClass().getName(), this.method));
      if (ex.getMessage() != null) {
        Log.e(TAG, ex.getMessage());
      } else {
        Log.e(TAG, "(no message available)");
      }
    }
    throw new XMLRPCCallException("XMLRCPCall failed");
  }

  public List<RPCMap> callAsList() {
    try {
      final Object[] mapArray;
      mapArray = (Object[]) this.rpcCall();
      final List<RPCMap> mapList = new ArrayList<>();
      for (Object map : mapArray) {
        mapList.add(RPCMap.of(map));
      }
      return mapList;
    } catch (XMLRPCCallException e) {
      return null;
    }
  }

  public RPCMap call() {
    try {
      return RPCMap.of(this.rpcCall());
    } catch (XMLRPCCallException e) {
      return null;
    }
  }

  private class XMLRPCCallException extends Throwable {
    public XMLRPCCallException(final String message) {
      super(message);
    }
  }
}
