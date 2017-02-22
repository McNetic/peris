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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class PostAdapter extends BaseAdapter {
  private static final int DEFAULT_FONT_SIZE = 16;

  private Context context;
  private ArrayList<Post> data;
  private boolean useShading = false;
  private boolean useOpenSans = false;
  private int fontSize = DEFAULT_FONT_SIZE;
  private boolean currentAvatarSetting = false;
  private int page;
  private PerisApp application;

  PostAdapter(final ArrayList<Post> data, final Context context, final PerisApp app, final int pageNumber) {
    this.data = data;
    this.context = context;
    this.application = app;
    this.page = pageNumber;

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
    View v = arg1;
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(R.layout.post, null);
    }
    final Post po = this.data.get(arg0);
    ElementRenderer.renderPost(v, this.application, this.page, this.context, arg0, this.useOpenSans, this.useShading, po, this.fontSize, this.currentAvatarSetting);
    return v;
  }
}
