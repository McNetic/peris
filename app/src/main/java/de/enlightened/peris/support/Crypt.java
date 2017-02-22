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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public final class Crypt {
  private Crypt() {
  }

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
