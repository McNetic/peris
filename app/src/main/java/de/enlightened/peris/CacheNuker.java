package de.enlightened.peris;

import android.content.Context;
import android.content.SharedPreferences;

import com.nostra13.universalimageloader.core.ImageLoader;

public final class CacheNuker {

  private CacheNuker() {
  }

  public static void nukeCache(final Context context) {
    final SharedPreferences appPreferences = context.getSharedPreferences("prefs", 0);
    final SharedPreferences.Editor editor = appPreferences.edit();
    editor.clear();
    editor.commit();
    //TrimCache trimmer = new TrimCache(c);
    //trimmer.trim();
    final ImageLoader imageLoader = ImageLoader.getInstance();
    imageLoader.clearDiskCache();
    imageLoader.clearMemoryCache();
  }
}
