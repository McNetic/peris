/*
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

package de.enlightened.peris.support;

import java.util.Date;

public final class DateTimeUtils {
  private static final long MILLIS_PER_SECOND = 1000;
  private static final int SECONDS_PER_MINUTE = 60;
  private static final int MINUTES_PER_HOUR = 60;
  private static final int HOURS_PER_DAY = 24;
  private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
  private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;

  private DateTimeUtils() {
  }

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
