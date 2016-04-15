package com.ozodrukh.eclass.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.Date;

public class Assignment implements Parcelable {
  private int week;
  private int lesson;
  private String assignmentTitle;
  private Date fromDate;
  private Date tillDate;
  private Date submissionDate;

  private double maxScore;
  private double minScore;
  private double totalScore;
  private String totalScoreText;

  public Assignment() {
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(week);
    dest.writeInt(lesson);
    dest.writeString(assignmentTitle);
    dest.writeDouble(maxScore);
    dest.writeDouble(minScore);
    dest.writeDouble(totalScore);
    dest.writeString(totalScoreText);

    ParcelableUtils.writeDate(dest, fromDate);
    ParcelableUtils.writeDate(dest, tillDate);
    ParcelableUtils.writeDate(dest, submissionDate);
  }

  protected Assignment(Parcel in) {
    week = in.readInt();
    lesson = in.readInt();
    assignmentTitle = in.readString();
    maxScore = in.readDouble();
    minScore = in.readDouble();
    totalScore = in.readDouble();
    totalScoreText = in.readString();

    fromDate = ParcelableUtils.readDate(in);
    tillDate = ParcelableUtils.readDate(in);
    submissionDate = ParcelableUtils.readDate(in);
  }

  public static final Creator<Assignment> CREATOR = new Creator<Assignment>() {
    @Override public Assignment createFromParcel(Parcel in) {
      return new Assignment(in);
    }

    @Override public Assignment[] newArray(int size) {
      return new Assignment[size];
    }
  };

  public int getWeek() {
    return week;
  }

  public void setWeek(int week) {
    this.week = week;
  }

  public int getLesson() {
    return lesson;
  }

  public void setLesson(int lesson) {
    this.lesson = lesson;
  }

  public String getAssignmentTitle() {
    return assignmentTitle;
  }

  public void setAssignmentTitle(String assignmentTitle) {
    this.assignmentTitle = assignmentTitle;
  }

  public Date getFromDate() {
    return fromDate;
  }

  public void setFromDate(Date fromDate) {
    this.fromDate = fromDate;
  }

  public Date getTillDate() {
    return tillDate;
  }

  public void setTillDate(Date tillDate) {
    this.tillDate = tillDate;
  }

  public Date getSubmissionDate() {
    return submissionDate;
  }

  public void setSubmissionDate(Date submissionDate) {
    this.submissionDate = submissionDate;
  }

  public double getMaxScore() {
    return maxScore;
  }

  public void setMaxScore(double maxScore) {
    this.maxScore = maxScore;
  }

  public void setMaxScoreText(String scoreText) {
    this.maxScore = decodePointNumber(scoreText);
  }

  public double getMinScore() {
    return minScore;
  }

  public void setMinScore(double minScore) {
    this.minScore = minScore;
  }

  public void setMinScoreText(String scoreText) {
    this.minScore = decodePointNumber(scoreText);
  }

  public double getTotalScore() {
    return totalScore;
  }

  public boolean isTotalScoreUnavailable(){
    return !TextUtils.isEmpty(totalScoreText);
  }

  public String getTotalScoreText() {
    return totalScoreText;
  }

  public void setTotalScore(double totalScore) {
    this.totalScore = totalScore;
  }

  public void setTotalScoreText(String scoreText) {
    try{
      this.totalScore = decodePointNumber(scoreText);
    }catch (NumberFormatException e){
      this.totalScoreText = scoreText;
    }
  }

  public static double decodePointNumber(String scoreText){
    int tillPoint = scoreText.indexOf("Point");
    if (tillPoint != -1) {
      scoreText = scoreText.substring(0, tillPoint).trim();
    }
    return Double.parseDouble(scoreText);
  }

  @Override public String toString() {
    return "Assignment{" +
        "week=" + week +
        ", lesson=" + lesson +
        ", assignmentTitle='" + assignmentTitle + '\'' +
        ", fromDate=" + fromDate +
        ", tillDate=" + tillDate +
        ", submissionDate=" + submissionDate +
        ", maxScore=" + maxScore +
        ", minScore=" + minScore +
        ", totalScore=" + totalScore +
        '}';
  }

  @Override public int describeContents() {
    return 0;
  }

}
