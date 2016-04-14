package com.ozodrukh.instant.eclass.utils;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.widget.Toast;
import com.ozodrukh.eclass.Timber;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import rx.functions.Action1;

public class RxUtils {

  /**
   * Handles {@link IOException} caused by phone-in-offline-mode or {@link SocketTimeoutException}
   * because of slow connection and shows related messages to user
   */
  public static Action1<Throwable> handleIOExceptions(Context context) {
    final WeakReference<Context> contextRef = new WeakReference<>(context);

    return new Action1<Throwable>() {
      @Override public void call(Throwable throwable) {
        Timber.e(throwable, "Couldn't update User session");

        if (throwable instanceof IOException && contextRef.get() != null) {
          Context context = contextRef.get();

          CharSequence errorMsg =
              AndroidUtils.getExceptionDetailHumanReadableMessage(context, (IOException) throwable);

          if (context instanceof Activity) {
            Snackbar.make(((Activity) context).findViewById(android.R.id.content), errorMsg,
                Snackbar.LENGTH_LONG).show();
          } else {
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
          }
        }
      }
    };
  }


}
