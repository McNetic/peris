package de.enlightened.peris.support;

import android.util.Log;

import java.util.ArrayList;

import de.timroes.axmlrpc.XMLRPCClient;

/**
 * Created by Nicolai Ehemann on 18.02.2017.
 */

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

  public RPCMap call() {
    Log.d(TAG, "Performing Server Call: Method = " + this.method);
    try {
      return RPCMap.of(this.xmlRPCClient.call(this.method, this.params.toArray()));
    } catch (Exception ex) {
      String.format("Tapatalk call error (%s) : %s", ex.getClass().getName(), this.method);
      if (ex.getMessage() != null) {
        Log.e(TAG, ex.getMessage());
      } else {
        Log.e(TAG, "(no message available)");
      }
    }
    return null;
  }
}
