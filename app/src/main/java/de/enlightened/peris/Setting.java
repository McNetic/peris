package de.enlightened.peris;

public class Setting {
  public String settingName = "Setting";
  public String settingColor = "0";
  public int settingIcon = R.drawable.drawer_favorites;
  public int counterItem = 0;

  public Setting(final String settingName, final int settingIcon, final int counterItem) {
    this.settingName = settingName;
    this.settingIcon = settingIcon;
    this.counterItem = counterItem;
  }

  public Setting(final String settingName, final int settingIcon) {
    this.settingName = settingName;
    this.settingIcon = settingIcon;
  }
}
