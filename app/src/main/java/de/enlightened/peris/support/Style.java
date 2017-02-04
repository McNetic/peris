package de.enlightened.peris.support;

import android.graphics.Color;

/**
 * Created by Nicolai Ehemann on 04.02.2017.
 */

public final class Style {
  public static String colorToColorString(final int color) {
    return String.format("%x%x%x", Color.red(color), Color.green(color), Color.blue(color));
  }

  public static String colorToAColorString(final int color) {
    return String.format("%x%s", Color.alpha(color), colorToColorString(color));
  }
}
