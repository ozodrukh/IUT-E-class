package com.ozodrukh.instant.eclass.accounts;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import com.ozodrukh.eclass.Timber;
import com.ozodrukh.instant.eclass.R;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class BlurBackgroundView extends FrameLayout {

  private int overlayColor;

  public BlurBackgroundView(Context context) {
    this(context, null);
  }

  public BlurBackgroundView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BlurBackgroundView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setWillNotDraw(false);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurBackgroundView);
    try {
      overlayColor = a.getColor(R.styleable.BlurBackgroundView_overlayColor, 0x86000000);
    } finally {
      a.recycle();
    }
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    new BackgroundLoader(this, "drawables/login_background_facebook_raw.jpg", w, h).execute();
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.drawColor(overlayColor);
  }

  @Override public boolean hasOverlappingRendering() {
    return false;
  }

  private static class BackgroundLoader extends AsyncTask<Void, Void, Bitmap> {

    private static final AtomicReference<BackgroundLoader> runningLoader = new AtomicReference<>();
    private static final WeakHashMap<Integer, Bitmap> cachedDensityBitmaps = new WeakHashMap<>();

    private AssetManager assetManager;
    private String imagePath;
    private int width, height;
    private int densityDpi;

    private WeakReference<View> viewRef;

    public BackgroundLoader(View imageView, String imagePath, int width, int height) {
      this.densityDpi = imageView.getResources().getDisplayMetrics().densityDpi;
      this.viewRef = new WeakReference<>(imageView);
      this.assetManager = imageView.getContext().getAssets();
      this.imagePath = imagePath;
      this.width = width;
      this.height = height;
    }

    @Override protected void onPostExecute(Bitmap bitmap) {
      runningLoader.getAndSet(null);

      if (viewRef.get() == null) {
        return;
      }

      View view = viewRef.get();
      Drawable originalBackground = view.getBackground();
      if (originalBackground instanceof BitmapDrawable) {
        Bitmap bmp = ((BitmapDrawable) originalBackground).getBitmap();

        if (bitmap != bmp && !cachedDensityBitmaps.containsValue(bmp)) {
          bmp.recycle();
          System.gc();
        }
      }

      TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[] {
          new ColorDrawable(Color.BLACK), new BitmapDrawable(view.getResources(), bitmap)
      });
      view.setBackground(transitionDrawable);

      transitionDrawable.setCrossFadeEnabled(true);
      transitionDrawable.startTransition(500);
    }

    @Override protected Bitmap doInBackground(Void... params) {
      synchronized (runningLoader) {
        BackgroundLoader running = runningLoader.getAndSet(this);
        if (running != null) {
          running.cancel(true);
        }
      }

      Bitmap resultImage = cachedDensityBitmaps.get(densityDpi);

      if(resultImage != null){
        return resultImage;
      }

      try {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 2;
        options.inScaled = true;
        options.inDensity = DisplayMetrics.DENSITY_XHIGH;
        options.inTargetDensity = densityDpi;

        InputStream imageStream = assetManager.open(imagePath);
        Bitmap original = BitmapFactory.decodeStream(imageStream, null, options);

        imageStream.close();

        Timber.d("w = %d, h = %d", original.getWidth(), original.getHeight());

        if (original.getWidth() > original.getHeight() && height > width) {
          original = cropImageCenter(original, 1.0f * width / height, true);
        }

        resultImage = Bitmap.createScaledBitmap(original, width, height, true);

        if (!original.isRecycled()) {
          original.recycle();
        }

        cachedDensityBitmaps.put(densityDpi, resultImage);
      } catch (IOException e) {
        e.printStackTrace();
      }

      System.gc();
      return resultImage;
    }

    static Bitmap cropImageCenter(Bitmap original, float scaleProportion, boolean recycleOrigin) {
      Timber.d("ScaleDown = %f", scaleProportion);

      Rect bounds = new Rect();

      int width = (int) (original.getWidth() * scaleProportion);
      int height = (int) (original.getHeight() * scaleProportion);

      bounds.left = (original.getWidth() - width) / 2;
      bounds.top = (original.getHeight() - height) / 2;
      bounds.right = bounds.left + width;
      bounds.bottom = bounds.top + height;

      Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
      Canvas canvas = new Canvas(bitmap);
      canvas.drawBitmap(original, bounds, new Rect(0, 0, width, height), null);

      if (recycleOrigin) {
        original.recycle();
      }

      return bitmap;
    }
  }
}
