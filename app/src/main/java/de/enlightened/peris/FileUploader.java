/*
 * Copyright (C) 2014 - 2015 Initial Author
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

package de.enlightened.peris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

public class FileUploader {

  private static final String TAG = FileUploader.class.getName();
  private static final int BITMAP_JPEG_QUALITY = 100;

  public String uploadBitmap(final Context context, final String url, final Bitmap bitmap, final PerisApp application) {
    String result = "fail";
    try {
      final HttpClient httpClient = new DefaultHttpClient();
      final HttpContext localContext = new BasicHttpContext();
      final HttpPost httpPost = new HttpPost(url);
      final MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();

      bitmap.compress(CompressFormat.JPEG, BITMAP_JPEG_QUALITY, bos);
      final byte[] data = bos.toByteArray();
      entity.addPart("uploadedfile", new ByteArrayBody(data, "temp.jpg"));
      entity.addPart("server_address", new StringBody(application.getSession().getServer().getUrlString()));
      entity.addPart("id", new StringBody(application.getSession().getServer().serverUserName));
      httpPost.setEntity(entity);

      final HttpResponse response = httpClient.execute(httpPost, localContext);
      final BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
      result = reader.readLine();
    } catch (Exception ex) {
      Log.d(TAG, ex.getMessage());
    }
    return result;
  }

}
