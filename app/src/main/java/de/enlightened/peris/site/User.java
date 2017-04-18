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
package de.enlightened.peris.site;

import java.util.Date;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
  private final String userId;
  private final String userName;
  private final int postCount;
  private final Date registrationDate;
  private final Date lastActivity;
  private final boolean online;
  private final boolean acceptMessages;
  private final boolean followedByMe;
  private final boolean followsMe;
  private final boolean acceptFollow;
  private final int followingCount;
  private final int followerCount;
  private final String description;
  private final String currentAction;
  private final String topicId;
  private final String avatarUrl;
  private final Map customFields;
}
