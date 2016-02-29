package com.ozodrukh.eclass.entity;

import android.os.Parcel;
import android.os.Parcelable;
import com.ozodrukh.eclass.Utils;
import com.ozodrukh.eclass.guava.ParcelableUtils;
import java.util.Date;

public class Assignment implements Parcelable{
  private int week;
  private int lesson;
  private String assignmentTitle;
  private Date fromDate;
  private Date tillDate;
  private Date submissionDate;

  private double maxScore;
  private double minScore;
  private double totalScore;

  public Assignment() {
  }

  protected Assignment(Parcel in) {
    week = in.readInt();
    lesson = in.readInt();
    assignmentTitle = in.readString();
    maxScore = in.readDouble();
    minScore = in.readDouble();
    totalScore = in.readDouble();

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
    int tillPoint = scoreText.indexOf("Point");
    this.maxScore = Utils.decodeNumber(scoreText, 0, tillPoint == -1 ? scoreText.length() : tillPoint);
  }

  public double getMinScore() {
    return minScore;
  }

  public void setMinScore(double minScore) {
    this.minScore = minScore;
  }

  public void setMinScoreText(String scoreText) {
    int tillPoint = scoreText.indexOf("Point");
    this.minScore = Utils.decodeNumber(scoreText, 0, tillPoint == -1 ? scoreText.length() : tillPoint);
  }

  public double getTotalScore() {
    return totalScore;
  }

  public void setTotalScore(double totalScore) {
    this.totalScore = totalScore;
  }

  public void setTotalScoreText(String scoreText) {
    int tillPoint = scoreText.indexOf("Point");
    this.totalScore = Utils.decodeNumber(scoreText, 0, tillPoint == -1 ? scoreText.length() : tillPoint);
  }

  @Override
  public String toString() {
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

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(week);
    dest.writeInt(lesson);
    dest.writeString(assignmentTitle);
    dest.writeDouble(maxScore);
    dest.writeDouble(minScore);
    dest.writeDouble(totalScore);

    ParcelableUtils.writeDate(dest, fromDate);
    ParcelableUtils.writeDate(dest, tillDate);
    ParcelableUtils.writeDate(dest, submissionDate);
  }
}
