package com.ozodrukh.eclass;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;

final class Utils {

  static String pathSegment(HttpUrl url, int segment){
    List<String> path = url.pathSegments();
    if(path != null && segment >= 0 && !path.isEmpty() && path.size() > segment){
      return path.get(segment);
    }
    return "";
  }

  static String lastPathSegment(HttpUrl url){
    List<String> path = url.pathSegments();
    if(path != null && !path.isEmpty()){
      return path.get(path.size() - 1);
    }
    return "";
  }

  /**
   * Simple cookie manager implementation
   * */
  static CookieJar provideCookiesManager(List<Cookie> persistent){
    return new SimpleCookieJar(persistent);
  }

  static class SimpleCookieJar implements CookieJar {
    private List<Cookie> cookies = new ArrayList<>();

    public SimpleCookieJar() {}

    public SimpleCookieJar(List<Cookie> persistent) {
      this.cookies.addAll(persistent);
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
      this.cookies.addAll(cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
      List<Cookie> cookies = new ArrayList<>();
      for(Cookie cookie: this.cookies){
        if(cookie.matches(url)){
          cookies.add(cookie);
        }
      }
      return cookies;
    }
  }
}
