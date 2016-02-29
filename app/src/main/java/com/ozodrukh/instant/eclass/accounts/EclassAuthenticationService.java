package com.ozodrukh.instant.eclass.accounts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class EclassAuthenticationService extends Service{

  private EclassAuthenticator eclassAuthenticator;

  @Override public void onCreate() {
    super.onCreate();
    eclassAuthenticator = new EclassAuthenticator(this);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return eclassAuthenticator.getIBinder();
  }
}
