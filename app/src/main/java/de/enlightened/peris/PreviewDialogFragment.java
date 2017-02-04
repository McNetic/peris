package de.enlightened.peris;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.enlightened.peris.support.Style;


public class PreviewDialogFragment extends DialogFragment {

  private static final int DEFAULT_FONT_SIZE = 16;

  private LinearLayout previewDialogLinearLayout;
  private boolean useShading = false;
  private boolean useOpenSans = false;
  private int fontSize = DEFAULT_FONT_SIZE;
  private String previewText;
  private Typeface opensans;

  static PreviewDialogFragment newInstance() {
    return new PreviewDialogFragment();
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.opensans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensans.ttf");
    final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
    this.useShading = appPreferences.getBoolean("use_shading", false);
    this.useOpenSans = appPreferences.getBoolean("use_opensans", true);
    this.fontSize = appPreferences.getInt("font_size", DEFAULT_FONT_SIZE);
    this.setStyle(DialogFragment.STYLE_NORMAL, getTheme());
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.preview_dialog_layout, container, false);
    this.setupDialog(view);
    return view;
  }

  private void setupDialog(final View view) {
    this.previewDialogLinearLayout = (LinearLayout) view.findViewById(R.id.preview_dialog_linear_layout);
    final Bundle bundle = getArguments();
    this.previewText = bundle.getString("text");
    this.showPreview();
    this.getDialog().setTitle("Preview");
  }

  private void showPreview() {
    final String color = Style.colorToColorString(ContextCompat.getColor(this.getActivity(), R.color.color_preview));
    BBCodeParser.parseCode(this.getActivity(), this.previewDialogLinearLayout, this.previewText,
        this.opensans, this.useOpenSans, this.useShading, null, this.fontSize, false,
        color, (PerisApp) getActivity().getApplication());
  }

}
