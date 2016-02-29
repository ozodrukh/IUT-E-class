package com.ozodrukh.instant.eclass;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.ozodrukh.eclass.InhaEclassController;
import com.ozodrukh.eclass.Timber;
import com.ozodrukh.eclass.entity.User;
import com.ozodrukh.instant.eclass.accounts.EclassAuthenticator;
import com.ozodrukh.instant.eclass.utils.Utils;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import java.io.IOException;
import java.util.List;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.ozodrukh.eclass.InhaSessionEncoder.encode;

public class MainActivity extends RxAppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  private NavigationView navigationView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
    drawer.setDrawerListener(toggle);
    toggle.syncState();

    navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    checkUserLoggedIn();
  }

  protected void checkUserLoggedIn() {
    Account[] eclassAccounts = EclassAuthenticator.getAccounts();
    LoginEclassFragment fragment =
        (LoginEclassFragment) getSupportFragmentManager().findFragmentByTag(
            LoginEclassFragment.TAG);

    if (eclassAccounts.length == 0) {
      if (fragment == null || !fragment.isAdded()) {
        getSupportFragmentManager().beginTransaction()
            .add(R.id.app_content_view, new LoginEclassFragment(), LoginEclassFragment.TAG)
            .addToBackStack(LoginEclassFragment.TAG)
            .commit();
      }
    } else {
      AccountManager am = AccountManager.get(this);
      //TODO multi account feature
      Account primaryAccount = eclassAccounts[0];
      User primaryUser = EclassAuthenticator.loadUser(am, primaryAccount);

      InhaEclassController controller = InhaEclassController.getInstance();
      controller.setCurrentUser(primaryUser);
      controller.getWebService()
          .signIn(encode(primaryUser.getUserId()), encode(am.getPassword(primaryAccount)),
              primaryUser.getGrCode())
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .compose(this.<Response<List<User>>>bindToLifecycle())
          .subscribe(new Action1<Response<List<User>>>() {
            @Override public void call(Response<List<User>> listResponse) {
              startAssignmentsReportsFragment();
            }
          }, new Action1<Throwable>() {
            @Override public void call(Throwable throwable) {
              Timber.e(throwable, "Couldn't update User session");

              if(throwable instanceof IOException){
                CharSequence errorMsg = Utils.getExceptionDetailHumanReadableMessage(MainActivity.this,
                    (IOException) throwable);

                Snackbar.make(findViewById(android.R.id.content), errorMsg, Snackbar.LENGTH_LONG)
                    .show();
              }
            }
          });
    }
  }

  public AssignmentsReportFragment startAssignmentsReportsFragment() {
    LoginEclassFragment loginFragment =
        (LoginEclassFragment) getSupportFragmentManager().findFragmentByTag(
            LoginEclassFragment.TAG);

    FragmentTransaction transaction = null;

    if (loginFragment != null && loginFragment.isAdded()) {
      transaction = getSupportFragmentManager().beginTransaction();

      transaction.hide(loginFragment);
    }

    AssignmentsReportFragment reportFragment =
        (AssignmentsReportFragment) getSupportFragmentManager().findFragmentByTag(
            AssignmentsReportFragment.TAG);

    if (reportFragment == null) {
      if (transaction == null) {
        transaction = getSupportFragmentManager().beginTransaction();
      }
      reportFragment = new AssignmentsReportFragment();
      transaction.add(R.id.app_content_view, reportFragment, AssignmentsReportFragment.TAG)
          .addToBackStack(AssignmentsReportFragment.TAG)
          .commit();
    }

    navigationView.getMenu()
        .findItem(R.id.assignment_history)
        .setChecked(true);

    return reportFragment;
  }

  @Override public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    // getMenuInflater().inflate(R.menu.main, menu);
    return false;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody") @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.assignment_history) {
      startAssignmentsReportsFragment();
    } else if (id == R.id.nav_gallery) {

    } else if (id == R.id.nav_slideshow) {

    } else if (id == R.id.nav_manage) {

    } else if (id == R.id.nav_share) {

    } else if (id == R.id.logout) {
      Account[] eclassAccounts = EclassAuthenticator.getAccounts();

      AccountManager am = AccountManager.get(this);
      if (Build.VERSION.SDK_INT >= 23) {
        am.removeAccount(eclassAccounts[0], this, new AccountManagerCallback<Bundle>() {
          @Override public void run(AccountManagerFuture<Bundle> future) {
            dispatchUserLogout();
          }
        }, null);
      } else {
        am.removeAccount(eclassAccounts[0], new AccountManagerCallback<Boolean>() {
          @Override public void run(AccountManagerFuture<Boolean> future) {
            dispatchUserLogout();
          }
        }, null);
      }
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  protected void dispatchUserLogout() {
    final FragmentManager fm = getSupportFragmentManager();
    final int backStackEntryCount = fm.getBackStackEntryCount();
    for (int i = 0; i < backStackEntryCount; i++) {
      fm.popBackStack(fm.getBackStackEntryAt(i).getName(), 0);
    }

    checkUserLoggedIn();
  }
}
