package com.ozodrukh.instant.eclass.permission;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import java.lang.ref.WeakReference;

final class InternalActivityManger implements Application.ActivityLifecycleCallbacks {

  private WeakReference<Activity> runningActivity;

  public Activity getRunningActivity() {
    return runningActivity == null ? null : runningActivity.get();
  }

  public void setRunningActivity(Activity activity) {
    if (runningActivity == null || runningActivity.get() != activity) {
      this.runningActivity = new WeakReference<>(activity);
    }
  }

  @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

  }

  @Override public void onActivityStarted(Activity activity) {

  }

  @Override public void onActivityResumed(Activity activity) {
    setRunningActivity(activity);
  }

  @Override public void onActivityPaused(Activity activity) {
    if (runningActivity != null) {
      runningActivity.clear();
    }
  }

  @Override public void onActivityStopped(Activity activity) {

  }

  @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

  }

  @Override public void onActivityDestroyed(Activity activity) {

  }
}
