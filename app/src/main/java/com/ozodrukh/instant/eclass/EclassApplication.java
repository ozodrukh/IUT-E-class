package com.ozodrukh.instant.eclass;

import android.app.Application;
import com.ozodrukh.eclass.Timber;
import com.ozodrukh.instant.eclass.utils.AndroidUtils;
import com.yandex.metrica.YandexMetrica;

public class EclassApplication extends Application{

  public static EclassApplication instance;

  public static EclassApplication getContext() {
    return instance;
  }

  @Override public void onCreate() {
    super.onCreate();
    instance = this;

    Timber.plant(new AndroidUtils.AndroidDebugTree());

    // Initializing the AppMetrica SDK
    YandexMetrica.activate(getApplicationContext(), "");
    // Tracking user activity
    YandexMetrica.enableActivityAutoTracking(this);
  }
}
