package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import de.enlightened.peris.support.Net;

@SuppressLint("NewApi")
public class ChatAdapter extends BaseAdapter {
  private Context context;
  private ArrayList<Chat> data;
  private boolean useShading = false;
  private boolean useOpenSans = false;
  private boolean currentAvatarSetting = false;
  private PerisApp application;

  ChatAdapter(final ArrayList<Chat> data, final Context context, final PerisApp app) {
    this.data = data;
    this.context = context;
    this.application = app;

    if (context == null) {
      return;
    }

    final SharedPreferences appPreferences = context.getSharedPreferences("prefs", 0);
    this.useShading = appPreferences.getBoolean("use_shading", false);
    this.useOpenSans = appPreferences.getBoolean("use_opensans", false);
    //fontSize = app_preferences.getInt("font_size", 16);
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
    final Chat ch = this.data.get(arg0);
    View v = arg1;
    final LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    if (ch.getDisplayname().trim().contentEquals(this.application.getSession().getServer().serverUserName.trim())) {
      v = vi.inflate(R.layout.chat_post_me, null);
    } else {
      v = vi.inflate(R.layout.chat_post, null);
    }

    final TextView author = (TextView) v.findViewById(R.id.chat_post_author);
    final TextView timestamp = (TextView) v.findViewById(R.id.chat_post_timestamp);
    final TextView post = (TextView) v.findViewById(R.id.chat_post_body);

    final Typeface opensans = Typeface.createFromAsset(this.context.getAssets(), "fonts/opensans.ttf");
    final LinearLayout llColorBackground = (LinearLayout) v.findViewById(R.id.chat_background);

    String textColor = this.context.getString(R.string.default_text_color);
    if (this.application.getSession().getServer().serverTextColor.contains("#")) {
      textColor = this.application.getSession().getServer().serverTextColor;
    }
    textColor = "#000000";

    String boxColor = this.context.getString(R.string.default_element_background);
    //String bgColor = c.getString(R.string.default_background);
    if (this.application.getSession().getServer().serverBoxColor != null) {
      boxColor = this.application.getSession().getServer().serverBoxColor;
    }

    /*
    if(application.getSession().getServer().serverBackground != null) {
      bgColor = application.getSession().getServer().serverBackground;
    }
    */

    if (ch.getDisplaycolor().contains("#")) {
      boxColor = ch.getDisplaycolor().replace("#", "#33");
    }

    final ImageView chatTing = (ImageView) v.findViewById(R.id.chat_ting);
    if (boxColor.contains("#")) {
      llColorBackground.setBackgroundColor(Color.parseColor(boxColor));
      chatTing.setColorFilter(Color.parseColor(boxColor.replace("#33", "#")));
    } else {
      llColorBackground.setBackground(null);
      chatTing.setVisibility(View.GONE);
    }

    if (this.useOpenSans) {
      author.setTypeface(opensans);
      post.setTypeface(opensans);
      timestamp.setTypeface(opensans);
    }

    if (this.useShading) {
      author.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
      post.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
      timestamp.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
    }

    final ImageView avatar = (ImageView) v.findViewById(R.id.chat_avatar);
    ImageView chatAvatarFrame = (ImageView) v.findViewById(R.id.chat_avatar_frame);
    chatAvatarFrame.setColorFilter(Color.parseColor("#dddddd"));
    author.setText(ch.getDisplayname());
    timestamp.setText(ch.getTimestamp());
    post.setText(ch.getPostbody());
    author.setTextColor(Color.parseColor(textColor));
    timestamp.setTextColor(Color.parseColor(textColor));
    post.setTextColor(Color.parseColor(textColor));


    if (this.currentAvatarSetting) {
      if (Net.isUrl(ch.getDisplayavatar())) {
        final String imageUrl = ch.getDisplayavatar();
        ImageLoader.getInstance().displayImage(imageUrl, avatar);
      } else {
        avatar.setImageResource(R.drawable.no_avatar);
      }
    } else {
      avatar.setVisibility(View.GONE);
      chatAvatarFrame = (ImageView) v.findViewById(R.id.post_avatar_frame);
      chatAvatarFrame.setVisibility(View.GONE);
    }
    return v;
  }
}
