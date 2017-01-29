package de.enlightened.peris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.WeakHashMap;

public class MagicTextView extends TextView {
  private static final int DEFAULT_FOREGROUND_COLOR = 0xff000000;
  private static final int DEFAULT_BACKGROUND_COLOR = 0xff000000;
  private static final int DEFAULT_INNERSHADOW_COLOR = 0xff000000;
  private static final int DEFAULT_OUTERSHADOW_COLOR = 0xff000000;
  private static final int DEFAULT_STROKE_COLOR = 0xff000000;
  private static final float DEFAULT_STROKE_MITER = 10.0F;
  private static final float DEFAULT_SHADOW_RADIUS = 0.0001F;
  private static final int DEFAULT_TEXT_COLOR = 0xff000000;
  private ArrayList<Shadow> outerShadows;
  private ArrayList<Shadow> innerShadows;

  private WeakHashMap<String, Pair<Canvas, Bitmap>> canvasStore;

  private Canvas tempCanvas;
  private Bitmap tempBitmap;

  private Drawable foregroundDrawable;

  private float strokeWidth;
  private Integer strokeColor;
  private Join strokeJoin;
  private float strokeMiter;

  private int[] lockedCompoundPadding;
  private boolean frozen = false;

  public MagicTextView(final Context context) {
    super(context);
    this.init(null);
  }

  public MagicTextView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    this.init(attrs);
  }

  public MagicTextView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    this.init(attrs);
  }

  @SuppressLint("Recycle")
  @SuppressWarnings("deprecation")
  public final void init(final AttributeSet attrs) {
    this.outerShadows = new ArrayList<Shadow>();
    this.innerShadows = new ArrayList<Shadow>();
    if (this.canvasStore == null) {
      this.canvasStore = new WeakHashMap<String, Pair<Canvas, Bitmap>>();
    }

    if (attrs != null) {
      final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MagicTextView);

      final String typefaceName = a.getString(R.styleable.MagicTextView_typeface);
      if (typefaceName != null) {
        final Typeface tf = Typeface.createFromAsset(getContext().getAssets(), String.format("fonts/%s.ttf", typefaceName));
        setTypeface(tf);
      }

      if (a.hasValue(R.styleable.MagicTextView_foreground)) {
        final Drawable foreground = a.getDrawable(R.styleable.MagicTextView_foreground);
        if (foreground != null) {
          this.setForegroundDrawable(foreground);
        } else {
          this.setTextColor(a.getColor(R.styleable.MagicTextView_foreground, DEFAULT_FOREGROUND_COLOR));
        }
      }

      if (a.hasValue(R.styleable.MagicTextView_bgc)) {
        final Drawable background = a.getDrawable(R.styleable.MagicTextView_bgc);
        if (background != null) {
          this.setBackgroundDrawable(background);
        } else {
          this.setBackgroundColor(a.getColor(R.styleable.MagicTextView_bgc, DEFAULT_BACKGROUND_COLOR));
        }
      }

      if (a.hasValue(R.styleable.MagicTextView_innerShadowColor)) {
        this.addInnerShadow(a.getDimensionPixelSize(R.styleable.MagicTextView_innerShadowRadius, 0),
            a.getDimensionPixelOffset(R.styleable.MagicTextView_innerShadowDx, 0),
            a.getDimensionPixelOffset(R.styleable.MagicTextView_innerShadowDy, 0),
            a.getColor(R.styleable.MagicTextView_innerShadowColor, DEFAULT_INNERSHADOW_COLOR));
      }

      if (a.hasValue(R.styleable.MagicTextView_outerShadowColor)) {
        this.addOuterShadow(a.getDimensionPixelSize(R.styleable.MagicTextView_outerShadowRadius, 0),
            a.getDimensionPixelOffset(R.styleable.MagicTextView_outerShadowDx, 0),
            a.getDimensionPixelOffset(R.styleable.MagicTextView_outerShadowDy, 0),
            a.getColor(R.styleable.MagicTextView_outerShadowColor, DEFAULT_OUTERSHADOW_COLOR));
      }

      if (a.hasValue(R.styleable.MagicTextView_strokeColor)) {
        final float setStrokeWidth = a.getDimensionPixelSize(R.styleable.MagicTextView_strokeWidth, 1);
        final int setStrokeColor = a.getColor(R.styleable.MagicTextView_strokeColor, DEFAULT_STROKE_COLOR);
        final float setStrokeMiter = a.getDimensionPixelSize(R.styleable.MagicTextView_strokeMiter, 10);
        final Join setStrokeJoin;
        switch (a.getInt(R.styleable.MagicTextView_strokeJoinStyle, 0)) {
          case 0:
            setStrokeJoin = Join.MITER;
            break;
          case 1:
            setStrokeJoin = Join.BEVEL;
            break;
          case 2:
            setStrokeJoin = Join.ROUND;
            break;
          default:
            setStrokeJoin = null;
        }
        this.setStroke(setStrokeWidth, setStrokeColor, setStrokeJoin, setStrokeMiter);
      }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
        && (this.innerShadows.size() > 0
        || this.foregroundDrawable != null
    )
        ) {
      this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
  }

  public final void setStroke(final float width, final int color, final Join join, final float miter) {
    this.strokeWidth = width;
    this.strokeColor = color;
    this.strokeJoin = join;
    this.strokeMiter = miter;
  }

  public final void setStroke(final float width, final int color) {
    this.setStroke(width, color, Join.MITER, DEFAULT_STROKE_MITER);
  }

  public final void addOuterShadow(final float r, final float dx, final float dy, final int color) {
   this.outerShadows.add(new Shadow(r == 0 ? DEFAULT_SHADOW_RADIUS : r, dx, dy, color));
  }

  public final void addInnerShadow(final float r, final float dx, final float dy, final int color) {
    this.innerShadows.add(new Shadow(r == 0 ? DEFAULT_SHADOW_RADIUS : r, dx, dy, color));
  }

  public final void clearInnerShadows() {
    this.innerShadows.clear();
  }

  public final void clearOuterShadows() {
    this.outerShadows.clear();
  }

  public final void setForegroundDrawable(final Drawable d) {
    this.foregroundDrawable = d;
  }

  public final Drawable getForeground() {
    return this.foregroundDrawable == null ? this.foregroundDrawable : new ColorDrawable(this.getCurrentTextColor());
  }

  @SuppressLint("DrawAllocation")
  @SuppressWarnings("deprecation")
  @Override
  public final void onDraw(final Canvas canvas) {
    super.onDraw(canvas);

    this.freeze();
    final Drawable restoreBackground = this.getBackground();
    final Drawable[] restoreDrawables = this.getCompoundDrawables();
    final int restoreColor = this.getCurrentTextColor();

    this.setCompoundDrawables(null, null, null, null);

    for (Shadow shadow : this.outerShadows) {
      this.setShadowLayer(shadow.r, shadow.dx, shadow.dy, shadow.color);
      super.onDraw(canvas);
    }
    this.setShadowLayer(0, 0, 0, 0);
    this.setTextColor(restoreColor);

    if (this.foregroundDrawable != null && this.foregroundDrawable instanceof BitmapDrawable) {
      this.generateTempCanvas();
      super.onDraw(this.tempCanvas);
      final Paint paint = ((BitmapDrawable) this.foregroundDrawable).getPaint();
      paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
      this.foregroundDrawable.setBounds(canvas.getClipBounds());
      this.foregroundDrawable.draw(this.tempCanvas);
      canvas.drawBitmap(this.tempBitmap, 0, 0, null);
      this.tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    if (this.strokeColor != null) {
      final TextPaint paint = this.getPaint();
      paint.setStyle(Style.STROKE);
      paint.setStrokeJoin(this.strokeJoin);
      paint.setStrokeMiter(this.strokeMiter);
      this.setTextColor(this.strokeColor);
      paint.setStrokeWidth(this.strokeWidth);
      super.onDraw(canvas);
      paint.setStyle(Style.FILL);
      this.setTextColor(restoreColor);
    }
    if (this.innerShadows.size() > 0) {
      this.generateTempCanvas();
      final TextPaint paint = this.getPaint();
      for (Shadow shadow : this.innerShadows) {
        this.setTextColor(shadow.color);
        super.onDraw(this.tempCanvas);
        this.setTextColor(DEFAULT_TEXT_COLOR);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        paint.setMaskFilter(new BlurMaskFilter(shadow.r, BlurMaskFilter.Blur.NORMAL));

        this.tempCanvas.save();
        this.tempCanvas.translate(shadow.dx, shadow.dy);
        super.onDraw(this.tempCanvas);
        this.tempCanvas.restore();
        canvas.drawBitmap(this.tempBitmap, 0, 0, null);
        this.tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        paint.setXfermode(null);
        paint.setMaskFilter(null);
        this.setTextColor(restoreColor);
        this.setShadowLayer(0, 0, 0, 0);
      }
    }


    if (restoreDrawables != null) {
      this.setCompoundDrawablesWithIntrinsicBounds(restoreDrawables[0], restoreDrawables[1], restoreDrawables[2], restoreDrawables[3]);
    }
    this.setBackgroundDrawable(restoreBackground);
    this.setTextColor(restoreColor);

    this.unfreeze();
  }

  @SuppressLint("DefaultLocale")
  private void generateTempCanvas() {
    final String key = String.format("%dx%d", getWidth(), getHeight());
    final Pair<Canvas, Bitmap> stored = this.canvasStore.get(key);
    if (stored != null) {
      this.tempCanvas = stored.first;
      this.tempBitmap = stored.second;
    } else {
      this.tempCanvas = new Canvas();
      this.tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
      this.tempCanvas.setBitmap(this.tempBitmap);
      this.canvasStore.put(key, new Pair<Canvas, Bitmap>(this.tempCanvas, this.tempBitmap));
    }
  }


  // Keep these things locked while onDraw in processing
  public final void freeze() {
    this.lockedCompoundPadding = new int[]{
        this.getCompoundPaddingLeft(),
        this.getCompoundPaddingRight(),
        this.getCompoundPaddingTop(),
        this.getCompoundPaddingBottom(),
    };
    this.frozen = true;
  }

  public final void unfreeze() {
    this.frozen = false;
  }

  @Override
  public void requestLayout() {
    if (!this.frozen) {
      super.requestLayout();
    }
  }

  @Override
  public void postInvalidate() {
    if (!this.frozen) {
      super.postInvalidate();
    }
  }

  @Override
  public void postInvalidate(final int left, final int top, final int right, final int bottom) {
    if (!this.frozen) {
      super.postInvalidate(left, top, right, bottom);
    }
  }

  @Override
  public void invalidate() {
    if (!this.frozen) {
      super.invalidate();
    }
  }

  @Override
  public void invalidate(final Rect rect) {
    if (!this.frozen) {
      super.invalidate(rect);
    }
  }

  @Override
  public void invalidate(final int l, final int t, final int r, final int b) {
    if (!this.frozen) {
      super.invalidate(l, t, r, b);
    }
  }

  @Override
  public int getCompoundPaddingLeft() {
    return !this.frozen ? super.getCompoundPaddingLeft() : this.lockedCompoundPadding[0];
  }

  @Override
  public int getCompoundPaddingRight() {
    return !this.frozen ? super.getCompoundPaddingRight() : this.lockedCompoundPadding[1];
  }

  @Override
  public int getCompoundPaddingTop() {
    return !this.frozen ? super.getCompoundPaddingTop() : this.lockedCompoundPadding[2];
  }

  @Override
  public int getCompoundPaddingBottom() {
    return !this.frozen ? super.getCompoundPaddingBottom() : this.lockedCompoundPadding[3];
  }

  public static class Shadow {
    private float r;
    private float dx;
    private float dy;
    private int color;

    public Shadow(final float r, final float dx, final float dy, final int color) {
      this.r = r;
      this.dx = dx;
      this.dy = dy;
      this.color = color;
    }
  }
}
