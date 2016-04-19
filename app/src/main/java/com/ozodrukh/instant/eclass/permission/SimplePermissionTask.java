package com.ozodrukh.instant.eclass.permission;

import android.app.Activity;
import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimplePermissionTask implements PermissionRequiredTask {

  private final List<String> permissions;
  private final Runnable grantRunnable;
  private final Runnable denyRunnable;

  private SimplePermissionTask(List<String> permissions, Runnable grantRunnable,
      Runnable denyRunnable) {
    this.permissions = permissions;
    this.grantRunnable = grantRunnable;
    this.denyRunnable = denyRunnable;
  }

  @Override public void onPermissionsGranted(Context context, Police.Permissions permissions) {
    grantRunnable.run();
  }

  @Override public void onPermissionsDenied(Context context, Police.Permissions permissions) {
    if (denyRunnable != null) {
      denyRunnable.run();
    }
  }

  @Override
  public boolean showsRequestPermissionRationale(Activity activity, List<String> permissions) {
    return false;
  }

  @Override public List<String> requiredPermissions() {
    return permissions;
  }

  public static final class Builder {
    private Police police;
    private WeakReference<Activity> activityRef;

    private List<String> permissions = new ArrayList<>();
    private Runnable grantRunnable;
    private Runnable denyRunnable;
    private boolean requiredMainThread;
    private boolean checkPermissionsRationale;

    public Builder(Police police, Activity activity) {
      this.police = police;
      this.activityRef = new WeakReference<>(activity);
      this.checkPermissionsRationale = true;
    }

    public Builder requires(String... permissions) {
      this.permissions.addAll(Arrays.asList(permissions));
      return this;
    }

    public Builder withListener(Runnable runnable) {
      this.grantRunnable = runnable;
      return this;
    }

    public Builder withDenyListener(Runnable runnable) {
      this.denyRunnable = runnable;
      return this;
    }

    public Builder requiresMainThread(){
      this.requiredMainThread = true;
      return this;
    }

    public Builder checkPermissionsRationale(boolean check){
      this.checkPermissionsRationale = check;
      return this;
    }

    SimplePermissionTask build() {
      if (permissions.isEmpty()) {
        throw new RuntimeException("At least 1 permission must be listed");
      }

      if (grantRunnable == null) {
        throw new RuntimeException(
            "Grant action must not be ignored, otherwise why do you request permission?");
      }

      return new SimplePermissionTask(permissions, grantRunnable, denyRunnable);
    }

    public void check() {
      if (activityRef.get() == null) {
        return;
      }
      police.request(activityRef.get(), build(), requiredMainThread, checkPermissionsRationale);
    }
  }
}
