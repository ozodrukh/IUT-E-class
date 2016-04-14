package com.ozodrukh.instant.eclass.accounts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

public class AuthenticationActivity extends RxAppCompatActivity {

  /** Identification number for {@link Activity#startActivityForResult(Intent, int)} */
  public static final int REQUEST_ID = 1;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getSupportFragmentManager().beginTransaction()
        .add(android.R.id.content, new LoginEclassFragment(), LoginEclassFragment.TAG)
        .commit();
  }

}
