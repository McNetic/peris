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
