package com.ozodrukh.instant.eclass.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.ozodrukh.eclass.Timber;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AndroidUtils {

  /**
   * Checks whether mobile device connected to the network
   *
   * @param context Activity context
   * @return True if has active network and it is connected and has network
   * otherwise returns False
   */
  public static boolean isNetworkAvailable(Context context){
    ConnectivityManager manager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo currentNetworkInfo = manager.getActiveNetworkInfo();
    return currentNetworkInfo != null && currentNetworkInfo.isConnected();
  }

  /**
   * @return Active network info that device connected to
   * @see ConnectivityManager#getActiveNetwork()
   */
  public static NetworkInfo getActiveNetwork(Context context){
    ConnectivityManager manager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return manager.getActiveNetworkInfo();
  }

  public static CharSequence getExceptionDetailHumanReadableMessage(Context context, IOException e){
    NetworkInfo networkInfo = AndroidUtils.getActiveNetwork(context);
    StringBuilder troubleCauseName = new StringBuilder();

    if(networkInfo == null){
      troubleCauseName.append("Device is not connected to network");
    }else if(e instanceof SocketTimeoutException){
      troubleCauseName.append("Seems like network(")
          .append(networkInfo.getTypeName().toLowerCase())
          .append(") has high latency (poor condition)");
    }else if(e instanceof UnknownHostException){
      troubleCauseName.append("Looks like website is down, try some later");
    }

    return troubleCauseName.toString();
  }

  public static class AndroidDebugTree extends Timber.Tree{
    private static final int MAX_LOG_LENGTH = 4000;
    private static final int CALL_STACK_INDEX = 5;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");

    /**
     * Extract the tag which should be used for the message from the {@code element}. By default
     * this will use the class name without any anonymous class suffixes (e.g., {@code Foo$1}
     * becomes {@code Foo}).
     * <p>
     * Note: This will not be called if a {@linkplain Timber.Tree#tag(String) manual tag} was specified.
     */
    protected String createStackElementTag(StackTraceElement element) {
      String tag = element.getClassName();
      Matcher m = ANONYMOUS_CLASS.matcher(tag);
      if (m.find()) {
        tag = m.replaceAll("");
      }
      return tag.substring(tag.lastIndexOf('.') + 1);
    }

    @Override public String getTag() {
      String tag = super.getTag();
      if (tag != null) {
        return tag;
      }

      // DO NOT switch this to Thread.getCurrentThread().getStackTrace(). The test will pass
      // because Robolectric runs them on the JVM but on Android the elements are different.
      StackTraceElement[] stackTrace = new Throwable().getStackTrace();
      if (stackTrace.length <= CALL_STACK_INDEX) {
        throw new IllegalStateException(
            "Synthetic stacktrace didn't have enough elements: are you using proguard?");
      }
      return createStackElementTag(stackTrace[CALL_STACK_INDEX]);
    }

    @Override protected void log(int priority, String tag, String message, Throwable t) {
      if (message.length() < MAX_LOG_LENGTH) {
        if (priority == Log.ASSERT) {
          Log.wtf(tag, message);
        } else {
          Log.println(priority, tag, message);
        }
        return;
      }

      // Split by line, then ensure each line can fit into Log's maximum length.
      for (int i = 0, length = message.length(); i < length; i++) {
        int newline = message.indexOf('\n', i);
        newline = newline != -1 ? newline : length;
        do {
          int end = Math.min(newline, i + MAX_LOG_LENGTH);
          String part = message.substring(i, end);
          if (priority == Log.ASSERT) {
            Log.wtf(tag, part);
          } else {
            Log.println(priority, tag, part);
          }
          i = end;
        } while (i < newline);
      }
    }
  }
}
