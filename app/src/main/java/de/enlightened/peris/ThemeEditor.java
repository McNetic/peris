package de.enlightened.peris;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class ThemeEditor extends FragmentActivity {

  private PerisApp application;

  private AnalyticsHelper ah;

  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.application = (PerisApp) getApplication();
    this.application.setForceRefresh(true);

    //Track app analytics
    this.ah = ((PerisApp) getApplication()).getAnalyticsHelper();
    this.ah.trackScreen(getClass().getName(), false);

    this.setContentView(R.layout.theme_editor);

    this.updatePreview();
    this.juiceUpOptions();
  }

  private void updatePreview() {
    final SharedPreferences appPreferences = getSharedPreferences("prefs", 0);

    final boolean useShading = appPreferences.getBoolean("use_shading", false);
    final boolean useOpenSans = appPreferences.getBoolean("use_opensans", false);
    final int fontSize = appPreferences.getInt("font_size", 16);
    final boolean currentAvatarSetting = appPreferences.getBoolean("show_images", true);

    final TextView themeEditorPreviewAppbar = (TextView) findViewById(R.id.theme_editor_preview_appbar);
    themeEditorPreviewAppbar.setBackgroundColor(Color.parseColor(this.application.getSession().getServer().serverColor));

    if (ThemeSetter.getForegroundDark(this.application.getSession().getServer().serverColor)) {
      themeEditorPreviewAppbar.setTextColor(Color.BLACK);
    } else {
      themeEditorPreviewAppbar.setTextColor(Color.WHITE);
    }

    final FrameLayout themeEditorPreview = (FrameLayout) findViewById(R.id.theme_editor_preview);
    final ImageView themeEditorPreviewWallpaper = (ImageView) findViewById(R.id.theme_editor_preview_wallpaper);

    final String forumWallpaper = this.application.getSession().getServer().serverWallpaper;
    final String forumBackground = this.application.getSession().getServer().serverBackground;

    if (forumWallpaper != null && forumWallpaper.contains("http")) {
      final String imageUrl = forumWallpaper;
      themeEditorPreviewWallpaper.setVisibility(View.VISIBLE);
      ImageLoader.getInstance().displayImage(imageUrl, themeEditorPreviewWallpaper);
    } else {
      themeEditorPreviewWallpaper.setVisibility(View.GONE);
    }

    if (forumBackground != null && forumBackground.contains("#") && forumBackground.length() == 7) {
      themeEditorPreview.setBackgroundColor(Color.parseColor(forumBackground));
    } else {
      themeEditorPreview.setBackgroundColor(Color.parseColor(getString(R.string.default_background)));
    }

    final boolean useDivider = true;
    /*
    if (!(this.application.getSession().getServer().serverBackground.contentEquals(this.application.getSession().getServer().serverBoxColor)
        && this.application.getSession().getServer().serverBoxBorder.contentEquals("0"))) {
      useDivider = false;
    }
    */

    final LinearLayout previewHolder = (LinearLayout) findViewById(R.id.theme_editor_preview_holder);
    previewHolder.removeAllViews();

    final Category cat = new Category();
    cat.category_name = "Fun Category";
    cat.categoryType = "S";

    final Category top = new Category();
    top.category_name = "Important Thread";
    top.topicSticky = "Y";
    top.thread_count = "2";
    top.view_count = "7";
    top.category_lastthread = "NR89";
    top.categoryType = "C";
    top.hasNewTopic = true;
    top.categoryIcon = "http://localhost/nr90.jpg";

    final Post po = new Post();
    po.post_author = "nezkeeeze";
    po.post_body = "Hey guys I'm new.  How do I get colored text?  Can I be an admin?  How do I start a new topic?<br /><br />"
        + this.application.getSession().getServer().serverTagline;
    po.post_avatar = "http://localhost/nezkeys.png";

    final LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    final View vC = vi.inflate(R.layout.category, null);
    final View vT = vi.inflate(R.layout.thread, null);
    final View vP = vi.inflate(R.layout.post, null);

    ElementRenderer.renderCategory(vC, this.application, this, useOpenSans, useShading, cat, currentAvatarSetting);
    ElementRenderer.renderCategory(vT, this.application, this, useOpenSans, useShading, top, currentAvatarSetting);
    ElementRenderer.renderPost(vP, this.application, 1, this, 0, useOpenSans, useShading, po, fontSize, currentAvatarSetting);

    previewHolder.addView(vC);

    if (useDivider) {
      final View d1 = vi.inflate(R.layout.preview_seperator, null);
      previewHolder.addView(d1);
    }

    previewHolder.addView(vT);

    if (useDivider) {
      final View d2 = vi.inflate(R.layout.preview_seperator, null);
      previewHolder.addView(d2);
    }

    previewHolder.addView(vP);

    if (useDivider) {
      final View d3 = vi.inflate(R.layout.preview_seperator, null);
      previewHolder.addView(d3);
    }
  }

  private void setColor(final String color) {
    this.application.getSession().getServer().serverColor = color;
    this.application.getSession().updateServer();

    this.updatePreview();
  }

  private void setBackground(final String color) {
    this.application.getSession().getServer().serverBackground = color;
    this.application.getSession().updateServer();

    this.updatePreview();
  }

  private void setTextColor(final String color) {
    this.application.getSession().getServer().serverTextColor = color;
    this.application.getSession().updateServer();

    this.updatePreview();
  }

  private void setElementColor(final String color) {
    this.application.getSession().getServer().serverBoxColor = color;
    this.application.getSession().updateServer();

    this.updatePreview();
  }

  private void juiceUpOptions() {
    final Button btnAccent = (Button) findViewById(R.id.theme_editor_accent_color);
    btnAccent.setOnClickListener(new OnClickListener() {

      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        final ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
        newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.ColorSelectedListener() {

          public void onColorSelected(final String color) {
            setColor(color);
          }
        });
        newFragment.show(getSupportFragmentManager(), "dialog");
      }
    });

    final Button btnBackground = (Button) findViewById(R.id.theme_editor_background_color);
    btnBackground.setOnClickListener(new OnClickListener() {

      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        final ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
        newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.ColorSelectedListener() {

          public void onColorSelected(final String color) {
            setBackground(color);
          }
        });
        newFragment.show(getSupportFragmentManager(), "dialog");
      }
    });

    final Button btnTextColor = (Button) findViewById(R.id.theme_editor_text_color);
    btnTextColor.setOnClickListener(new OnClickListener() {

      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        final ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
        newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.ColorSelectedListener() {

          public void onColorSelected(final String color) {
            setTextColor(color);
          }
        });
        newFragment.show(getSupportFragmentManager(), "dialog");
      }
    });

    final Button btnElementBorder = (Button) findViewById(R.id.theme_editor_borders);
    btnElementBorder.setOnClickListener(new OnClickListener() {

      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        String currentSetting = getString(R.string.default_element_border);

        if (application.getSession().getServer().serverBoxBorder != null) {
          currentSetting = application.getSession().getServer().serverBoxBorder;
        }

        if (currentSetting.contentEquals("1")) {
          application.getSession().getServer().serverBoxBorder = "0";
        } else {
          application.getSession().getServer().serverBoxBorder = "1";
        }

        application.getSession().updateServer();

        updatePreview();
      }
    });

    final Button btnElementColor = (Button) findViewById(R.id.theme_editor_element_color);
    btnElementColor.setOnClickListener(new OnClickListener() {

      @Override
      @SuppressWarnings("checkstyle:requirethis")
      public void onClick(final View v) {
        final ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();

        final Bundle bundle = new Bundle();
        bundle.putBoolean("show_opacity", true);
        newFragment.setArguments(bundle);

        newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.ColorSelectedListener() {

          public void onColorSelected(final String color) {
            setElementColor(color);
          }
        });
        newFragment.show(getSupportFragmentManager(), "dialog");
      }
    });

    final Button btnWallpaper = (Button) findViewById(R.id.theme_editor_wallpaper);
    btnWallpaper.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(final View v) {
        final BackgroundUrlDialogFragment newFragment = BackgroundUrlDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "dialog");
      }
    });
  }

}
