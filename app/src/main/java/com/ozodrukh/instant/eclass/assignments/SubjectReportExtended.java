package com.ozodrukh.instant.eclass.assignments;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;
import com.ozodrukh.eclass.Timber;
import com.ozodrukh.eclass.entity.SubjectReport;
import com.ozodrukh.instant.eclass.R;
import com.ozodrukh.instant.eclass.utils.Truss;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SubjectReportExtended extends SubjectReport implements Parcelable {
  private static final SimpleDateFormat DAY_OF_MONTH =
      new SimpleDateFormat("dd, MMMM", Locale.getDefault());
  private static final SimpleDateFormat GENERAL_DATE =
      new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

  private static final Calendar todayCalendar = Calendar.getInstance();

  private String dateFormatted;

  public SubjectReportExtended(SubjectReport report) {
    super(report);
  }

  protected SubjectReportExtended(Parcel in) {
    super(in);
    dateFormatted = in.readString();
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(dateFormatted);
  }

  @Override public int describeContents() {
    return 0;
  }

  public static final Creator<SubjectReportExtended> CREATOR =
      new Creator<SubjectReportExtended>() {
        @Override public SubjectReportExtended createFromParcel(Parcel in) {
          return new SubjectReportExtended(in);
        }

        @Override public SubjectReportExtended[] newArray(int size) {
          return new SubjectReportExtended[size];
        }
      };

  @Override public void setSubmissionDate(Date submissionDate) {
    super.setSubmissionDate(submissionDate);

    if (submissionDate == null) {
      Timber.d("%s, has no submission date", getSubjectName());
      return;
    }

    Calendar submissionCalendar = Calendar.getInstance();
    submissionCalendar.setTimeInMillis(submissionDate.getTime());

    SimpleDateFormat dateFormat;
    if (todayCalendar.get(Calendar.YEAR) > submissionCalendar.get(Calendar.YEAR)
        || todayCalendar.get(Calendar.MONTH) > submissionCalendar.get(Calendar.MONTH)) {
      dateFormat = GENERAL_DATE;
    } else {
      dateFormat = DAY_OF_MONTH;
    }

    dateFormatted = dateFormat.format(submissionDate);
  }

  public String getSubmissionDateFormatted() {
    return dateFormatted;
  }

  /**
   * DataBinding custom attribute handler to set relative time from date time
   * as argument
   *
   * @param textView Target view
   * @param date Date to format as relative
   */
  @BindingAdapter("app:relativeTime")
  public static void setRelativeTimeDate(TextView textView, Date date) {
    textView.setText(DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(),
        5 * 60 * 1000, DateUtils.FORMAT_ABBREV_RELATIVE));
  }

  private static ForegroundColorSpan LINK_COLOR_SPAN;

  /**
   * DataBinding custom attribute handler to set attachment link on a text view
   * it's better to make little attribute modules instead of overwhelming with methods
   * in view holder or view
   *
   * Logic is simple makes textview looks like an anchor on web if report has
   * attachment
   *
   * @param textView Target View
   * @param report Data binding variable
   *
   * @see R.layout#subject_assignment_report_item_view
   */
  @BindingAdapter("app:fakeAttachmentLink")
  public static void setFakeAttachmentLink(TextView textView, SubjectReport report) {
    if (LINK_COLOR_SPAN == null) {
      LINK_COLOR_SPAN = new ForegroundColorSpan(
          ContextCompat.getColor(textView.getContext(), R.color.colorWebLinks));
    }

    boolean isAttachmentExists = true;

    Truss builder = new Truss().pushSpan(LINK_COLOR_SPAN);
    if (!TextUtils.isEmpty(report.getAttachmentLink())) {
      builder.pushSpan(new UnderlineSpan());
      isAttachmentExists = false;
    }
    builder.append(report.getName()).popSpans();
    textView.setText(builder.build());

    if (!isAttachmentExists) {
      return;
    }

    final Drawable attachmentIcon;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      attachmentIcon =
          ContextCompat.getDrawable(textView.getContext(), R.drawable.ic_attachment_black_24px);
    } else {
      attachmentIcon =
          VectorDrawableCompat.create(textView.getResources(), R.drawable.ic_attachment_black_24px,
              textView.getContext().getTheme());
    }

    assert attachmentIcon != null;

    attachmentIcon.setBounds(0, 0,
        textView.getResources().getDimensionPixelSize(R.dimen.attachment_icon_width),
        textView.getResources().getDimensionPixelSize(R.dimen.attachment_icon_height));

    textView.setCompoundDrawables(attachmentIcon, null, null, null);
  }

  @Override public String toString() {
    return "SubjectReportExtended{" +
        "dateFormatted='" + dateFormatted + '\'' +
        "} " + super.toString();
  }
}