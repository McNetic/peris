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
      Log.d("Peris", ex.getMessage());
    }
    return result;
  }

}
