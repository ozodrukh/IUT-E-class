package com.ozodrukh.eclass.entity;

import android.os.Parcel;
import android.os.Parcelable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Subject implements Parcelable{

  private String auth;
  private String grcode;
  private String subject;
  private String year;;
  private String sequence;
  private String classNumber;
  private String name;

  public Subject(String auth, String grcode, String subject, String year, String sequence,
      String classNumber, String name) {
    this.auth = auth;
    this.grcode = grcode;
    this.subject = subject;
    this.year = year;
    this.sequence = sequence;
    this.classNumber = classNumber;
    this.name = name;
  }

  public Subject() {
  }

  protected Subject(Parcel in) {
    auth = in.readString();
    grcode = in.readString();
    subject = in.readString();
    year = in.readString();
    sequence = in.readString();
    classNumber = in.readString();
    name = in.readString();
  }

  public static final Creator<Subject> CREATOR = new Creator<Subject>() {
    @Override public Subject createFromParcel(Parcel in) {
      return new Subject(in);
    }

    @Override public Subject[] newArray(int size) {
      return new Subject[size];
    }
  };

  public String getAuth() {
    return auth;
  }

  public void setAuth(String auth) {
    this.auth = auth;
  }

  public String getGrcode() {
    return grcode;
  }

  public void setGrcode(String grcode) {
    this.grcode = grcode;
  }

  public String getSubject() {
    return subject;
  }

  @JsonProperty("subj")
  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String   getSequence() {
    return sequence;
  }

  @JsonProperty("subjseq")
  public void setSequence(String sequence) {
    this.sequence = sequence;
  }

  @JsonProperty("subjclass")
  public String getClassNumber() {
    return classNumber;
  }

  public void setClassNumber(String classNumber) {
    this.classNumber = classNumber;
  }

  @JsonProperty("subjnm")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Subject{" +
      "auth='" + auth + '\'' +
      ", grcode='" + grcode + '\'' +
      ", subject='" + subject + '\'' +
      ", year='" + year + '\'' +
      ", sequence=" + sequence +
      ", classNumber='" + classNumber + '\'' +
      ", name='" + name + '\'' +
      '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(auth);
    dest.writeString(grcode);
    dest.writeString(subject);
    dest.writeString(year);
    dest.writeString(sequence);
    dest.writeString(classNumber);
    dest.writeString(name);
  }
}
