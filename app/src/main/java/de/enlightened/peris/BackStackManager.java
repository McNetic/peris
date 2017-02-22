/*
 * Copyright (C) 2014 - 2015 Initial Author
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

package de.enlightened.peris;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class BackStackManager {

  private static final String TAG = BackStackManager.class.getName();

  private ArrayList<ArrayList<BackStackItem>> backstackArray;

  public BackStackManager() {
    this.clearAllStacks();
  }

  public final void clearAllStacks() {
    this.backstackArray = new ArrayList<ArrayList<BackStackItem>>();
  }

  public final int createBackstack() {
    final ArrayList<BackStackItem> freshBackstack = new ArrayList<BackStackItem>();
    this.backstackArray.add(freshBackstack);
    return this.backstackArray.size() - 1;
  }

  public final int getBackStackSize(final int backstackId) {
    return this.backstackArray.get(backstackId).size();
  }

  public final void addToBackstack(final int backstackId, final int type, final Bundle bundle) {
    Log.i(TAG, "Backstack Adding Item " + type);
    final BackStackItem item = new BackStackItem(type, bundle);
    this.backstackArray.get(backstackId).add(item);
  }

  public final BackStackItem getActiveItem(final int backstackId) {
    if (this.backstackArray.get(backstackId).size() == 0) {
      return null;
    }

    return this.backstackArray.get(backstackId).get(this.backstackArray.get(backstackId).size() - 1);
  }

  public final BackStackItem getActiveItemAndRemove(final int backstackId) {
    if (this.backstackArray.get(backstackId).size() == 0) {
      return null;
    }

    final BackStackItem returnItem = this.backstackArray.get(backstackId).get(this.backstackArray.get(backstackId).size() - 1);
    this.backstackArray.get(backstackId).remove(this.backstackArray.get(backstackId).size() - 1);

    return returnItem;
  }

  public final BackStackItem navigateToBase(final int backstackId) {
    if (this.backstackArray.get(backstackId).size() == 0) {
      return null;
    }

    final BackStackItem returnItem = this.backstackArray.get(backstackId).get(0);
    this.backstackArray.get(backstackId).clear();
    return returnItem;
  }

  public final BackStackItem navigateBack(final int backstackId) {

    if (this.backstackArray.get(backstackId).size() == 0) {
      return null;
    }

    this.backstackArray.get(backstackId).remove(this.backstackArray.get(backstackId).size() - 1);

    final BackStackItem returnItem = this.backstackArray.get(backstackId).get(this.backstackArray.get(backstackId).size() - 1);

    this.backstackArray.get(backstackId).remove(this.backstackArray.get(backstackId).size() - 1);

    return returnItem;
  }

  public class BackStackItem {

    public static final int BACKSTACK_TYPE_FORUM = 1;
    public static final int BACKSTACK_TYPE_TOPIC = 2;
    public static final int BACKSTACK_TYPE_PROFILE = 3;
    public static final int BACKSTACK_TYPE_SETTINGS = 4;

    private int type;
    private Bundle params;

    public BackStackItem(final int itemType, final Bundle itemBundle) {
      this.type = itemType;
      this.params = itemBundle;
    }

    public final int getType() {
      return this.type;
    }

    public final Bundle getBundle() {
      return this.params;
    }
  }
}
