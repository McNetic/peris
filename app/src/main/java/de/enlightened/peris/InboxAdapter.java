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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.enlightened.peris.support.DateTimeUtils;
import de.enlightened.peris.support.Net;

@SuppressLint({"NewApi", "InflateParams"})
public class InboxAdapter extends BaseAdapter {

  private static final String TAG = InboxAdapter.class.getName();
  private static final int MAX_ITEM_COUNT = 50;
  private static final int DEFAULT_FONT_SIZE = 16;

  private Context context;
  private List<InboxItem> data;
  private boolean useShading = false;
  private boolean useOpenSans = false;
  private int fontSize = DEFAULT_FONT_SIZE;
  private PerisApp application;
  private OnClickListener deleteClicked = new OnClickListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View view) {
      final int itemId = (Integer) view.getTag();
      final InboxItem inboxItem = data.get(itemId);

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        new DeleteMessageTask(InboxAdapter.this.application.getSession().getApi(),
            inboxItem.messageId, inboxItem.folderId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      } else {
        new DeleteMessageTask(InboxAdapter.this.application.getSession().getApi(),
            inboxItem.messageId, inboxItem.folderId).execute();
      }

      inboxItem.isDeleted = true;
      data.remove(itemId);

      notifyDataSetChanged();
    }
  };

  InboxAdapter(final List<InboxItem> data, final Context context, final PerisApp application) {
    this.data = data;
    this.context = context;
    this.application = application;

    final SharedPreferences appPreferences = context.getSharedPreferences("prefs", 0);

    this.useShading = appPreferences.getBoolean("use_shading", false);
    this.useOpenSans = appPreferences.getBoolean("use_opensans", false);
    this.fontSize = appPreferences.getInt("font_size", DEFAULT_FONT_SIZE);
  }

  public int getCount() {
    return this.data.size();
  }

  public Object getItem(final int id) {
    return this.data.get(id);
  }

  public long getItemId(final int id) {
    return id;
  }

  public View getView(final int arg0, final View view, final ViewGroup viewGroup) {
    final View inboxView;
    if (view != null) {
      inboxView = view;
    } else {
      final LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inboxView = vi.inflate(R.layout.inbox_item, null);
    }

    final LinearLayout llBorderBackground = (LinearLayout) inboxView.findViewById(R.id.ll_border_background);
    final LinearLayout llColorBackground = (LinearLayout) inboxView.findViewById(R.id.ll_color_background);

    String textColor = this.context.getString(R.string.default_text_color);
    if (this.application.getSession().getServer().serverTextColor.contains("#")) {
      textColor = this.application.getSession().getServer().serverTextColor;
    }

    String boxColor = this.context.getString(R.string.default_element_background);
    if (this.application.getSession().getServer().serverBoxColor != null) {
      boxColor = this.application.getSession().getServer().serverBoxColor;
    }
    if (boxColor.contains("#")) {
      llColorBackground.setBackgroundColor(Color.parseColor(boxColor));
    } else {
      llColorBackground.setBackgroundColor(Color.TRANSPARENT);
    }

    String boxBorder = this.context.getString(R.string.default_element_border);
    if (this.application.getSession().getServer().serverBoxBorder != null) {
      boxBorder = this.application.getSession().getServer().serverBoxBorder;
    }
    if (boxBorder.contentEquals("1")) {
      llBorderBackground.setBackgroundResource(R.drawable.element_border);
    } else {
      llBorderBackground.setBackgroundColor(Color.TRANSPARENT);
    }

    final TextView tvSubject = (TextView) inboxView.findViewById(R.id.inbox_subject);
    final TextView tvUpdated = (TextView) inboxView.findViewById(R.id.inbox_sender);
    final TextView tvTimestamp = (TextView) inboxView.findViewById(R.id.inbox_timestamp);
    final ImageView ivSubforumIndicator = (ImageView) inboxView.findViewById(R.id.inbox_avatar);
    final Typeface opensans = Typeface.createFromAsset(this.context.getAssets(), "fonts/opensans.ttf");

    if (this.useOpenSans) {
      tvSubject.setTypeface(opensans);
      tvUpdated.setTypeface(opensans);
    }

    final InboxItem ii = this.data.get(arg0);
    if (Net.isUrl(ii.senderAvatar)) {
      ImageLoader.getInstance().displayImage(ii.senderAvatar, ivSubforumIndicator);
    } else {
      ivSubforumIndicator.setImageResource(R.drawable.no_avatar);
    }

    if (boxColor != null && boxColor.contains("#") && boxColor.length() == 7) {
      final ImageView categorySubforumIndicatorFrame = (ImageView) inboxView.findViewById(R.id.inbox_avatar_frame);
      categorySubforumIndicatorFrame.setColorFilter(Color.parseColor(boxColor));
    } else {
      final ImageView categorySubforumIndicatorFrame = (ImageView) inboxView.findViewById(R.id.inbox_avatar_frame);
      categorySubforumIndicatorFrame.setVisibility(View.GONE);
    }

    tvSubject.setTextColor(Color.parseColor(textColor));
    tvUpdated.setTextColor(Color.parseColor(textColor));
    tvTimestamp.setTextColor(Color.parseColor(textColor));

    if (this.useShading) {
      tvSubject.setShadowLayer(2, 0, 0, Color.parseColor(textColor.replace("#", "#66")));
      tvUpdated.setShadowLayer(2, 0, 0, Color.parseColor(textColor.replace("#", "#66")));
      tvTimestamp.setShadowLayer(2, 0, 0, Color.parseColor(textColor.replace("#", "#66")));
    }

    tvSubject.setText(ii.subject);
    tvUpdated.setText(ii.sender);

    if (ii.isUnread) {
      if (this.application.getSession().getServer().serverColor.contains("#")) {
        tvSubject.setTextColor(Color.parseColor(this.application.getSession().getServer().serverColor));
        if (this.useShading) {
          tvSubject.setShadowLayer(2, 0, 0, Color.parseColor(this.application.getSession().getServer().serverColor.replace("#", "#66")));
        }
      } else {
        tvSubject.setTextColor(Color.RED);

        if (this.useShading) {
          tvSubject.setShadowLayer(2, 0, 0, Color.parseColor("#66ff0000"));
        }
      }
      tvSubject.setTypeface(null, Typeface.BOLD);
    } else {
      tvSubject.setTextColor(Color.parseColor(textColor));
      tvSubject.setTypeface(null, Typeface.NORMAL);
      if (this.useShading) {
        tvSubject.setShadowLayer(2, 0, 0, Color.parseColor(textColor.replace("#", "#66")));
      }
    }

    try {
      tvTimestamp.setText(DateTimeUtils.getTimeAgo(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(ii.sentDate)));
    } catch (IllegalArgumentException | ParseException ex) {
      tvTimestamp.setVisibility(View.GONE);
    }

    final ImageView inboxDelete = (ImageView) inboxView.findViewById(R.id.inbox_delete);
    inboxDelete.setTag(arg0);
    inboxDelete.setOnClickListener(this.deleteClicked);

    return inboxView;
  }
}
