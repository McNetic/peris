package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class CategoryAdapter extends BaseAdapter {
  private static final int DEFAULT_FONT_SIZE = 16;

  private Context context;
  private ArrayList<Category> data;
  private boolean useShading = false;
  private boolean useOpenSans = false;
  private boolean currentAvatarSetting = false;
  private int fontSize = DEFAULT_FONT_SIZE;
  private PerisApp application;

  CategoryAdapter(final ArrayList<Category> data, final Context context, final PerisApp app) {
    this.data = data;
    this.context = context;
    this.application = app;

    if (context == null) {
      return;
    }

    final SharedPreferences appPreferences = context.getSharedPreferences("prefs", 0);
    this.useShading = appPreferences.getBoolean("use_shading", false);
    this.useOpenSans = appPreferences.getBoolean("use_opensans", false);
    this.fontSize = appPreferences.getInt("font_size", DEFAULT_FONT_SIZE);
    this.currentAvatarSetting = appPreferences.getBoolean("show_images", true);
  }

  public int getCount() {
    return this.data.size();
  }

  public Object getItem(final int arg0) {
    return this.data.get(arg0);
  }

  public long getItemId(final int arg0) {
    return arg0;
  }

  @SuppressLint("InflateParams")
  public View getView(final int arg0, final View arg1, final ViewGroup arg2) {
    final Category ca = this.data.get(arg0);
    View v = arg1;
    final LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    if (ca.type.contentEquals("S")) {
      v = vi.inflate(R.layout.category, null);
    } else {
      v = vi.inflate(R.layout.thread, null);
    }

    ElementRenderer.renderCategory(v, this.application, this.context, this.useOpenSans, this.useShading, ca, this.currentAvatarSetting);
    return v;
  }

  public int dpToPx(final int dp) {
    final DisplayMetrics displayMetrics = this.context.getResources().getDisplayMetrics();
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }
}
