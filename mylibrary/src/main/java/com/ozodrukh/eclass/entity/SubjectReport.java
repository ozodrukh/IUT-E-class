package com.ozodrukh.eclass.entity;

import android.os.Parcel;
import android.os.Parcelable;
import com.ozodrukh.eclass.Timber;
import com.ozodrukh.eclass.Utils;
import com.ozodrukh.eclass.guava.OptionsBuilder;
import com.ozodrukh.eclass.guava.ParcelableUtils;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class SubjectReport implements Parcelable{

  protected SubjectReport(Parcel in) {
    name = in.readString();
    scoreText = in.readString();
    score = in.readDouble();
    subjectName = in.readString();
    attachmentLink = in.readString();
    year = in.readInt();
    semester = in.readInt();
    detailAttrs = in.createStringArray();
    submissionDate = ParcelableUtils.readDate(in);
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(scoreText);
    dest.writeDouble(score);
    dest.writeString(subjectName);
    dest.writeString(attachmentLink);
    dest.writeInt(year);
    dest.writeInt(semester);
    dest.writeStringArray(detailAttrs);
    ParcelableUtils.writeDate(dest, submissionDate);
  }

  public static final Creator<SubjectReport> CREATOR = new Creator<SubjectReport>() {
    @Override public SubjectReport createFromParcel(Parcel in) {
      return new SubjectReport(in);
    }

    @Override public SubjectReport[] newArray(int size) {
      return new SubjectReport[size];
    }
  };

  /**
   * @return Text inside single quotes
   */
  public static String clearQuotes(String value) {
    StringBuilder builder = new StringBuilder();
    boolean readLiteral = false;
    int index = 0;
    while (true) {
      char c = value.charAt(index);
      if (c == '\'') {
        if (readLiteral) {
          return builder.toString();
        }
        readLiteral = true;
      } else if (readLiteral) {
        builder.append(c);
      }
      index++;
    }
  }

  // Report list information
  private String name;
  private String scoreText;
  private double score = Double.NaN;
  private String subjectName;
  private String attachmentLink;
  private int year;
  private int semester;
  private Date submissionDate;

  private String[] detailAttrs;

  public SubjectReport(String name, String scoreText, double score, String subjectName,
      String attachmentLink, int year, int semester, Date submissionDate, String[] detailAttrs) {
    this.name = name;
    this.scoreText = scoreText;
    this.score = score;
    this.subjectName = subjectName;
    this.attachmentLink = attachmentLink;
    this.year = year;
    this.semester = semester;
    this.submissionDate = submissionDate;
    this.detailAttrs = detailAttrs;
  }

  public SubjectReport(SubjectReport report) {
    this.name = report.name;
    this.scoreText = report.scoreText;
    this.score = report.score;
    this.subjectName = report.subjectName;
    this.attachmentLink = report.attachmentLink;
    this.year = report.year;
    this.semester = report.semester;
    this.submissionDate = report.submissionDate;
    this.detailAttrs = report.detailAttrs;
  }

  public SubjectReport() {
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getScoreText() {
    return scoreText;
  }

  public void setScoreText(String scoreText) {
    this.scoreText = scoreText = scoreText.trim();

    if (!scoreText.equalsIgnoreCase("private") && scoreText.length() > 0) {
      int tillPoint = scoreText.indexOf("Point");
      this.score = Utils.decodeNumber(scoreText, 0, tillPoint == -1 ? scoreText.length() : tillPoint);
    } else {
      this.score = Double.NaN;
    }
  }

  public String getSubjectName() {
    return subjectName;
  }

  public void setSubjectName(String subjectName) {
    this.subjectName = subjectName;
  }

  public String getAttachmentLink() {
    return attachmentLink;
  }

  public void setAttachmentLink(String attachmentLink) {
    this.attachmentLink = attachmentLink;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getSemester() {
    return semester;
  }

  public void setSemester(int semester) {
    this.semester = semester;
  }

  public Date getSubmissionDate() {
    return submissionDate;
  }

  public void setSubmissionDate(Date submissionDate) {
    this.submissionDate = submissionDate;
  }

  public Map<String, String> getDetailAttrs() {
    if (detailAttrs == null) {
      return null;
    }

    return new OptionsBuilder<String, String>()
        .put("p_process", "viewRecordReport")
        .put("p_pageno", "")
        .put("p_grcode", clearQuotes(detailAttrs[0]))
        .put("p_subj", clearQuotes(detailAttrs[1]))
        .put("p_year", clearQuotes(detailAttrs[2]))
        .put("p_subjseq", clearQuotes(detailAttrs[3]))
        .put("p_class", clearQuotes(detailAttrs[4]))
        .put("p_ordseq", clearQuotes(detailAttrs[5]))
        .put("p_seq", clearQuotes(detailAttrs[6]))
        .build();
  }

  public void setDetailAttrs(String[] detailAttrs) {
    Timber.d(Arrays.toString(detailAttrs));
    this.detailAttrs = detailAttrs;
  }

  public String[] getDetailAttrsArray(){
    return detailAttrs;
  }

  @Override
  public String toString() {
    return "SubjectReport{" +
        "name='" + name + '\'' +
        ", scoreText='" + scoreText + '\'' +
        ", score=" + score +
        ", subjectName='" + subjectName + '\'' +
        ", attachmentLink='" + attachmentLink + '\'' +
        ", year=" + year +
        ", semester=" + semester +
        ", submissionDate=" + submissionDate +
        ", detailAttrs=" + Arrays.toString(detailAttrs) +
        '}';
  }

  @Override public int describeContents() {
    return 0;
  }
}
