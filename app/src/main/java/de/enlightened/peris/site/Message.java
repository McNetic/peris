/**
 * Copyright (C) 2017 Nicolai Ehemann
 * <p>
 * This file is part of Peris.
 * <p>
 * Peris is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Peris is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Peris.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.enlightened.peris.site;

import java.util.Date;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Message {
  private final String id;
  private final State state;
  private final String author;
  private final String authorId;
  private final boolean authorOnline;
  private final String authorAvatar;
  private final String body;
  private final Date timestamp;

  public enum State {
    Unread(1),
    Read(2),
    Replied(3),
    Forwarded(4);

    private final int id;

    State(final int id) {
      this.id = id;
    }
  }
}
