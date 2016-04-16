package com.ozodrukh.instant.eclass.accounts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

public class AuthenticationActivity extends RxAppCompatActivity {

  /** Identification number for {@link Activity#startActivityForResult(Intent, int)} */
  public static final int REQUEST_ID = 1;

  private LoginEclassFragment loginFragment;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getSupportFragmentManager().beginTransaction()
        .add(android.R.id.content, loginFragment = new LoginEclassFragment(),
            LoginEclassFragment.TAG)
        .commit();
  }

  @Override protected void onStop() {
    super.onStop();
    loginFragment = null;
  }

  @Override public void onBackPressed() {
    if(loginFragment == null || !loginFragment.onBackPressed()) {
      setResult(RESULT_CANCELED);
      super.onBackPressed();
    }
  }
}
