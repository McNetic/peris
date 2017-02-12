package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

@SuppressLint("NewApi")
class ApeImageCacher {

  private static final String TAG = ApeImageCacher.class.getName();;
  public static final String CACHE_DIRECTORY = ".ff_cache";

  protected ApeImageCacher() {
  }

  public static final void downloadImage(final String imageURL, final ImageView ivHolder, final PerisApp application, final Context context) {

    Log.d(TAG, "Downloading " + imageURL + " with the ApeImageCacher");
    final String cacheName = application.getSession().getServer().serverAddress + "_" + imageURL.substring((imageURL.lastIndexOf("?")) + 1, imageURL.length()) + ".jpg";

    if (ivHolder == null) {
      return;
    }

    final File saveDirectory = new File(Environment.getExternalStorageDirectory(), CACHE_DIRECTORY);

    if (!saveDirectory.exists() && !saveDirectory.mkdirs()) {
      Log.d(TAG, "failed to create directory");
    } else {

      try {
        final BitmapFactory.Options options = new BitmapFactory.Options();

        final File file = new File(saveDirectory.getPath() + File.separator + cacheName);

        final FileInputStream fis = new FileInputStream(file);

        final BufferedInputStream buf = new BufferedInputStream(fis);
        final Bitmap bmImg = BitmapFactory.decodeStream(buf, null, options);

        if (bmImg.getHeight() < 2) {
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            new FetchSubforumIconTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cacheName, ivHolder, imageURL, application);
          } else {
            new FetchSubforumIconTask().execute(cacheName, ivHolder, imageURL, application);
          }

          Log.d(TAG, "Downloading new copy for " + imageURL);
        } else {

          ivHolder.setImageBitmap(bmImg);
          Log.d(TAG, "Using cached image for " + imageURL);
        }
      } catch (Exception e) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
          new FetchSubforumIconTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cacheName, ivHolder, imageURL, application);
        } else {
          new FetchSubforumIconTask().execute(cacheName, ivHolder, imageURL, application);
        }

        Log.d(TAG, "Downloading new copy for " + imageURL);
      }
    }
  }


}
