package com.ozodrukh.eclass;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

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
  public static CookieJar provideCookiesManager(List<Cookie> persistent){
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

  /**
   * Determine whether the characters is a number. Numbers are of the
   * form -12.34e+56. Fractional and exponential parts are optional. Leading
   * zeroes are not allowed in the value or exponential part, but are allowed
   * in the fraction.
   */
  public static Double decodeNumber(String value, int offset, int length) {
    char[] chars = value.toCharArray();
    int i = offset;
    int c = chars[i];

    if (c == '-') {
      c = chars[++i];
    }

    if (c == '0') {
      c = chars[++i];
    } else if (c >= '1' && c <= '9') {
      c = chars[++i];
      while (c >= '0' && c <= '9') {
        c = chars[++i];
      }
    } else {
      return Double.NaN;
    }

    if (c == '.') {
      c = chars[++i];
      while (c >= '0' && c <= '9') {
        c = chars[++i];
      }
    }

    if (c == 'e' || c == 'E') {
      c = chars[++i];
      if (c == '+' || c == '-') {
        c = chars[++i];
      }
      if (c >= '0' && c <= '9') {
        c = chars[++i];
        while (c >= '0' && c <= '9') {
          c = chars[++i];
        }
      } else {
        return Double.NaN;
      }
    }

    if (i == offset + length) {
      return Double.parseDouble(value.substring(offset, offset + length));
    } else {
      return Double.NaN;
    }
  }
}
