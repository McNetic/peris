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

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Config {
  private final String serverPluginVersion;
  private final ForumSystem forumSystem;
  private final String avatarSubmissionName;
  private final Boolean accountManagementEnabled;

  public enum ForumSystem {
    UNKNOWN,
    PHPBB,
    MYBB,
    VBULLETIN
  }

  public static class ConfigBuilder {
    public ConfigBuilder serverPluginVersion(final String newServerPluginVersion) {
      this.serverPluginVersion = newServerPluginVersion;
      if (this.serverPluginVersion == null) {
        this.forumSystem = ForumSystem.UNKNOWN;
        this.avatarSubmissionName = "uploadfile";
      } else if (this.serverPluginVersion.contains("pb")) {
        this.forumSystem = ForumSystem.PHPBB;
        this.avatarSubmissionName = "uploadfile";
      } else if (this.serverPluginVersion.contains("mb")) {
        this.forumSystem = ForumSystem.MYBB;
        this.avatarSubmissionName = "avatarupload";
      } else if (this.serverPluginVersion.contains("vb")) {
        this.forumSystem = ForumSystem.VBULLETIN;
        this.avatarSubmissionName = "upload";
      } else {
        this.forumSystem = ForumSystem.UNKNOWN;
        this.avatarSubmissionName = "uploadfile";
      }
      return this;
    }
  }
}
