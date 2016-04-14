package com.ozodrukh.eclass;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozodrukh.eclass.entity.User;
import com.ozodrukh.eclass.json.JsObjectConverterFactory;
import java.util.Collections;
import okhttp3.Cookie;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Global controller of E-class system to work with their API with internal right configurations
 */
public class InhaEclassController {

  private static InhaEclassController instance;

  public static void setInstance(InhaEclassController instance) {
    InhaEclassController.instance = instance;
  }

  /**
   * @return Global instance of Inha E-class Controller
   */
  public static InhaEclassController getInstance() {
    if (instance == null) {
      instance = new InhaEclassController(null);
    }
    return instance;
  }

  private User currentUser;
  private OkHttpClient okHttpClient;
  private InhaEclassWebService webService;

  public InhaEclassController(String token) {
    // if token is available add token cookie in every header
    Cookie jSession = token == null ? null
        : new Cookie.Builder().name(InhaEclassWebService.KEY_TOKEN)
            .domain(InhaEclassWebService.ENDPOINT).path("/").value(token).build();

    OkHttpClient client = okHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(
        new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
      /* Allow further api calls requires JSESSION_ID token in cookie */
        .cookieJar(Utils.provideCookiesManager(
            jSession == null ? Collections.<Cookie>emptyList() : Collections.singletonList(jSession)))
        .build();

    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    this.webService = new Retrofit.Builder().baseUrl(InhaEclassWebService.ENDPOINT)
        // Put it first so the next converter will receive proper json value instead of jsx
        .addConverterFactory(new JsObjectConverterFactory())
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .callFactory(client)
        .build()
        .create(InhaEclassWebService.class);
  }

  public OkHttpClient getOkHttpClient() {
    return okHttpClient;
  }

  /**
   * Set current logged in user as global variable
   *
   * @param currentUser Logged in User
   */
  public void setCurrentUser(User currentUser) {
    this.currentUser = currentUser;
  }

  /** @return Current E-class logged in user */
  public User getCurrentUser() {
    return currentUser;
  }

  /** @return E-class API service */
  public InhaEclassWebService getWebService() {
    return webService;
  }
}
