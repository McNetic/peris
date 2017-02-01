package de.enlightened.peris.support;

import java.util.Date;

/**
 * Created by Nicolai Ehemann on 01.02.2017.
 */

public final class DateTimeUtils {
  private static final long MILLIS_PER_SECOND = 1000;
  private static final int SECONDS_PER_MINUTE = 60;
  private static final int MINUTES_PER_HOUR = 60;
  private static final int HOURS_PER_DAY = 24;
  private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
  private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;

  public static String getTimeAgo(final Date date) throws IllegalArgumentException {
    final String timeAgo;
    final long seconds = (new Date().getTime() - date.getTime()) / MILLIS_PER_SECOND;
    if (seconds < 0) {
      throw new IllegalArgumentException("Date is in the future");
    } else if (seconds > SECONDS_PER_DAY - 1) {
      timeAgo = String.format("%dd", seconds / SECONDS_PER_DAY);
    } else if (seconds > SECONDS_PER_HOUR - 1) {
      timeAgo = String.format("%dh", seconds / SECONDS_PER_HOUR);
    } else if (seconds > SECONDS_PER_MINUTE - 1) {
      timeAgo = String.format("%dm", seconds / SECONDS_PER_DAY);
    } else {
      timeAgo = String.format("%ds", seconds);
    }
    return timeAgo;
  }
}
