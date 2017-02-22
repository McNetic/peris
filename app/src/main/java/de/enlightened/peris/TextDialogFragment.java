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

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class TextDialogFragment extends DialogFragment {

  private static final int DEFAULT_FONT_SIZE = 16;
  private static final int PROGRESS_STEP = 10;

  private CheckBox cbShading;
  private CheckBox cbOpenSans;
  private SeekBar sbFontSize;
  private TextView tbSample;
  private boolean useShading = false;
  private boolean useOpenSans = false;
  private int fontSize = DEFAULT_FONT_SIZE;
  private Typeface opensans;
  private OnCheckedChangeListener shadingChecked = new OnCheckedChangeListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
      useShading = isChecked;
      updateSample();
    }

  };
  private OnCheckedChangeListener sansChecked = new OnCheckedChangeListener() {

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
      useOpenSans = isChecked;
      updateSample();
    }
  };

  private OnSeekBarChangeListener fontSizeChanged = new OnSeekBarChangeListener() {

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
      // TODO Auto-generated method stub
    }

    @Override
    @SuppressWarnings("checkstyle:requirethis")
    public void onStopTrackingTouch(final SeekBar seekBar) {
      fontSize = seekBar.getProgress() + PROGRESS_STEP;
      updateSample();
    }
  };

  static TextDialogFragment newInstance() {
    return new TextDialogFragment();
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.opensans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensans.ttf");
    this.setStyle(STYLE_NO_TITLE, getTheme());
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View v = inflater.inflate(R.layout.text_options, container, false);
    this.setupDialog(v);
    return v;
  }

  private void setupDialog(final View v) {
    this.cbShading = (CheckBox) v.findViewById(R.id.text_settings_cb_smoothing);
    this.cbOpenSans = (CheckBox) v.findViewById(R.id.text_settings_cb_opensans);
    this.sbFontSize = (SeekBar) v.findViewById(R.id.text_settings_sb_font_size);
    this.tbSample = (TextView) v.findViewById(R.id.text_settings_tb_sample);
    this.cbShading.setOnCheckedChangeListener(this.shadingChecked);
    this.cbOpenSans.setOnCheckedChangeListener(this.sansChecked);
    this.sbFontSize.setOnSeekBarChangeListener(this.fontSizeChanged);

    this.getCurrentValues();
    this.updateSample();
  }

  private void getCurrentValues() {
    final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
    this.useShading = appPreferences.getBoolean("use_shading", false);
    this.useOpenSans = appPreferences.getBoolean("use_opensans", false);
    this.fontSize = appPreferences.getInt("font_size", DEFAULT_FONT_SIZE);
    this.sbFontSize.setProgress(this.fontSize - PROGRESS_STEP);
    this.cbShading.setChecked(this.useShading);
    this.cbOpenSans.setChecked(this.useOpenSans);
  }

  private void storeNewValues() {
    final SharedPreferences appPreferences = getActivity().getSharedPreferences("prefs", 0);
    final SharedPreferences.Editor editor = appPreferences.edit();
    editor.putBoolean("use_shading", this.useShading);
    editor.putBoolean("use_opensans", this.useOpenSans);
    editor.putInt("font_size", this.fontSize);
    editor.commit();
  }

  private void updateSample() {
    this.tbSample.setTextSize(TypedValue.COMPLEX_UNIT_SP, this.fontSize);

    if (this.useShading) {
      this.tbSample.setShadowLayer(2, 0, 0, this.tbSample.getCurrentTextColor());
    } else {
      this.tbSample.setShadowLayer(0, 0, 0, 0);
    }

    if (this.useOpenSans) {
      this.tbSample.setTypeface(this.opensans);
    } else {
      this.tbSample.setTypeface(null);
    }

    this.storeNewValues();
  }

}
