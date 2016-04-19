package com.ozodrukh.instant.eclass.permission;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import java.util.List;

interface PermissionRequiredTask {

  /**
   * Called if only at least one permission is granted amon
   * listed permissions
   *
   * Method invokes on {@link Looper#getMainLooper()} e.g main-thread
   * only if result received in {@link Activity#onRequestPermissionsResult(int, String[], int[])}
   *
   * @param permissions Result of user request
   */
  void onPermissionsGranted(Context context, Police.Permissions permissions);

  /**
   * Called if only at least one permission is denied among
   * listed permissions
   *
   * Method invokes on {@link Looper#getMainLooper()} e.g main-thread
   * only if result received in {@link Activity#onRequestPermissionsResult(int, String[], int[])}
   *
   * @param permissions Result of user request
   */
  void onPermissionsDenied(Context context, Police.Permissions permissions);

  /**
   * TODO undocumented
   */
  boolean showsRequestPermissionRationale(Activity activity, List<String> permissions);

  /**
   * @return Required permissions list
   */
  List<String> requiredPermissions();

  /**
   * Empty body abstract implementation
   */
  abstract class Simple implements PermissionRequiredTask {

    @Override public void onPermissionsGranted(Context context, Police.Permissions permissions) {
    }

    @Override public void onPermissionsDenied(Context context, Police.Permissions permissions) {
    }

    @Override public List<String> requiredPermissions() {
      return null;
    }

    @Override
    public boolean showsRequestPermissionRationale(Activity activity, List<String> permissions) {
      return false;
    }
  }
}