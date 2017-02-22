package de.enlightened.peris.site;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Nicolai Ehemann on 18.02.2017.
 */
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
