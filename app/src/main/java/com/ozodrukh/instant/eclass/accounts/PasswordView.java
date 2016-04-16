package com.ozodrukh.instant.eclass.accounts;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;
import com.ozodrukh.instant.eclass.R;
import com.ozodrukh.instant.eclass.utils.AndroidUtils;

/**
 * An EditText implementing the material design guidelines for password input:
 * https://www.google.com/design/spec/components/text-fields.html#text-fields-password-input
 * <p/>
 * It uses the right drawable for the visibility indicator.  If you try to use it yourself, you
 * will have a bad time.
 */
public class PasswordView extends EditText {

  private Drawable eye;
  private Drawable eyeWithStrike;
  private final static int VISIBILITY_ENABLED = (int) (255 * .54f); // 54%
  private final static int VISIBILITY_DISABLED = (int) (255 * .38f); // 38%
  private boolean visible = false;
  private boolean useStrikeThrough = false;

  public PasswordView(Context context) {
    super(context);
    init(null);
  }

  public PasswordView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public PasswordView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public PasswordView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    // Make sure to mutate so that if there are multiple password fields, they can have
    // different visibilities.
    eye = AndroidUtils.loadVectorDrawable(getContext(), R.drawable.ic_eye).mutate();
    eyeWithStrike =
        AndroidUtils.loadVectorDrawable(getContext(), R.drawable.ic_eye_strike).mutate();
    eyeWithStrike.setAlpha(VISIBILITY_ENABLED);
    setup();
  }

  protected void setup() {
    setInputType(
        InputType.TYPE_CLASS_TEXT | (visible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            : InputType.TYPE_TEXT_VARIATION_PASSWORD));
    Drawable drawable = useStrikeThrough && !visible ? eyeWithStrike : eye;
    Drawable[] drawables = getCompoundDrawables();
    setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawable, drawables[3]);
    eye.setAlpha(visible && !useStrikeThrough ? VISIBILITY_ENABLED : VISIBILITY_DISABLED);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP && event.getX() >= (getRight()
        - getCompoundDrawables()[2].getBounds().width())) {
      visible = !visible;
      setup();
      invalidate();
      return true;
    }

    return super.onTouchEvent(event);
  }

  @Override public void setInputType(int type) {
    Typeface typeface = getTypeface();
    super.setInputType(type);
    setTypeface(typeface);
  }

  public void setUseStrikeThrough(boolean useStrikeThrough) {
    this.useStrikeThrough = useStrikeThrough;
  }
}