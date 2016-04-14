package com.ozodrukh.eclass;

import com.ozodrukh.eclass.entity.Subject;
import com.ozodrukh.eclass.entity.User;
import com.ozodrukh.eclass.guava.OptionsBuilder;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * API blueprint file
 */
public interface InhaEclassWebService {
  String GR_CODE = "N000001";
  String KEY_TOKEN = "JSESSIONID";
  String ENDPOINT = "http://eclass.inha.uz";

  @FormUrlEncoded @POST("jsp/include/ajax.login.jsp") Observable<Response<List<User>>> signIn(
      @Field("p_userid") String userId, @Field("p_passwd") String password,
      @Field("p_grcode") String grcode);

  @FormUrlEncoded @POST("jsp/include/ajax.subj2.jsp") Observable<List<Subject>> getSubjects(
      @FieldMap Map<String, String> fields);

  @FormUrlEncoded @POST("servlet/controller.learn.ReportStuServlet")
  Observable<Response<ResponseBody>> getAssignmentsReport(@FieldMap Map<String, String> fields);

  /**
   * Utilities for sending options for E-class system with their default values
   */
  final class Utils {
    /**
     * Configured POST form to get subjects list
     *
     * @param user Logged in user
     * @return Subjects for logged in user
     */
    public static Map<String, String> getSubjectsOptions(User user) {
      return new OptionsBuilder<String, String>().put("p_grcode", user.getGrCode())
          .put("p_systemgubun", "3")
          .put("p_levels", "1")
          .put("p_language", "ENG")
          .put("p_userid", user.getUserId())
          .put("p_name", user.getName())
          .put("p_auth", "S")
          .put("p_comp", "0001")
          .put("p_ip", "196.168.1.12")
          .put("p_mtype", "UA")
          .put("p_passwdcheck", "N")
          .put("p_language_", "ENG")
          .build();
    }

    /**
     * Configures POST form to open certain subject
     *
     * @param user Logged in user
     * @param subject Selected subject to open
     * @return Configured post form to open referenced subject
     */
    public static Map<String, String> getAssignmentsOptions(User user, int page, Subject subject) {
      return getReportOptions("listRecordReport", page, user, subject);
    }

    public static Map<String, String> getReportOptions(String method, int page, User user,
        Subject subject) {
      OptionsBuilder<String, String> builder =
          new OptionsBuilder<String, String>().put("p_process", method)
              .put("p_pageno", String.valueOf(page))
              .put("p_reptype", "P")
              .put("p_isreport", "Y");

      if (subject != null) {
        builder.put("p_grcode", user.getGrCode())
            .put("p_subj", subject.getSubject())
            .put("p_class", subject.getClassNumber())
            .put("p_year", subject.getYear())
            .put("p_subjseq", subject.getSequence());
      }
      return builder.build();
    }
  }
}
