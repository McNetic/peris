package de.enlightened.peris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;

public class AvatarUploader {

  private static final String TAG = AvatarUploader.class.getName();;
  private static final int JPEG_COMPRESSION_QUALITY = 75;

  public final String uploadBitmap(final Context context, final URL url, final Bitmap bitmap, final PerisApp application) {

    String result = "fail";

    final BasicCookieStore cStore = new BasicCookieStore();

    final CookieManager cookiemanager = new CookieManager();
    cookiemanager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    CookieHandler.setDefault(cookiemanager);

    String cookieString = "";

    for (String s : application.getSession().getCookies().keySet()) {
      try {
        final BasicClientCookie aCookie = new BasicClientCookie(s, application.getSession().getCookies().get(s));
        cStore.addCookie(aCookie);

        cookieString = cookieString + s + "=" + application.getSession().getCookies().get(s) + ";";
      } catch (Exception ex) {
        // nobody cares
        cookieString = "";
      }
    }

    try {

      final HttpClient httpClient = new DefaultHttpClient();
      final HttpContext localContext = new BasicHttpContext();

      localContext.setAttribute(ClientContext.COOKIE_STORE, cStore);

      final HttpPost httpPost = new HttpPost(url.toURI());

      //httpPost.setHeader("User-Agent", "Peris");
      httpPost.setHeader("Cookie", cookieString);

      Log.d(TAG, "Cookie String: " + cookieString);

      final MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();

      bitmap.compress(CompressFormat.JPEG, JPEG_COMPRESSION_QUALITY, bos);

      Log.d(TAG, "Outgoing Avatar Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());


      final byte[] data = bos.toByteArray();

      //entity.addPart("myParam", new StringBody("my value"));

      final File f = new File(context.getCacheDir(), "temp.jpg");
      f.createNewFile();
      final FileOutputStream fos = new FileOutputStream(f);
      fos.write(data);
      fos.close();

      final FileBody bin = new FileBody(f, "image/jpeg");

      final String methodName = "upload_avatar";
      String uploadFileFieldName;

      //String uploadFileFieldName = "uploadfile";

      uploadFileFieldName = application.getSession().getApi().getConfig().getAvatarSubmissionName();

      Log.d(TAG, "Avatar Upload Field Name: " + uploadFileFieldName);

      entity.addPart(uploadFileFieldName, bin);
      entity.addPart("method_name", new StringBody(methodName));

      httpPost.setEntity(entity);

      final HttpResponse response = httpClient.execute(httpPost, localContext);
      final BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

      String line;
      String xml = "";

      while ((line = reader.readLine()) != null) {
        xml = xml + line;
        Log.d(TAG, line);
      }

      result = reader.readLine();

    } catch (Exception ex) {
      //fuck it
      Log.d(TAG, ex.getMessage());
    }
    return result;
  }

}
