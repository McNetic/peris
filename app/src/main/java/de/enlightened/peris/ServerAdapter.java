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

import java.util.ArrayList;

@SuppressLint("ViewHolder")
public class ServerAdapter extends BaseAdapter {
  private Context context;
  private ArrayList<Server> data;

  ServerAdapter(final ArrayList<Server> data, final Context context) {
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
      serverAddress.setText(server.serverAddress.replace("http://", ""));
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
    if (server.serverIcon.contains("http")) {
      serverIcon.setVisibility(View.VISIBLE);
      ImageLoader.getInstance().displayImage(server.serverIcon, serverIcon);
    } else {
      serverIcon.setVisibility(View.GONE);
    }
    return v;
  }
}
