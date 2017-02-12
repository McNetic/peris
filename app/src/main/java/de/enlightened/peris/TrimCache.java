package de.enlightened.peris;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class TrimCache {

  private static final String TAG = TrimCache.class.getName();

  private Context context;

  public TrimCache(final Context context) {
    this.context = context;
  }

  public static boolean deleteDir(final File dir) {
    if (dir != null && dir.isDirectory()) {
      final String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        final boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }
    return dir.delete();
  }

  public void trim() {
    final File cache = this.context.getCacheDir();
    final File appDir = new File(cache.getParent());
    if (appDir.exists()) {
      final String[] children = appDir.list();
      for (String s : children) {
        if (!"lib".equals(s)) {
          deleteDir(new File(appDir, s));
          Log.i(TAG, "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
        }
      }
    }
  }
}
