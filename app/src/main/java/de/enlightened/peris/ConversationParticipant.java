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
import android.content.res.Resources;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import de.enlightened.peris.support.Net;

public class ConversationParticipant extends ImageView {
  private static final float AVATAR_MARGIN = 6F;
  private static final float AVATAR_SIZE = 48F;
  private static final int AVATAR_ALPHA = 100;

  private ImageLoader imageLoader;
  private String username;
  private String userid;
  private String userColor;
  private String userStatus;

  @SuppressWarnings("deprecation")
  public ConversationParticipant(final Context context) {
    super(context);

    this.imageLoader = ImageLoader.getInstance();
    final DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().build();
    final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(defaultOptions).build();
    ImageLoader.getInstance().init(config);

    final Resources r = getResources();
    final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, AVATAR_MARGIN, r.getDisplayMetrics());
    final int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, AVATAR_SIZE, r.getDisplayMetrics());

    final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
    lp.setMargins(margin, margin, margin, margin);
    this.setLayoutParams(lp);
    this.setScaleType(ScaleType.CENTER_CROP);
  }

  public void setUserid(final String userid) {
    this.userid = userid;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getUserId() {
    return this.userid;
  }

  public String getUserColor() {
    return this.userColor;
  }

  public void setUserColor(final String color) {
    this.userColor = color;
  }

  public String getUserStatus() {
    return this.userStatus;
  }

  @SuppressWarnings("deprecation")
  @SuppressLint("NewApi")
  public void setUserStatus(final String status) {
    this.userStatus = status;

    if (status.contentEquals("O")) {
      if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
        this.setAlpha(AVATAR_ALPHA);
      } else {
        this.setImageAlpha(AVATAR_ALPHA);
      }
    }
  }

  public void setImage(final String imageURL) {
    if (Net.isUrl(imageURL)) {
      this.imageLoader.displayImage(imageURL, this);
    } else {
      this.setImageResource(R.drawable.no_avatar);
    }
  }
}
