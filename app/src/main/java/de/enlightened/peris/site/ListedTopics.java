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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListedTopics implements Serializable {
  private final String forumId;
  private final String forumName;
  private final int count;
  private final int unreadAnnouncementCount;
  private final int unreadStickyCount;
  private final boolean postAllowed;
  private final boolean subscriptionAllowed;
  private final boolean subscribed;
  private final List<Topic> topics = new ArrayList<Topic>();

  public void addTopic(final Topic topic) {
    this.topics.add(topic);
  }

  //TODO: get rid of this? is this really a TopicItem?
  private final String id;
}
