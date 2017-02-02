package de.enlightened.peris.support;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Created by Nicolai Ehemann on 02.02.2017.
 */

public final class Crypt {

  private static String byteToHex(final byte[] hash) {
    final Formatter formatter = new Formatter();
    for (byte b : hash) {
      formatter.format("%02x", b);
    }
    final String rv = formatter.toString();
    formatter.close();
    return rv;
  }

  private static String calculateDigest(final String algorithm, final String string) {
    String cryptedString = "";
    try {
      final MessageDigest digest = MessageDigest.getInstance(algorithm);
      digest.reset();
      digest.update(string.getBytes("UTF-8"));
      cryptedString = byteToHex(digest.digest());
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return cryptedString;
  }

  public static String md5(final String string) {
    return calculateDigest("MD5", string);
  }

  public static String sha1(final String string) {
    return calculateDigest("SHA-1", string);
  }
}
