package de.enlightened.peris;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import de.enlightened.peris.support.Net;

public class BackgroundUrlDialogFragment extends DialogFragment {

  private EditText etUrl;
  private Button btnSave;
  private Button btnBackgroundBag;
  private Button btnClearWallpaper;
  private String currentURL = "";
  private PerisApp application;
  private OnClickListener goToBackgroundBag = new OnClickListener() {

    @Override
    public void onClick(final View v) {
      final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.backgroundbag.com"));
      startActivity(browserIntent);
    }

  };

  @SuppressWarnings("checkstyle:requirethis")
  private OnClickListener saveURL = new OnClickListener() {

    @Override
    public void onClick(final View v) {
      if (Net.isUrl(etUrl.getText().toString().trim())) {
        currentURL = etUrl.getText().toString().trim();
        application.getSession().getServer().serverWallpaper = currentURL;
        application.getSession().updateServer();
        BackgroundUrlDialogFragment.this.dismiss();

        getActivity().finish();
        getActivity().startActivity(getActivity().getIntent());

      }
    }

  };

  @SuppressWarnings("checkstyle:requirethis")
  private OnClickListener clearWallpaper = new OnClickListener() {

    @Override
    public void onClick(final View v) {
      if (Net.isUrl(etUrl.getText().toString().trim())) {
        currentURL = "0";
        application.getSession().getServer().serverWallpaper = currentURL;
        application.getSession().updateServer();
        BackgroundUrlDialogFragment.this.dismiss();

        getActivity().finish();
        getActivity().startActivity(getActivity().getIntent());

      }
    }

  };

  static BackgroundUrlDialogFragment newInstance() {
    return new BackgroundUrlDialogFragment();
  }

  @Override
  public final void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.application = (PerisApp) getActivity().getApplication();
    this.currentURL = this.application.getSession().getServer().serverWallpaper;

    this.setStyle(STYLE_NO_TITLE, getTheme());
  }

  @Override
  @SuppressWarnings("checkstyle:requirethis")
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View v = inflater.inflate(R.layout.wallpaper_url_window, container, false);

    setupDialog(v);

    return v;
  }

  @SuppressWarnings("checkstyle:requirethis")
  private void setupDialog(final View v) {

    this.etUrl = (EditText) v.findViewById(R.id.etWallpaperURL);
    this.btnSave = (Button) v.findViewById(R.id.btnSetWallpaper);
    this.btnBackgroundBag = (Button) v.findViewById(R.id.btnFindWallpapers);
    this.btnClearWallpaper = (Button) v.findViewById(R.id.btnClearWallpaper);

    this.etUrl.setOnFocusChangeListener(new OnFocusChangeListener() {

      @Override
      public void onFocusChange(final View v, final boolean hasFocus) {
        if (hasFocus) {
          if (etUrl.getText().length() > 0) {
            etUrl.selectAll();
          }
        }
      }

    });

    if (Net.isUrl(currentURL)) {
      etUrl.setText(currentURL);
    }

    btnBackgroundBag.setOnClickListener(goToBackgroundBag);
    btnClearWallpaper.setOnClickListener(clearWallpaper);
    btnSave.setOnClickListener(saveURL);
  }
}
