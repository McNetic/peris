package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

@SuppressLint({"NewApi", "InflateParams"})
public class SettingsAdapter extends BaseAdapter {
  private Context context;
  private ArrayList<Setting> data;

  SettingsAdapter(final ArrayList<Setting> data, final Context c) {
    this.data = data;
    this.context = c;
  }

  public int getCount() {
    return this.data.size();
  }

  public Object getItem(final int arg0) {
    return this.data.get(arg0);
  }

  public long getItemId(final int arg0) {
    return arg0;
  }

  public View getView(final int arg0, final View arg1, final ViewGroup arg2) {
    View view = arg1;
    if (view == null) {
      final LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = vi.inflate(R.layout.settings_item, null);
    }

    final TextView tvSubject = (TextView) view.findViewById(R.id.settings_name);
    final ImageView ivLogo = (ImageView) view.findViewById(R.id.settings_logo);
    final TextView tvCounter = (TextView) view.findViewById(R.id.settings_counter);
    final Setting setting = this.data.get(arg0);

    tvSubject.setText(setting.settingName);
    ivLogo.setImageResource(setting.settingIcon);

    if (setting.settingColor.contains("#")) {
      ivLogo.setColorFilter(Color.parseColor(setting.settingColor));
    } else {
      ivLogo.setColorFilter(Color.parseColor("#000000"));
    }
    if (setting.counterItem == 0) {
      tvCounter.setVisibility(View.GONE);
    } else {
      tvCounter.setText(Integer.toString(setting.counterItem));
    }
    return view;
  }
}
