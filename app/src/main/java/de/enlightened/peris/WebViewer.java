package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewer extends FragmentActivity {
  private ActionBar actionBar;
  private String background;
  private WebView wvMain;
  private PerisApp application;
  private AnalyticsHelper ah;

  /**
   * Called when the activity is first created.
   */

  @SuppressLint("NewApi")
  @Override
  public void onCreate(final Bundle savedInstanceState) {

    this.application = (PerisApp) getApplication();
    this.background = this.application.getSession().getServer().serverColor;
    ThemeSetter.setTheme(this, this.background);
    super.onCreate(savedInstanceState);
    ThemeSetter.setActionBar(this, this.background);
    this.actionBar = getActionBar();
    //actionBar.setDisplayHomeAsUpEnabled(true);
    //actionBar.setHomeButtonEnabled(true);
    //actionBar.setTitle(screenTitle);
    this.actionBar.setSubtitle(this.application.getSession().getServer().serverAddress);

    //Track app analytics
    this.ah = this.application.getAnalyticsHelper();
    this.ah.trackScreen(getClass().getName(), false);

    setContentView(R.layout.web_viewer);
    this.wvMain = (WebView) findViewById(R.id.web_viewer_webview);
    this.wvMain.setWebViewClient(new HelloWebViewClient());
    this.wvMain.loadUrl(this.application.getSession().getServer().getUrlString());

    new CheckForumIconTask(this.application.getSession()).execute();
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onStop() {
    super.onStop();

  }

  @Override
  public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
        if (this.wvMain.canGoBack()) {
          this.wvMain.goBack();
        } else {
          this.finish();
        }
        return true;
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.web_view_menu, menu);
    return true;
  }

  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.web_view_menu_close:
        finish();
        break;
      case R.id.web_view_menu_theme:
        final ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance();
        newFragment.setOnColorSelectedListener(new ColorPickerDialogFragment.ColorSelectedListener() {
          @SuppressWarnings("checkstyle:requirethis")
          public void onColorSelected(final String color) {
            setColor(color);
          }
        });
        newFragment.show(getSupportFragmentManager(), "dialog");
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private void setColor(final String color) {
    this.application.getSession().getServer().serverColor = color;
    this.application.getSession().updateServer();
    this.finish();
    this.startActivity(getIntent());
  }

  private class HelloWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
      view.loadUrl(url);
      return true;
    }
  }
}
