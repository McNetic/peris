package de.enlightened.peris;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.enlightened.peris.support.DateTimeUtils;
import de.enlightened.peris.support.Net;

final class ElementRenderer {

  private static final int POSTS_PER_PAGE = 20;

  private ElementRenderer() {
  }

  @SuppressWarnings("checkstyle:parameternumber")
  public static void renderPost(
      final View view,
      final PerisApp application,
      final int page,
      final Context context,
      final int postNumberInPage,
      final boolean useOpenSans,
      final boolean useShading,
      final Post po,
      final int fontSize,
      final boolean currentAvatarSetting) {
    final TextView poAuthor = (TextView) view.findViewById(R.id.post_author);
    final TextView poTimestamp = (TextView) view.findViewById(R.id.post_timestamp);
    final TextView poPage = (TextView) view.findViewById(R.id.post_number);
    final TextView tvThanks = (TextView) view.findViewById(R.id.post_thanks_count);
    final TextView tvLikes = (TextView) view.findViewById(R.id.post_likes_count);
    final TextView tvOnline = (TextView) view.findViewById(R.id.post_online_status);
    final Typeface opensans = Typeface.createFromAsset(context.getAssets(), "fonts/opensans.ttf");

    if (page == -1) {
      poPage.setVisibility(View.GONE);
    } else {
      final int postNumber = ((page - 1) * POSTS_PER_PAGE) + (postNumberInPage + 1);
      poPage.setText("#" + postNumber);
    }

    final LinearLayout llBorderBackground = (LinearLayout) view.findViewById(R.id.ll_border_background);
    final LinearLayout llColorBackground = (LinearLayout) view.findViewById(R.id.ll_color_background);

    String textColor = context.getString(R.string.default_text_color);
    if (application.getSession().getServer().serverTextColor.contains("#")) {
      textColor = application.getSession().getServer().serverTextColor;
    }

    String boxColor = context.getString(R.string.default_element_background);
    if (application.getSession().getServer().serverBoxColor != null) {
      boxColor = application.getSession().getServer().serverBoxColor;
    }

    if (boxColor.contains("#")) {
      llColorBackground.setBackgroundColor(Color.parseColor(boxColor));
    } else {
      llColorBackground.setBackgroundColor(Color.TRANSPARENT);
    }

    String boxBorder = context.getString(R.string.default_element_border);
    if (application.getSession().getServer().serverBoxBorder != null) {
      boxBorder = application.getSession().getServer().serverBoxBorder;
    }

    if (boxBorder.contentEquals("1")) {
      llBorderBackground.setBackgroundResource(R.drawable.element_border);
    } else {
      llBorderBackground.setBackgroundColor(Color.TRANSPARENT);
    }

    if (useOpenSans) {
      poAuthor.setTypeface(opensans);
      poTimestamp.setTypeface(opensans);
      tvThanks.setTypeface(opensans);
      tvLikes.setTypeface(opensans);
      tvOnline.setTypeface(opensans);
      poPage.setTypeface(opensans);
    }

    if (useShading) {
      poAuthor.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
      tvThanks.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
      tvLikes.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
      tvOnline.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
    }

    final LinearLayout llPostBodyHolder = (LinearLayout) view.findViewById(R.id.post_body_holder);
    llPostBodyHolder.removeAllViews();
    //llPostBodyHolder.setMovementMethod(null);

    final ImageView poAvatar = (ImageView) view.findViewById(R.id.post_avatar);

    if (boxColor != null && boxColor.contains("#") && boxColor.length() == 7) {
      final ImageView postAvatarFrame = (ImageView) view.findViewById(R.id.post_avatar_frame);
      postAvatarFrame.setColorFilter(Color.parseColor(boxColor));
    } else {
      final ImageView postAvatarFrame = (ImageView) view.findViewById(R.id.post_avatar_frame);
      postAvatarFrame.setVisibility(View.GONE);
    }

    if (po.userOnline) {
      tvOnline.setText("ONLINE");
      tvOnline.setVisibility(View.VISIBLE);
    } else {
      tvOnline.setVisibility(View.GONE);
    }

    poAuthor.setText(po.author);
    final String timeAgo = po.timestamp;

    try {
      poTimestamp.setText(DateTimeUtils.getTimeAgo(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(po.timestamp)));
    } catch (IllegalArgumentException | ParseException ex) {
      poTimestamp.setVisibility(View.GONE);
      tvOnline.setText(po.timestamp);
      tvOnline.setVisibility(View.VISIBLE);
      tvOnline.setTextColor(Color.parseColor(textColor));
    }

    tvThanks.setText("+" + Integer.toString(po.thanksCount) + " Thanks");
    tvLikes.setText("+" + Integer.toString(po.likeCount) + " Likes");

    if (po.thanksCount == 0) {
      tvThanks.setVisibility(View.GONE);
    } else {
      tvThanks.setVisibility(View.VISIBLE);
    }

    if (po.likeCount == 0) {
      tvLikes.setVisibility(View.GONE);
    } else {
      tvLikes.setVisibility(View.VISIBLE);
    }

    if (po.userBanned) {
      poAuthor.setPaintFlags(poAuthor.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
      poAuthor.setTextColor(Color.LTGRAY);
    }

    final String postContent = po.body;
    BBCodeParser.parseCode(context, llPostBodyHolder, postContent, opensans, useOpenSans, useShading, po.attachmentList, fontSize, true, textColor, application);

    poAuthor.setTextColor(Color.parseColor(textColor));
    poTimestamp.setTextColor(Color.parseColor(textColor));
    poPage.setTextColor(Color.parseColor(textColor));

    if (po.categoryModerator != null) {
      if (po.authorId != null) {
        if (po.authorId.contentEquals(po.categoryModerator) && !po.categoryModerator.contentEquals("0")) {
          poAuthor.setTextColor(Color.BLUE);
        }
      }
    }

    if (po.authorLevel.contentEquals("D")) {
      poAuthor.setTextColor(Color.parseColor("#ffcc00"));
    }

    if (currentAvatarSetting) {
      if (Net.isUrl(po.avatar)) {
        final String imageUrl = po.avatar;
        ImageLoader.getInstance().displayImage(imageUrl, poAvatar);
      } else {
        poAvatar.setImageResource(R.drawable.no_avatar);
      }
    } else {
      poAvatar.setVisibility(View.GONE);
    }
  }

  public static void renderCategory(
      final View view,
      final PerisApp application,
      final Context context,
      final boolean useOpenSans,
      final boolean useShading,
      final Category ca,
      final boolean currentAvatarSetting) {
    final TextView tvCategoryName = (TextView) view.findViewById(R.id.category_name);
    final TextView tvCategoryLastThread = (TextView) view.findViewById(R.id.category_last_thread);
    final TextView tvCategoryUpdate = (TextView) view.findViewById(R.id.category_last_update);
    final ImageView ivSubforumIndicator = (ImageView) view.findViewById(R.id.category_subforum_indicator);
    final TextView tvThreadReplies = (TextView) view.findViewById(R.id.thread_replies);
    final TextView tvThreadViews = (TextView) view.findViewById(R.id.thread_views);

    final LinearLayout llBorderBackground = (LinearLayout) view.findViewById(R.id.ll_border_background);
    final LinearLayout llColorBackground = (LinearLayout) view.findViewById(R.id.ll_color_background);

    String textColor = context.getString(R.string.default_text_color);
    if (application.getSession().getServer().serverTextColor.contains("#")) {
      textColor = application.getSession().getServer().serverTextColor;
    }

    String boxColor = context.getString(R.string.default_element_background);
    if (application.getSession().getServer().serverBoxColor != null) {
      boxColor = application.getSession().getServer().serverBoxColor;
    }

    if (boxColor.contains("#")) {
      llColorBackground.setBackgroundColor(Color.parseColor(boxColor));
    } else {
      llColorBackground.setBackgroundColor(Color.TRANSPARENT);
    }

    String boxBorder = context.getString(R.string.default_element_border);
    if (application.getSession().getServer().serverBoxBorder != null) {
      boxBorder = application.getSession().getServer().serverBoxBorder;
    }

    if (boxBorder.contentEquals("1")) {
      llBorderBackground.setBackgroundResource(R.drawable.element_border);
    } else {
      try {
        llBorderBackground.setBackgroundResource(0);
      } catch (Exception ex) {
        // Android might be old version that cannot set background
        // didn't want to research which
        Log.d("Peris", ex.getMessage());
      }
    }

    final Typeface opensans = Typeface.createFromAsset(context.getAssets(), "fonts/opensans.ttf");
    if (useOpenSans) {
      tvCategoryName.setTypeface(opensans);
      tvCategoryLastThread.setTypeface(opensans);
      tvCategoryUpdate.setTypeface(opensans);
    }

    if (ca.type.contentEquals("S")) {
      tvCategoryLastThread.setVisibility(View.GONE);
      tvCategoryUpdate.setVisibility(View.GONE);
    } else {
      tvCategoryLastThread.setVisibility(View.VISIBLE);
      tvCategoryUpdate.setVisibility(View.VISIBLE);
    }

    tvCategoryName.setTextColor(Color.parseColor(textColor));
    tvCategoryLastThread.setTextColor(Color.parseColor(textColor));
    tvCategoryUpdate.setTextColor(Color.parseColor(textColor));

    if (tvThreadReplies != null) {
      tvThreadReplies.setTextColor(Color.parseColor(textColor));
    }

    if (tvThreadViews != null) {
      tvThreadViews.setTextColor(Color.parseColor(textColor));
    }

    if (useShading) {
      tvCategoryName.setShadowLayer(2, 0, 0, Color.parseColor("#66000000"));
    }

    String timeAgo;
    if (ca.lastUpdate == null) {
      timeAgo = "never";
    } else {
      timeAgo = ca.lastUpdate;
      if (ca.type.contentEquals("C")) {
        try {
          timeAgo = DateTimeUtils.getTimeAgo(new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH).parse(ca.lastUpdate));
        } catch (IllegalArgumentException | ParseException e) {
          timeAgo = ca.lastUpdate;
        }
      }
    }

    tvCategoryName.setText(ca.name);
    tvCategoryLastThread.setText(Html.fromHtml(ca.lastThread));
    tvCategoryUpdate.setText(timeAgo);

    if (ca.isLocked) {
      tvCategoryName.setTextColor(Color.LTGRAY);
      tvCategoryName.setText("LOCKED: " + ca.name);
    }

    if (useOpenSans) {
      if (ca.hasNewTopic) {
        tvCategoryName.setTypeface(opensans, Typeface.BOLD);
      } else {
        tvCategoryName.setTypeface(opensans, Typeface.NORMAL);
      }
    } else {
      if (ca.hasNewTopic) {
        tvCategoryName.setTypeface(null, Typeface.BOLD);
      } else {
        tvCategoryName.setTypeface(null, Typeface.NORMAL);
      }
    }

    applyAvatarSettings(currentAvatarSetting, application, ca, ivSubforumIndicator, view, boxColor);

    if (ca.type.contentEquals("C")) {
      if (tvThreadReplies != null) {
        if (ca.threadCount != null) {
          tvThreadReplies.setText(ca.threadCount);
        } else {
          tvThreadReplies.setVisibility(View.GONE);
        }
      }

      if (tvThreadViews != null) {
        if (ca.viewCount != null) {
          tvThreadViews.setText(ca.viewCount);
        } else {
          tvThreadViews.setVisibility(View.GONE);
        }
      }
    }

    if (ca.topicSticky.contentEquals("Y")) {
      tvCategoryName.setTextColor(Color.RED);
      if (useShading) {
        tvCategoryName.setShadowLayer(2, 0, 0, Color.parseColor("#66ff0000"));
      }
    }

    if (Net.isUrl(ca.url)) {
      tvCategoryUpdate.setVisibility(View.VISIBLE);
      tvCategoryUpdate.setText(ca.url);
    }
  }

  @SuppressWarnings("checkstyle:nestedifdepth")
  private static void applyAvatarSettings(
      final boolean currentAvatarSetting,
      final PerisApp application,
      final Category ca,
      final ImageView ivSubforumIndicator,
      final View view,
      final String boxColor) {
    if (currentAvatarSetting) {
      if (ca.type.contentEquals("S")) {
        if (ivSubforumIndicator != null) {
          ivSubforumIndicator.setVisibility(View.VISIBLE);

          if (Net.isUrl(ca.icon)) {
            final String imageUrl = ca.icon;
            ImageLoader.getInstance().displayImage(imageUrl, ivSubforumIndicator);
          } else {
            if (Net.isUrl(ca.url)) {
              ivSubforumIndicator.setImageResource(R.drawable.social_global_on);
            } else {

              ivSubforumIndicator.setImageResource(R.drawable.default_unread);

              if (ca.hasNewTopic) {
                if (application.getSession().getServer().serverColor.contains("#")) {
                  final String appColor = application.getSession().getServer().serverColor;
                  ivSubforumIndicator.setColorFilter(Color.parseColor(appColor));
                } else {
                  ivSubforumIndicator.setColorFilter(Color.BLACK);
                }
              } else {
                ivSubforumIndicator.setColorFilter(Color.BLACK);
              }

              /*
              if(ca.hasNewTopic) {
                if(ca.hasChildren) {
                  ivSubforumIndicator.setImageResource(R.drawable.category_unread);
                } else {
                  ivSubforumIndicator.setImageResource(R.drawable.default_unread);
                }

              } else {
                if(ca.hasChildren) {
                  ivSubforumIndicator.setImageResource(R.drawable.category_read);
                } else {
                  ivSubforumIndicator.setImageResource(R.drawable.default_read);
                }
              }
              */
            }
          }
        }
      } else {
        if (ivSubforumIndicator != null) {
          if (Net.isUrl(ca.icon)) {
            final String imageUrl = ca.icon;
            ImageLoader.getInstance().displayImage(imageUrl, ivSubforumIndicator);
          } else {
            ivSubforumIndicator.setImageResource(R.drawable.no_avatar);
          }
        }

        if (boxColor != null && boxColor.contains("#") && boxColor.length() == 7) {
          final ImageView categorySubforumIndicatorFrame = (ImageView) view.findViewById(R.id.category_subforum_indicator_frame);
          categorySubforumIndicatorFrame.setColorFilter(Color.parseColor(boxColor));
        } else {
          final ImageView categorySubforumIndicatorFrame = (ImageView) view.findViewById(R.id.category_subforum_indicator_frame);
          categorySubforumIndicatorFrame.setVisibility(View.GONE);
        }

      }
    } else {
      ivSubforumIndicator.setVisibility(View.GONE);
      final View indicator = view.findViewById(R.id.category_subforum_indicator_frame);
      if (indicator != null) {
        indicator.setVisibility(View.GONE);
      }
    }
  }

}
