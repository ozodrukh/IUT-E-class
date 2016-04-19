package com.ozodrukh.instant.eclass;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.ozodrukh.eclass.InhaEclassController;
import com.ozodrukh.eclass.entity.SubjectReport;
import com.ozodrukh.eclass.entity.User;
import com.ozodrukh.instant.eclass.accounts.AuthenticationActivity;
import com.ozodrukh.instant.eclass.accounts.EclassAuthenticator;
import com.ozodrukh.instant.eclass.accounts.LoginEclassFragment;
import com.ozodrukh.instant.eclass.assignments.AssignmentsReportFragment;
import com.ozodrukh.instant.eclass.assignments.AttachmentDownloaderService;
import com.ozodrukh.instant.eclass.permission.Police;
import com.ozodrukh.instant.eclass.utils.RxUtils;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import java.io.File;
import java.util.List;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.ozodrukh.eclass.InhaSessionEncoder.encode;

@SuppressWarnings("ConstantConditions") @RuntimePermissions public class MainActivity
    extends RxAppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

  private NavigationView navigationView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    checkUserLoggedIn(savedInstanceState == null);
  }

  @Override protected void onResume() {
    super.onResume();

    checkUserLoggedIn(false);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    Police.with(this).onRequestPermissionsResult(this, requestCode, permissions, grantResults);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == AuthenticationActivity.REQUEST_ID) {
      if (resultCode == RESULT_OK) {
        User user = data.getParcelableExtra(LoginEclassFragment.KEY_USER);
        setPrimaryUser(user);
      } else if (resultCode == RESULT_CANCELED) {
        finish();
      }
    }
  }

  @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
  public void dispatchHandleAttachmentLink(SubjectReport report) {
    File baseDir =
        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "E-class");

    if (!baseDir.exists() && !baseDir.mkdirs()) {
      throw new RuntimeException("No access to storage");
    }

    File destination = new File(baseDir, report.getName());

    Intent intent = new Intent(this, AttachmentDownloaderService.class);
    intent.putExtra(AttachmentDownloaderService.EXTRA_ASSIGNMENT_SUBJECT, report);
    intent.putExtra(AttachmentDownloaderService.EXTRA_DESTINATION, destination.getPath());
    this.startService(intent);
  }

  /**
   * On primary user was determined application will configure all details
   * depends on previously configuration for this user
   *
   * TODO - avatar, email in navigation drawer?
   * TODO - user configurations
   *
   * @param user User that is primary for current session
   */
  protected void setPrimaryUser(User user) {
    View userBanner = navigationView.getHeaderView(0);

    TextView nameView = (TextView) userBanner.findViewById(R.id.user_name);
    nameView.setText(user.getUserId());

    InhaEclassController.getInstance().setCurrentUser(user);
  }

  /**
   * Checks whether already has accounts in application, if no system requires
   * log in in order to use Application therefore we launching {@link AuthenticationActivity}
   * otherwise we loading assignments history
   */
  protected void checkUserLoggedIn(boolean refershToken) {
    Account[] eclassAccounts = EclassAuthenticator.getAccounts();

    if (eclassAccounts.length == 0) {
      // REQUEST User Log in
      startActivityForResult(new Intent(this, AuthenticationActivity.class),
          AuthenticationActivity.REQUEST_ID);
    } else if (refershToken) {
      AccountManager am = AccountManager.get(this);
      //TODO multi account feature
      Account primaryAccount = eclassAccounts[0];
      User primaryUser = EclassAuthenticator.loadUser(am, primaryAccount);

      setPrimaryUser(primaryUser);

      // As we know E-class currently drops all session in next 2-3 hours
      // therefore we need always sign in again

      InhaEclassController.getInstance()
          .getWebService()
          .signIn(encode(primaryUser.getUserId()), encode(am.getPassword(primaryAccount)),
              primaryUser.getGrCode())
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .compose(this.<Response<List<User>>>bindToLifecycle())
          .subscribe(new Action1<Response<List<User>>>() {
            @Override public void call(Response<List<User>> listResponse) {
              startAssignmentsReportsFragment();
            }
          }, RxUtils.handleIOExceptions(this));
    }
  }

  public AssignmentsReportFragment startAssignmentsReportsFragment() {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    AssignmentsReportFragment reportFragment =
        (AssignmentsReportFragment) getSupportFragmentManager().findFragmentByTag(
            AssignmentsReportFragment.TAG);

    if (reportFragment == null) {
      reportFragment = new AssignmentsReportFragment();
      transaction.add(R.id.app_content_view, reportFragment, AssignmentsReportFragment.TAG)
          .commit();
    } else {
      reportFragment.requestRecreate();
    }

    navigationView.getMenu().findItem(R.id.assignment_history).setChecked(true);
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
            recreate();
          }
        }, null);
      } else {
        am.removeAccount(eclassAccounts[0], new AccountManagerCallback<Boolean>() {
          @Override public void run(AccountManagerFuture<Boolean> future) {
            recreate();
          }
        }, null);
      }
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }
}
