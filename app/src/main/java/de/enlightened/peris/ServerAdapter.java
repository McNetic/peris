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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

@SuppressLint("ViewHolder")
public class ServerAdapter extends BaseAdapter {
  private Context context;
  private List<Server> data;

  ServerAdapter(final List<Server> data, final Context context) {
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

  public View getView(final int arg0, final View arg1, final ViewGroup arg2) {
    View v = arg1;
    final LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    v = vi.inflate(R.layout.server, null);

    final TextView serverAddress = (TextView) v.findViewById(R.id.server_address);
    final TextView serverUsername = (TextView) v.findViewById(R.id.server_username);
    final ImageView serverUserAvater = (ImageView) v.findViewById(R.id.server_user_avatar);
    final RelativeLayout serverTabColor = (RelativeLayout) v.findViewById(R.id.server_tab_color);

    final Server server = this.data.get(arg0);
    if (server.serverName.contentEquals("0")) {
      serverAddress.setText(server.serverAddress);
    } else {
      serverAddress.setText(server.serverName);
    }


    if (server.serverUserName.contentEquals("0")) {
      serverUsername.setText("Guest");
    } else {
      serverUsername.setText(server.serverUserName);
    }

    if (server.serverAvatar.contains("http")) {
      ImageLoader.getInstance().displayImage(server.serverAvatar, serverUserAvater);
    } else {
      if (server.serverTagline.contentEquals("[*WEBVIEW*]")) {
        serverUserAvater.setImageResource(R.drawable.webview_forum);
      } else {
        serverUserAvater.setImageResource(R.drawable.no_avatar);
      }
    }

    if (server.serverColor.contains("#")) {
      serverTabColor.setBackgroundColor(Color.parseColor(server.serverColor));
    } else {
      serverTabColor.setBackgroundColor(Color.parseColor(this.context.getString(R.string.default_color)));
    }

    final ImageView serverIcon = (ImageView) v.findViewById(R.id.server_server_icon);
    if (server.serverIcon != null) {
      ImageLoader.getInstance().displayImage(server.serverIcon, serverIcon);
    }
    return v;
  }
}
