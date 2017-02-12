package de.enlightened.peris;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ColorPickerDialogFragment extends DialogFragment {

  private static final String TAG = ColorPickerDialogFragment.class.getName();
  private static final int MAX_OPACITY = 255;

  private boolean showOpacity = false;
  private int opacity = MAX_OPACITY;
  private LinearLayout colorPickerColorsLayout;
  private ColorSelectedListener colorSelected = null;
  private OnClickListener colorSetter = new OnClickListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onClick(final View v) {
      final String color = (String) v.getTag();
      setColor(color);
    }

  };

  static ColorPickerDialogFragment newInstance() {
    return new ColorPickerDialogFragment();
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setStyle(STYLE_NO_TITLE, getTheme());
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View v = inflater.inflate(R.layout.color_picker_dialog, container, false);
    this.setupColorPicker(v);
    return v;
  }

  private void setupColorPicker(final View v) {
    final Bundle bundle = getArguments();
    if (bundle != null) {
      if (bundle.containsKey("show_opacity")) {
        this.showOpacity = bundle.getBoolean("show_opacity");
      }
    }

    this.colorPickerColorsLayout = (LinearLayout) v.findViewById(R.id.color_picker_colors_layout);
    final LinearLayout opacityLayout = (LinearLayout) v.findViewById(R.id.color_picker_opacity_layout);

    if (this.showOpacity) {
      opacityLayout.setVisibility(View.VISIBLE);

      final SeekBar colorOpacitySeeker = (SeekBar) v.findViewById(R.id.color_opacity_seeker);
      colorOpacitySeeker.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

        @Override
        @SuppressWarnings("checkstyle:requirethis")
        public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
          // TODO Auto-generated method stub
          opacity = progress;
          final float opacityPercent = progress / (float) MAX_OPACITY;
          //Log.d(TAG ,progress + "/255 = " + opacityPercent);
          colorPickerColorsLayout.setAlpha(opacityPercent);
        }

        @Override
        public void onStartTrackingTouch(final SeekBar seekBar) {
          // TODO Auto-generated method stub
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
          // TODO Auto-generated method stub
        }

      });
    } else {
      opacityLayout.setVisibility(View.GONE);
    }

    final LinearLayout llPicker = (LinearLayout) v.findViewById(R.id.profileColorPicker);
    llPicker.setVisibility(View.VISIBLE);

    v.findViewById(R.id.pickColor1).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor2).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor3).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor4).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor5).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor6).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor7).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor8).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor9).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor10).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor11).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor12).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor13).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor14).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor15).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor16).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor17).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor18).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor19).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor20).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor21).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor22).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor23).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor24).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor25).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor26).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor27).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor28).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor29).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor30).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor31).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor32).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor33).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor34).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor35).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor36).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor37).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor38).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor39).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor40).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor41).setOnClickListener(this.colorSetter);
    v.findViewById(R.id.pickColor42).setOnClickListener(this.colorSetter);
  }

  private void setColor(final String color) {
    if (this.colorSelected == null) {
      this.dismiss();
      return;
    }

    final String selectedColor;
    if (this.opacity < MAX_OPACITY) {
      final String alphaValue = String.format("%02d", Integer.toHexString(this.opacity));
      selectedColor = color.replace("#", "#" + alphaValue);
      Log.d(TAG, "Alpha'd color is: " + selectedColor);
    } else {
      selectedColor = color;
    }
    this.colorSelected.onColorSelected(selectedColor);
    this.dismiss();
  }

  public void setOnColorSelectedListener(final ColorSelectedListener listener) {
    this.colorSelected = listener;
  }

  //Color Selected Interface
  public interface ColorSelectedListener {
    void onColorSelected(String color);
  }
}
