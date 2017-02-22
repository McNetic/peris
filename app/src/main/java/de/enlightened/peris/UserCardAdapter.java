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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class UserCardAdapter extends BaseAdapter {
  private Context context;
  private ArrayList<IgnoreItem> data;

  UserCardAdapter(final ArrayList<IgnoreItem> data, final Context context) {
    this.data = data;
    this.context = context;
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
      v = vi.inflate(R.layout.ignore_item, null);
    }

    final TextView iiUsername = (TextView) v.findViewById(R.id.ignore_item_username);
    final TextView iiTimestamp = (TextView) v.findViewById(R.id.ignore_item_timestamp);
    final ImageView iiAvatar = (ImageView) v.findViewById(R.id.ignore_item_avatar);
    final IgnoreItem ii = this.data.get(arg0);
    iiUsername.setText(ii.ignoreItemUsername);
    String via = ii.ignoreItemDate;
    if (via.contentEquals("Index page")) {
      via = "Lurking...";
    }

    iiTimestamp.setText(via);
    iiUsername.setTextColor(Color.parseColor("#333333"));
    iiTimestamp.setTextColor(Color.parseColor("#333333"));

    if (ii.ignoreItemAvatar != null && ii.ignoreItemAvatar.contains("http://")) {
      final String imageUrl = ii.ignoreItemAvatar;
      ImageLoader.getInstance().displayImage(imageUrl, iiAvatar);
    } else {
      iiAvatar.setImageResource(R.drawable.no_avatar);
    }
    return v;
  }
}
