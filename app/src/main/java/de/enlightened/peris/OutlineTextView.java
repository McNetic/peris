package de.enlightened.peris;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

public class OutlineTextView extends TextView {

  public OutlineTextView(final Context context) {
    super(context);
  }

  @Override
  public void draw(final Canvas canvas) {
    for (int i = 0; i < 5; i++) {
      super.draw(canvas);
    }
  }
}
