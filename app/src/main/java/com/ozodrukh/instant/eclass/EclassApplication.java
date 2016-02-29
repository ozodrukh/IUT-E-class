package com.ozodrukh.instant.eclass;

import android.app.Application;
import com.ozodrukh.eclass.Timber;
import com.ozodrukh.instant.eclass.utils.Utils;
import com.yandex.metrica.YandexMetrica;

public class EclassApplication extends Application{

  public static EclassApplication instance;

  public static EclassApplication getContext() {
    return instance;
  }

  @Override public void onCreate() {
    super.onCreate();
    instance = this;

    Timber.plant(new Utils.AndroidDebugTree());

    // Initializing the AppMetrica SDK
    YandexMetrica.activate(getApplicationContext(), "0a0859b6-6bf3-4c25-82cd-1cbae54c0023");
    // Tracking user activity
    YandexMetrica.enableActivityAutoTracking(this);
  }
}
