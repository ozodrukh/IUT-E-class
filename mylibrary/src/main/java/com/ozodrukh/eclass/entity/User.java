package com.ozodrukh.eclass.entity;

import android.os.Parcel;
import android.os.Parcelable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User implements Parcelable{

  private boolean success;

  private String grCode;
  private String userId;
  private String name;
  private String password;

  public User(boolean success, String grCode, String userId, String name, String password) {
    this.success = success;
    this.grCode = grCode;
    this.userId = userId;
    this.name = name;
    this.password = password;
  }

  public User() {
  }

  protected User(Parcel in) {
    success = in.readByte() != 0;
    grCode = in.readString();
    userId = in.readString();
    name = in.readString();
    password = in.readString();
  }

  public static final Creator<User> CREATOR = new Creator<User>() {
    @Override public User createFromParcel(Parcel in) {
      return new User(in);
    }

    @Override public User[] newArray(int size) {
      return new User[size];
    }
  };

  public boolean isAuthenticated() {
    return success;
  }

  /* package local */void setIsFlag(int flag){
    success = flag == 1;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getUserId() {
    return userId;
  }

  @JsonProperty(value = "userid")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getGrCode() {
    return grCode;
  }

  @JsonProperty("grcode")
  public void setGrCode(String grCode) {
    this.grCode = grCode;
  }

  @Override
  public String toString() {
    return "User{" +
      "success=" + success +
      ", userId='" + userId + '\'' +
      ", name='" + name + '\'' +
      ", password='" + password + '\'' +
      '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeByte((byte) (success ? 1 : 0));
    dest.writeString(grCode);
    dest.writeString(userId);
    dest.writeString(name);
    dest.writeString(password);
  }
}
