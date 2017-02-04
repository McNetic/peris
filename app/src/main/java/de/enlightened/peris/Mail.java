package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;


public class Mail extends FragmentActivity {

  @SuppressLint("NewApi")
  public void onCreate(final Bundle savedInstanceState) {
    final PerisApp application = (PerisApp) getApplication();
    final String background = application.getSession().getServer().serverColor;

    ThemeSetter.setTheme(this, background);
    super.onCreate(savedInstanceState);
    ThemeSetter.setActionBar(this, background);

    setTitle("Inbox");
    setContentView(R.layout.single_frame_activity);

    //Setup forum background
    final String forumWallpaper = application.getSession().getServer().serverWallpaper;
    final String forumBackground = application.getSession().getServer().serverBackground;
    final FrameLayout sfaHolder = (FrameLayout) findViewById(R.id.sfa_holder);
    final ImageView sfaWallpaper = (ImageView) findViewById(R.id.sfa_wallpaper);
    if (forumBackground != null && forumBackground.contains("#") && forumBackground.length() == 7) {
      sfaHolder.setBackgroundColor(Color.parseColor(forumBackground));
    } else {
      sfaHolder.setBackgroundColor(Color.parseColor(getString(R.string.default_background)));
    }

    if (forumWallpaper != null && forumWallpaper.contains("http")) {
      final String imageUrl = forumWallpaper;
      ImageLoader.getInstance().displayImage(imageUrl, sfaWallpaper);
    } else {
      sfaWallpaper.setVisibility(View.GONE);
    }

    final MailFragment mf = new MailFragment();
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction ftZ = fragmentManager.beginTransaction();
    ftZ.replace(R.id.single_frame_layout_frame, mf);
    ftZ.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ftZ.commit();
  }
}
