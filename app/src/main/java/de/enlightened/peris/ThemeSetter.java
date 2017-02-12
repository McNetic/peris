package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

@SuppressLint("NewApi")
public final class ThemeSetter {

  private static final String TAG = ThemeSetter.class.getName();;
  private static final float TRANSPARENCY_HALF = 0.5F;
  private static final int RADIX_HEX = 16;
  private static final int DARKEN_BY_POINTS = 40;
  private static final int MAX_LIGHT_FOREGROUND = 382;

  private ThemeSetter() {
  }

  public static void setNavBarOnly(final Activity activity, final String color) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
      activity.getWindow().setNavigationBarColor(Color.parseColor(darkenColor(color)));
    }
  }

  public static void setNavAndStatusBar(final Activity activity, final String color) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      //activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
      activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
      //activity.getWindow().setStatusBarColor(Color.parseColor(darkenColor(color)));
      activity.getWindow().setStatusBarColor(Color.parseColor(darkenColor(color)));
      activity.getWindow().setNavigationBarColor(Color.parseColor(darkenColor(color)));
    }
  }

  public static void setActionBar(final Activity activity, final String color) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      final ActionBar bar = activity.getActionBar();
      bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
      final int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
      final TextView actionBarTextView = (TextView) activity.findViewById(actionBarTitleId);
      if (actionBarTextView != null) {
        actionBarTextView.setTextColor(Color.parseColor(getForeground(color)));
      }

      final int actionBarSubTitleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
      final TextView actionBarSubTextView = (TextView) activity.findViewById(actionBarSubTitleId);
      if (actionBarSubTextView != null) {
        actionBarSubTextView.setTextColor(Color.parseColor(getForeground(color)));
        actionBarSubTextView.setAlpha(TRANSPARENCY_HALF);
      }
    } else {
      activity.setTheme(android.R.style.Theme_Light);
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      activity.getWindow().setStatusBarColor(Color.parseColor(darkenColor(color)));
      activity.getWindow().setNavigationBarColor(Color.parseColor(darkenColor(color)));
    }
  }

  public static void setActionBarNoElevation(final Activity activity, final String color) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      final ActionBar bar = activity.getActionBar();
      bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));

      final int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
      final TextView actionBarTextView = (TextView) activity.findViewById(actionBarTitleId);
      if (actionBarTextView != null) {
        actionBarTextView.setTextColor(Color.parseColor(getForeground(color)));
      }

      final int actionBarSubTitleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
      final TextView actionBarSubTextView = (TextView) activity.findViewById(actionBarSubTitleId);
      if (actionBarSubTextView != null) {
        actionBarSubTextView.setTextColor(Color.parseColor(getForeground(color)));
        actionBarSubTextView.setAlpha(TRANSPARENCY_HALF);
      }
    } else {
      activity.setTheme(android.R.style.Theme_Light);
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      activity.getWindow().setStatusBarColor(Color.parseColor(darkenColor(color)));
      activity.getWindow().setNavigationBarColor(Color.parseColor(darkenColor(color)));
      activity.getActionBar().setElevation(0f);
    }
  }

  public static void setThemeNoTitlebar(final Activity activity, final String color) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        activity.setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
      } else {
        if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.LOLLIPOP) {
          activity.setTheme(android.R.style.Theme_Material_Light_NoActionBar_TranslucentDecor);
        } else {
          activity.setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        }
      }
    } else {
      activity.setTheme(android.R.style.Theme_Light_NoTitleBar);
    }
  }

  public static void setThemeFullscreen(final Activity activity, final String color) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        activity.setTheme(android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
      } else {
        activity.setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
      }
    } else {
      activity.setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
    }
  }

  public static void setTheme(final Activity activity, final String color) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        activity.setTheme(android.R.style.Theme_Holo_Light);
      } else {
        if (getForegroundDark(color)) {
          activity.setTheme(android.R.style.Theme_DeviceDefault_Light);
        } else {
          activity.setTheme(android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
        }
      }
    } else {
      activity.setTheme(android.R.style.Theme_Light);
    }
  }

  public static String darkenColor(final String colorStr) {
    int r = Integer.valueOf(colorStr.substring(1, 3), RADIX_HEX);
    int g = Integer.valueOf(colorStr.substring(3, 5), RADIX_HEX);
    int b = Integer.valueOf(colorStr.substring(5, 7), RADIX_HEX);
    if (r > DARKEN_BY_POINTS) {
      r -= DARKEN_BY_POINTS;
    } else {
      r = 0;
    }

    if (g > DARKEN_BY_POINTS) {
      g -= DARKEN_BY_POINTS;
    } else {
      g = 0;
    }

    if (b > DARKEN_BY_POINTS) {
      b -= DARKEN_BY_POINTS;
    } else {
      b = 0;
    }

    String hexR = Integer.toHexString(r);
    String hexG = Integer.toHexString(g);
    String hexB = Integer.toHexString(b);

    if (hexR.length() == 1) {
      hexR = "0" + hexR;
    }
    if (hexG.length() == 1) {
      hexG = "0" + hexG;
    }
    if (hexB.length() == 1) {
      hexB = "0" + hexB;
    }
    Log.d(TAG, "Converted " + r + "," + g + "," + b + " to " + "#" + hexR + hexG + hexB);
    return "#" + hexR + hexG + hexB;
  }

  public static String getForeground(final String hexColor) {
    if (getForegroundDark(hexColor)) {
      return "#000000";
    } else {
      return "#ffffff";
    }
  }

  public static boolean getForegroundDark(final String hexColor) {
    if (hexColor != null
        && hexColor.contains("#")
        && hexColor.length() == 7) {
      final int red = Integer.valueOf(hexColor.substring(1, 3), 16);
      final int green = Integer.valueOf(hexColor.substring(3, 5), 16);
      final int blue = Integer.valueOf(hexColor.substring(5, 7), 16);
      final int totalColor = red + green + blue;

      if (totalColor > MAX_LIGHT_FOREGROUND) {
        return true;
      }
    }
    return false;
  }
}
