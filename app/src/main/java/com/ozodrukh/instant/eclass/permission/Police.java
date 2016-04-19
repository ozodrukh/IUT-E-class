package com.ozodrukh.instant.eclass.permission;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.SparseArrayCompat;
import com.google.repacked.kotlin.NotImplementedError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static android.support.v4.content.PermissionChecker.checkCallingOrSelfPermission;

/**
 * Police - is application task queue for tasks, API is simplest as possible.
 *
 * Manual:
 * <code>
 * Using Police.with(Context applicationContext)
 *  .createRequest(Activity runningActivity)
 *  .requires(String... permissions)
 *  .withListener(Runnable grantAction)
 *  .withDenyListener(Runnable errorAction)
 *  .check();
 * </code>
 *
 * And in Your activity, it's better to create BaseActivity as root Activity
 * and call Police.with(Context).onRequestPermissionsResult(Context context, int taskId, String[]
 * permissions, int[] grantResults)
 *
 * That's all.
 */
public final class Police {

  private static final AtomicInteger idGen = new AtomicInteger(1100);

  private static Police instance;

  /**
   * Returns global instance Police
   *
   * @param context Application Context
   * @return {@link Police} global instance
   */
  public static Police with(Context context) {
    if (instance == null) {
      instance = new Police();
    }
    return instance;
  }

  /**
   * Register returned callback using
   * {@link Application#registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks)},
   * and use fluent API without passing {@link Activity} in argument
   *
   * @param context Application Context
   * @return {@link Application.ActivityLifecycleCallbacks} to register in your {@link Application}
   */
  public static Application.ActivityLifecycleCallbacks handleActivityCallback(Context context) {
    return with(context).activityManger;
  }

  private final SparseArrayCompat<PermissionRequiredTask> pendingTasks;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final InternalActivityManger activityManger;

  public Police() {
    this.activityManger = new InternalActivityManger();
    this.pendingTasks = new SparseArrayCompat<>();
  }

  /**
   * Useful only if {@link #handleActivityCallback(Context)} was registered in {@link Application}
   * class, creates fluent API
   *
   * @param permissions Required permissions list
   * @return Permission required task builder
   */
  public SimplePermissionTask.Builder withPermissions(String... permissions) {
    return createRequest(this.activityManger.getRunningActivity()).requires(permissions);
  }

  public SimplePermissionTask.Builder createRequest(Activity activity) {
    return new SimplePermissionTask.Builder(this, activity);
  }

  public void request(PermissionRequiredTask task, boolean skipRationalRequest) {
    if (activityManger.getRunningActivity() == null) {
      throw new NotImplementedError(
          "To use this method, first needs to register ActivityLifecycleCallbacks in your Application, "
              + "or might be no activity is running");
    }

    request(activityManger.getRunningActivity(), task, true, skipRationalRequest);
  }

  /**
   * Requests permission from Activity
   *
   * Normally it first checks if all permissions are already granted, if so
   * runs task immediately. Task running thread depends on <code>requireMainThread</code>
   * if flag is true, than it will be queued and runned on main-thread, otherwise invoked
   * immediately on current thread
   *
   * if <code>skipRationaleRequest</code> is enabled, it means that all permissions firstly
   * will be checked to {@link Activity#shouldShowRequestPermissionRationale(String)}, if any
   * of permissions requires rationale then {@link PermissionRequiredTask#showsRequestPermissionRationale(Activity,
   * List)} called, it's important to proceed request if you showing any ui tips by calling
   * this method one more time after your ui tip is finished
   *
   * @param activity Activity that requests permission
   * @param task Result handler
   * @param requireMainThread Requires to invoke task in main-thread?
   * @param skipRationaleRequest see {@link Activity#shouldShowRequestPermissionRationale(String)}
   */
  public void request(final Activity activity, final PermissionRequiredTask task,
      boolean requireMainThread, boolean skipRationaleRequest) {
    if (activity == null) {
      throw new NullPointerException(
          "if you using request() without Activity argument, promote you to register ActivityLifecycleCallbacks");
    } else if (activity.isFinishing()) { //TODO needs in finishing checking?
      return;
    }

    activityManger.setRunningActivity(activity);

    final boolean inMainThread = Thread.currentThread() == Looper.getMainLooper().getThread();

    final String[] requiredPermissions = new String[task.requiredPermissions().size()];
    task.requiredPermissions().toArray(requiredPermissions);

    if (PermissionsHelper.hasSelfPermissions(activity, requiredPermissions)) {
      final Permissions permissions =
          Permissions.createGranted(activity, task.requiredPermissions());

      if (requireMainThread && !inMainThread) {
        handler.post(new Runnable() {
          @Override public void run() {
            task.onPermissionsGranted(activityManger.getRunningActivity(), permissions);
          }
        });
      } else {
        task.onPermissionsGranted(activity, permissions);
      }
    } else {
      boolean selfProceed = true;
      if (!skipRationaleRequest) {
        final List<String> rationalPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
          if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            rationalPermissions.add(permission);
          }
        }

        if (!inMainThread) {
          handler.post(new Runnable() {
            @Override public void run() {
              final Activity a = activityManger.getRunningActivity();
              if (a != null && !task.showsRequestPermissionRationale(a, rationalPermissions)) {
                final int taskId = idGen.incrementAndGet();
                pendingTasks.put(taskId, task);

                ActivityCompat.requestPermissions(a, requiredPermissions, taskId);
              }
            }
          });
        }

        selfProceed =
            inMainThread && task.showsRequestPermissionRationale(activity, rationalPermissions);
      }

      if (selfProceed) {
        final int taskId = idGen.incrementAndGet();
        pendingTasks.put(taskId, task);

        ActivityCompat.requestPermissions(activity, requiredPermissions, taskId);
      }
    }
  }

  /**
   * Searches for Activity context from reference
   *
   * @param context Abstract context instance
   * @return Activity if reference is instance of this class
   */
  private static Activity getActivity(Context context) {
    if (context instanceof Activity) {
      return (Activity) context;
    } else if (context instanceof ContextWrapper) {
      return getActivity(((ContextWrapper) context).getBaseContext());
    } else if (context == context.getApplicationContext()) {
      throw new IllegalArgumentException(
          "Cannot request permission from Application context, requires activity context");
    } else {
      throw new RuntimeException("Activity context is required");
    }
  }

  public boolean onRequestPermissionsResult(Context context, int taskId,
      @NonNull String[] permissions, @NonNull int[] grantResults) {
    PermissionRequiredTask task = pendingTasks.get(taskId);
    if (task == null) {
      return false;
    }

    Permissions ps = Permissions.create(context, Arrays.asList(permissions), grantResults);
    if (ps.grantedPermissionsCount > 0) {
      task.onPermissionsGranted(context, ps);
    }

    if (ps.deniedPermissions.size() > 0) {
      task.onPermissionsDenied(context, ps);
    }

    pendingTasks.remove(taskId);
    return true;
  }

  public static class Permissions {
    List<String> permissions;
    List<String> deniedPermissions;
    int[] grantResults;
    int grantedPermissionsCount;

    Permissions(List<String> permissions) {
      this.permissions = Collections.unmodifiableList(permissions);
      this.deniedPermissions = Collections.emptyList();
    }

    static Permissions createGranted(Context context, List<String> requiredPermissions) {
      int[] grantResults = new int[requiredPermissions.size()];
      Arrays.fill(grantResults, PERMISSION_GRANTED);

      return create(context, requiredPermissions, grantResults);
    }

    static Permissions create(Context context, List<String> requiredPermissions,
        int[] grantResults) {
      Permissions permissions = new Permissions(requiredPermissions);
      permissions.setGrantResults(grantResults);
      permissions.ensurePermissionsDistributedCorrectly(context);
      return permissions;
    }

    /**
     * Recheck all internal permissions whether they are distributed
     * in lists correctly
     *
     * @param context Used Context instance
     */
    void ensurePermissionsDistributedCorrectly(Context context) {
      for (String permission : permissions) {
        if (checkCallingOrSelfPermission(context, permission) != PERMISSION_GRANTED) {
          if (deniedPermissions == Collections.EMPTY_LIST) {
            deniedPermissions = new ArrayList<>();
          }

          if (!deniedPermissions.contains(permission)) {
            deniedPermissions.add(permission);
            grantedPermissionsCount--;
          }
        } else if (deniedPermissions.contains(permission)) {
          deniedPermissions.remove(permission);
          grantedPermissionsCount++;
        }
      }
    }

    /**
     * Distributes internally granted results in correct lists
     *
     * @param grantResults Result of permission request
     */
    void setGrantResults(int[] grantResults) {
      for (int i = 0; i < grantResults.length; i++) {
        int result = grantResults[i];
        if (result != PERMISSION_GRANTED) {
          if (deniedPermissions == Collections.EMPTY_LIST) {
            deniedPermissions = new ArrayList<>();
          }
          deniedPermissions.add(permissions.get(i));
        } else {
          grantedPermissionsCount++;
        }
      }

      this.grantResults = grantResults;
    }

    /**
     * @return True if all permissions are granter, otherwise returns false
     */
    public boolean verifyPermissions() {
      return grantedPermissionsCount == permissions.size();
    }

    /**
     * @return List of denied permissions
     */
    public List<String> getDeniedPermissions() {
      return deniedPermissions;
    }

    /**
     * @param permission Android Manifest permission
     * @return True if referenced permissions was granted by user,
     * otherwise false
     */
    public boolean isGranted(String permission) {
      if (grantResults == null) {
        return false;
      } else if (!PermissionsHelper.permissionExists(permission)) {
        return false;
      } else {
        final int permissionIndex = permissions.indexOf(permission);
        if (permissionIndex >= 0) {
          return grantResults[permissionIndex] == PERMISSION_GRANTED;
        } else {
          throw new IllegalArgumentException("Permission was not listed in requiredPermissions()");
        }
      }
    }

    /**
     * @param permission Android Manifest permission
     * @return True if permission was denied, otherwise returns false
     * @see #isGranted(String)
     */
    public boolean isDenied(String permission) {
      if (permissions.contains(permission)) {
        return deniedPermissions.indexOf(permission) >= 0;
      } else {
        throw new IllegalArgumentException("Permission was not listed in requiredPermissions()");
      }
    }
  }
}
