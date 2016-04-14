package com.ozodrukh.eclass;

import android.text.TextUtils;
import com.ozodrukh.eclass.entity.Assignment;
import com.ozodrukh.eclass.entity.SubjectReport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class ReportParser<ParsedData> {
  protected SimpleDateFormat ECLASS_DATE_PATTERN =
      new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

  public abstract ParsedData parse(String htmlDocument);

  /**
   * Parser of E-class Assignment history HTML document
   */
  public static class ListRecordReportParser extends ReportParser<List<SubjectReport>> {

    public static final int NO = 0;
    public static final int YEAR = 1;
    public static final int SEMESTER = 2;
    public static final int COURSE_PLAN = 3;
    public static final int ASSIGNMENT_TITLE = 4;
    public static final int SUBMISSION_DATE = 5;
    public static final int RECIEVED_SCORE = 6;
    public static final int GOOD_ASSIGNMENT = 7;
    public static final int SEARCH = 8;

    @Override public List<SubjectReport> parse(String htmlDocument) {
      Document document = Jsoup.parse(htmlDocument);
      Elements reportTable = document.body().select("table:first-child tbody tr");
      List<SubjectReport> reportList = new ArrayList<>(reportTable.size());
      for (int index = 1; index < reportTable.size(); index++) {
        Elements cells = reportTable.get(index).select("td");

        SubjectReport report = new SubjectReport();

        for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++) {
          Element element = cells.get(cellIndex);
          switch (cellIndex) {
            case YEAR:
              report.setYear(Integer.parseInt(element.text()));
              break;
            case SEMESTER:
              report.setSemester(Integer.parseInt(element.text()));
              break;
            case COURSE_PLAN:
              report.setSubjectName(element.text());
              break;
            case ASSIGNMENT_TITLE:
              Element anchor = element.child(0);
              report.setAttachmentLink(anchor.attr("href"));
              report.setName(TextUtils.isEmpty(anchor.text()) ? anchor.attr("title") : anchor.text());
              break;
            case SUBMISSION_DATE:
              try {
                report.setSubmissionDate(ECLASS_DATE_PATTERN.parse(element.text()));
              } catch (ParseException e) {
                e.printStackTrace();
              }
              break;
            case RECIEVED_SCORE:
              report.setScoreText(element.text());
              break;
            case SEARCH: {
              Element span = element.child(0);
              if ("span".equals(span.tagName())) {
                String reportDetailViewFunction = span.attr("onclick");
                int s = reportDetailViewFunction.indexOf("whenReportDetailView(");
                int e = reportDetailViewFunction.indexOf(");");
                if (s >= 0 && e >= 0) {
                  s += "whenReportDetailView(".length();

                  String[] detailSubjectAttrs = reportDetailViewFunction.substring(s, e).split(",");
                  report.setDetailAttrs(detailSubjectAttrs);
                }
              }
              break;
            }
            case NO:
            case GOOD_ASSIGNMENT:
            default:
              break;
          }
        }

        if (report.getName() != null
            && report.getSubjectName() != null
            && report.getScoreText() != null) {

          reportList.add(report);
        }
      }
      return reportList;
    }
  }

  public static class ReportStuServeletParser extends ReportParser<Assignment> {

    private final static int WEEK = 0;
    private final static int LESSON = 1;
    private final static int ASSIGNMENT_TITLE = 2;
    private final static int DUE_DATE = 3;
    private final static int FULL_SCORE = 4;
    private final static int BASE_SCORE = 5;

    private final static int SUBMISSION_DATE = 0;
    private final static int TOTAL_SCORE = 1;

    @Override public Assignment parse(String htmlDocument) {
      Document document = Jsoup.parse(htmlDocument);
      Elements tables = document.select(".contentWrapper table");

      Assignment assignment = new Assignment();

      if (tables.isEmpty()) {
        return assignment;
      }

      final Elements detailsInfo = tables.get(0).select("tbody tr:nth-child(2) td");
      for (int i = 0; i < detailsInfo.size(); i++) {
        Element element = detailsInfo.get(i);
        switch (i) {
          case WEEK:
            assignment.setWeek(Integer.parseInt(element.text()));
            break;
          case LESSON:
            assignment.setLesson(Integer.parseInt(element.text()));
            break;
          case ASSIGNMENT_TITLE:
            assignment.setAssignmentTitle(element.text());
            break;
          case DUE_DATE:
            String dueDate = element.text();
            String[] dates = dueDate.split("~");
            try {
              assignment.setFromDate(ECLASS_DATE_PATTERN.parse(dates[0].trim()));
              assignment.setTillDate(ECLASS_DATE_PATTERN.parse(dates[1].trim()));
            } catch (ParseException e) {
              e.printStackTrace();
            }
            break;
          case FULL_SCORE:
            assignment.setMaxScoreText(element.text());
            break;
          case BASE_SCORE:
            assignment.setMinScoreText(element.text());
            break;
        }
      }

      final Elements submitterInfo = tables.get(1).select("tbody tr:nth-child(2) td");
      for (int i = 0; i < submitterInfo.size(); i++) {
        Element element = submitterInfo.get(i);
        switch (i) {
          case SUBMISSION_DATE:
            try {
              assignment.setSubmissionDate(ECLASS_DATE_PATTERN.parse(element.text()));
            } catch (ParseException e) {
              e.printStackTrace();
            }
            break;
          case TOTAL_SCORE:
            assignment.setTotalScoreText(element.text());
            break;
        }
      }

      return assignment;
    }
  }
}
