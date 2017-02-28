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

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageBox {
  private final int remainingMessageCount;
  private final Map<String, MessageFolder> messageFolders = new HashMap<>();
  private MessageFolder inboxFolder;
  private MessageFolder sentFolder;

  public void addMessageFolder(final MessageFolder folder) {
    this.messageFolders.put(folder.getName(), folder);
    if (this.inboxFolder == null || folder.getType().equals(MessageFolder.Type.INBOX)) {
      this.inboxFolder = folder;
    } else if (folder.getType().equals(MessageFolder.Type.SENT)) {
      this.sentFolder = folder;
    }
  }
}
