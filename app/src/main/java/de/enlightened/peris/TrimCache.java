package de.enlightened.peris;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class TrimCache {

  private Context context;

  public TrimCache(Context context) {
    this.context = context;
  }

  public static boolean deleteDir(File dir) {
    if (dir != null && dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }
    return dir.delete();
  }

  public void trim() {
    File cache = context.getCacheDir();
    File appDir = new File(cache.getParent());
    if (appDir.exists()) {
      String[] children = appDir.list();
      for (String s : children) {
        if (!s.equals("lib")) {
          deleteDir(new File(appDir, s));
          Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
        }
      }
    }
  }
}
