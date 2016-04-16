package com.ozodrukh.instant.eclass.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.TextViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.ozodrukh.eclass.InhaEclassController;
import com.ozodrukh.eclass.InhaEclassWebService;
import com.ozodrukh.eclass.InhaSessionEncoder;
import com.ozodrukh.eclass.Timber;
import com.ozodrukh.eclass.entity.User;
import com.ozodrukh.instant.eclass.BaseFragment;
import com.ozodrukh.instant.eclass.R;
import com.ozodrukh.instant.eclass.utils.AndroidUtils;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class LoginEclassFragment extends BaseFragment {

  /** Provides access to signed user when he was authenticated in Application */
  public static final String KEY_USER = "arg:user";

  public static final String TAG = "fragment:sign-in";

  private View signInForm;
  private View signInButton;
  private EditText userIdView;
  private EditText userPasswordView;
  private TextView loginStatusView;
  private ProgressBar progressBar;

  private Deque<Snackbar> snackBarQueue = new ArrayDeque<>();

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.eclass_login_fragmnet, container, false);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    userIdView = (EditText) view.findViewById(R.id.user_id);
    userPasswordView = (EditText) view.findViewById(R.id.password);
    signInForm = view.findViewById(R.id.sign_in_form);
    signInButton = view.findViewById(R.id.signIn);

    progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
    loginStatusView = (TextView) view.findViewById(R.id.additional_subtitle);

    loginStatusView.setOnClickListener(new View.OnClickListener() {
      int clicksCounter = 0;

      @Override public void onClick(View v) {
        clicksCounter++;

        if (clicksCounter == 5) {
          clicksCounter = 0;
          showSnackbar("Hey, dude, here is no eastern eggs:)", Snackbar.LENGTH_LONG);
        }
      }
    });

    signInButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= 19) {
          signInButton.cancelPendingInputEvents();
        }

        setLoginFormEnabled(false);

        if (AndroidUtils.isNetworkAvailable(getContext())) {
          setAdditionalInformation(getString(R.string.eclass_signin_process_description),
              R.style.TextAppearance_AppTheme_Info, false);
        } else {
          setAdditionalInformation(getString(R.string.no_network_connection),
              R.style.TextAppearance_AppTheme_Error, true);

          setLoginFormEnabled(true);
          return;
        }

        Observable<Response<List<User>>> observable;
        if (!AndroidUtils.isNetworkAvailable(getContext())) {
          observable = Observable.empty();
        } else {
          observable = InhaEclassController.getInstance()
              .getWebService()
              .signIn(encode(userIdView), encode(userPasswordView), InhaEclassWebService.GR_CODE)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .compose(LoginEclassFragment.this.<Response<List<User>>>bindToLifecycle());
        }

        observable.filter(new Func1<Response<List<User>>, Boolean>() {
          @Override public Boolean call(Response<List<User>> users) {
            return users != null;
          }
        }).subscribe(new Action1<Response<List<User>>>() {
          @Override public void call(Response<List<User>> usersResponse) {
            final List<User> users = usersResponse.body();
            final String token = usersResponse.raw().header(InhaEclassWebService.KEY_TOKEN);

            if (users.isEmpty()) {
              setAdditionalInformation(getString(R.string.login_failed_user_not_found),
                  R.style.TextAppearance_AppTheme_Error, true);

              Timber.d("Users list is empty");
            } else {
              User user = users.get(0);
              Timber.d("Logged in as " + user.getName());

              if (user.isAuthenticated()) {
                onUserSignedIn(user, token);
              } else {
                Timber.e("User not found");

                setAdditionalInformation(getString(R.string.login_failed_user_not_found),
                    R.style.TextAppearance_AppTheme_Error, true);
              }

              setLoginFormEnabled(true);
            }
          }
        }, new Action1<Throwable>() {
          @Override public void call(Throwable e) {
            Timber.e(e, "Login failed");

            View view = getView();
            if (view == null) {
              return;
            }

            if (e instanceof IOException) {
              setLoginFormEnabled(true);

              showSnackbar(AndroidUtils.getExceptionDetailHumanReadableMessage(getContext(),
                  (IOException) e), Snackbar.LENGTH_LONG);
            } else {
              showSnackbar(e.getLocalizedMessage(), Snackbar.LENGTH_LONG);
            }
          }
        });
      }
    });
  }

  protected void showSnackbar(@NonNull CharSequence message, @Snackbar.Duration int length) {
    Snackbar snackbar = Snackbar.make((View) signInForm.getParent(), message, length);

    if (signInForm.getLayoutParams().width != MATCH_PARENT) {
      View snackBarLayout = snackbar.getView();
      ViewGroup parent = (ViewGroup) signInForm.getParent();
      FrameLayout.LayoutParams params =
          new FrameLayout.LayoutParams(parent.getWidth() - signInForm.getWidth(), WRAP_CONTENT);
      params.leftMargin = getResources().getDimensionPixelSize(R.dimen.snack_bar_margins);
      params.bottomMargin = params.rightMargin = params.leftMargin;
      params.width -= params.leftMargin * 2;
      params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
      params.leftMargin += signInForm.getWidth();

      snackBarLayout.setLayoutParams(params);
    } else {
      snackbar.setCallback(snackBarCallback);
    }

    snackbar.show();
  }

  protected Snackbar.Callback snackBarCallback = new Snackbar.Callback() {
    @Override public void onDismissed(Snackbar snackbar, int event) {
      if (event != Snackbar.Callback.DISMISS_EVENT_MANUAL) {
        snackBarQueue.remove(snackbar);
      }
    }

    @Override public void onShown(Snackbar snackbar) {
      snackBarQueue.push(snackbar);
    }
  };

  boolean onBackPressed() {
    if (snackBarQueue.isEmpty()) {
      return false;
    } else {
      snackBarQueue.pop().dismiss();
      return true;
    }
  }

  /**
   * Called after authentication was succeed and user was found
   * using account manager we need save account in the system
   *
   * @param user User found in IUT E-class
   */
  protected void onUserSignedIn(User user, String token) {
    setAdditionalInformation(getString(R.string.login_proceed_welcome_tpl) + user.getName(),
        R.style.TextAppearance_AppTheme_Info, false);

    InhaEclassController.getInstance().setCurrentUser(user);

    Account account = new Account(user.getUserId(), EclassAuthenticator.ECLASS_USER);

    AccountManager manager = AccountManager.get(getContext());
    if (manager.addAccountExplicitly(account, userPasswordView.getText().toString(),
        EclassAuthenticator.getUserData(user))) {

      manager.setAuthToken(account, EclassAuthenticator.ECLASS_USER, token);
    }

    Intent intent = new Intent();
    intent.putExtra(KEY_USER, user);

    Activity hostActivity = getActivity();
    hostActivity.setResult(Activity.RESULT_OK, intent);
    hostActivity.finish();
  }

  /**
   * Returns encoded text from view
   *
   * @param view View with text to encode
   * @return Encoded text from TextView
   */
  private String encode(TextView view) {
    return InhaSessionEncoder.encode(view.getText().toString());
  }

  protected void setAdditionalInformation(CharSequence text, @StyleRes int textApperance,
      boolean shake) {
    loginStatusView.setText(text);

    TextViewCompat.setTextAppearance(loginStatusView, textApperance);

    if (loginStatusView.getVisibility() != View.VISIBLE) {
      loginStatusView.setVisibility(View.VISIBLE);
    }

    if (!shake) {
      loginStatusView.setScaleY(0.3f);
      loginStatusView.setAlpha(0.3f);
      loginStatusView.animate()
          .scaleY(1f)
          .alpha(1f)
          .setDuration(300)
          .setInterpolator(new AccelerateDecelerateInterpolator())
          .start();
    } else {
      ObjectAnimator shakeAnimator =
          ObjectAnimator.ofFloat(loginStatusView, View.TRANSLATION_X, 0, 100, -100, 0);
      shakeAnimator.setInterpolator(new BounceInterpolator());
      shakeAnimator.setDuration(575);
      shakeAnimator.start();
    }
  }

  protected void setLoginFormEnabled(boolean enabled) {
    signInButton.setEnabled(enabled);
    userIdView.setEnabled(enabled);
    userPasswordView.setEnabled(enabled);

    if (!enabled) {
      if (progressBar.getVisibility() != View.VISIBLE) {
        progressBar.setVisibility(View.VISIBLE);
      }

      progressBar.setScaleY(0.3f);
      progressBar.setAlpha(0.3f);
      progressBar.animate()
          .scaleY(1f)
          .alpha(1f)
          .setDuration(500)
          .setInterpolator(new AccelerateDecelerateInterpolator())
          .withEndAction(null)
          .start();
    } else {
      progressBar.animate()
          .scaleY(0f)
          .alpha(0f)
          .setDuration(500)
          .setInterpolator(new AccelerateDecelerateInterpolator())
          .withEndAction(new Runnable() {
            @Override public void run() {
              progressBar.setVisibility(View.INVISIBLE);
            }
          })
          .start();
    }
  }
}
