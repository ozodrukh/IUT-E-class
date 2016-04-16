package com.ozodrukh.instant.eclass.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import java.util.List;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LoginEclassFragment extends BaseFragment {

  /** Provides access to signed user when he was authenticated in Application */
  public static final String KEY_USER = "arg:user";

  public static final String TAG = "fragment:sign-in";

  private View signInButton;
  private EditText userIdView;
  private EditText userPasswordView;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.eclass_login_fragmnet, container, false);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    userIdView = (EditText) view.findViewById(R.id.user_id);
    userPasswordView = (EditText) view.findViewById(R.id.password);
    signInButton = view.findViewById(R.id.signIn);

    signInButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Snackbar.make(v, String.format("Sign in process username = %s, password = %s, online = %s",
            userIdView.getText(), userPasswordView.getText(),
            AndroidUtils.isNetworkAvailable(getContext())), Snackbar.LENGTH_LONG).show();

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

            View view = getView();

            if (users.isEmpty()) {
              Timber.d("Users list is empty");
            } else {
              User user = users.get(0);
              Timber.d("Logged in as " + user.getName());

              if (view != null) {
                Snackbar.make(view, "Logged in as " + user.getName(), Snackbar.LENGTH_LONG).show();
              }

              if (user.isAuthenticated()) {
                onUserSignedIn(user, token);
              } else {
                Timber.e("User not found");

                if (view != null) {
                  Snackbar.make(view, "User not found", Snackbar.LENGTH_LONG).show();
                }
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

              Snackbar.make(view, AndroidUtils.getExceptionDetailHumanReadableMessage(getContext(),
                  (IOException) e), Snackbar.LENGTH_LONG).show();
            } else {
              Snackbar.make(view, e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
          }
        });
      }
    });
  }

  /**
   * Called after authentication was succeed and user was found
   * using account manager we need save account in the system
   *
   * @param user User found in IUT E-class
   */
  protected void onUserSignedIn(User user, String token) {
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

  protected void setLoginFormEnabled(boolean enabled) {
    signInButton.setEnabled(enabled);
    userIdView.setEnabled(enabled);
    userPasswordView.setEnabled(enabled);
  }
}
