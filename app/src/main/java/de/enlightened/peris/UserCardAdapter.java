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

import java.util.List;

import de.enlightened.peris.site.OnlineUser;

@SuppressLint("NewApi")
public class UserCardAdapter extends BaseAdapter {
  private final Session session;
  private final Context context;
  private final List<OnlineUser> data;

  UserCardAdapter(final Session session, final List<OnlineUser> data, final Context context) {
    this.session = session;
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
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    View view = convertView;
    if (view == null) {
      final LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = vi.inflate(R.layout.online_user, null);
    }

    final TextView viewUserName = (TextView) view.findViewById(R.id.online_user_username);
    final TextView viewTimestamp = (TextView) view.findViewById(R.id.online_user_timestamp);
    final ImageView viewAvatar = (ImageView) view.findViewById(R.id.online_user_avatar);
    final OnlineUser onlineUser = this.data.get(position);
    viewUserName.setText(onlineUser.getUserName());
    String displayText = onlineUser.getDisplayText();
    if ("Index page".equals(displayText)) {
      displayText = "Lurking...";
    }

    viewTimestamp.setText(displayText);
    viewUserName.setTextColor(Color.parseColor("#333333"));
    viewTimestamp.setTextColor(Color.parseColor("#333333"));

    final String avatarUrl;
    if (null != onlineUser.getAvatarUrl()) {
      avatarUrl = onlineUser.getAvatarUrl();
    } else {
      avatarUrl = UserCardAdapter.this.session.getServer().getAvatarURL(onlineUser.getId()).toExternalForm();
    }
    //TODO: else viewAvatar.setImageResource(R.drawable.no_avatar);
    ImageLoader.getInstance().displayImage(avatarUrl, viewAvatar);
    return view;
  }
}
