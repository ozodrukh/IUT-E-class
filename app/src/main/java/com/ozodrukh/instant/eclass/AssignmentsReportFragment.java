package com.ozodrukh.instant.eclass;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import com.ozodrukh.eclass.InhaEclassController;
import com.ozodrukh.eclass.ReportParser.ListRecordReportParser;
import com.ozodrukh.eclass.Timber;
import com.ozodrukh.eclass.entity.SubjectReport;
import com.ozodrukh.instant.eclass.utils.EndlessPagination;
import com.ozodrukh.instant.eclass.utils.Utils;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.support.v7.widget.RecyclerView.ViewHolder;
import static com.ozodrukh.eclass.InhaEclassWebService.Utils.getAssignmentsOptions;

public class AssignmentsReportFragment extends BaseFragment {
  public static final String TAG = "fragment:assignments-report";

  private RecyclerView reportsListView;
  private EndlessPagination pagintator;
  private AssignmentsReportsAdapter adapter;
  private InhaEclassController eclassController;

  private Action1<List<SubjectReportExtended>> onAssignmentsReportLoaded =
      new Action1<List<SubjectReportExtended>>() {
        @Override public void call(List<SubjectReportExtended> reports) {
          pagintator.setReceiveNotifications(!reports.isEmpty());
          int itemsCount = adapter.getItemCount();
          adapter.addSubjectsReports(reports);

          if (itemsCount == 0) {
            /*
              After assignment list is downloaded when can compute how many items
              device can show, therefore we adding layout listener after loading
              items
             */
            reportsListView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                  @Override public void onGlobalLayout() {
                    LinearLayoutManager lm =
                        (LinearLayoutManager) reportsListView.getLayoutManager();
                    int visibleItemsCount = lm.findLastCompletelyVisibleItemPosition();

                    if (visibleItemsCount > 0) {

                      int lastItemBottom = reportsListView.findViewHolderForAdapterPosition(
                          visibleItemsCount).itemView.getBottom();

                      if (lastItemBottom > 0 && lastItemBottom < lm.getHeight()) {
                        int itemHeight = lastItemBottom / visibleItemsCount;
                        visibleItemsCount = lm.getHeight() / itemHeight;
                      }

                      // where 4 is additional threshold
                      pagintator.setThreshold(visibleItemsCount + 4);
                      reportsListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                  }
                });
          }
        }
      };

  private EndlessPagination.OnThresholdReachListener onThresholdReachListener =
      new EndlessPagination.OnThresholdReachListener() {
        @Override public boolean onThresholdReached(int page) {
          if (Utils.isNetworkAvailable(getContext())) {
            getAssignmentsReport(page, onAssignmentsReportLoaded);
            return false;
          }
          return true;
        }
      };

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container) {
    return reportsListView = new RecyclerView(inflater.getContext());
  }

  @Override public void onViewCreated(View view, Bundle state) {
    super.onViewCreated(view, state);
    final Context context = getContext();
    List<SubjectReportExtended> items = Collections.emptyList();

    if (state != null) {
      pagintator = new EndlessPagination(reportsListView, onThresholdReachListener,
          state.getBoolean("receiveNotifications", true),
          state.getInt("scrollItemsThreshold", EndlessPagination.THRESHOLD),
          state.getInt("pagesLoaded", 0));

      items = state.getParcelableArrayList("adapter:items");
    }

    eclassController = InhaEclassController.getInstance();

    reportsListView.setItemAnimator(new DefaultItemAnimator());
    reportsListView.setLayoutManager(new LinearLayoutManager(context));
    reportsListView.setAdapter(adapter = new AssignmentsReportsAdapter());

    adapter.addSubjectsReports(items);

    pagintator = pagintator != null ? pagintator
        : new EndlessPagination(reportsListView, onThresholdReachListener);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    // Pagination state
    outState.putInt("pagesLoaded", pagintator.getPage());
    outState.putInt("scrollItemsThreshold", pagintator.getThreshold());
    outState.putBoolean("receiveNotifications", pagintator.canSendNotifications());

    // Fragment state
    if (adapter.getItems() != Collections.EMPTY_LIST) {
      outState.putParcelableArrayList("adapter:items",
          (ArrayList<? extends Parcelable>) adapter.getItems());
    }
  }

  /**
   * @param page Page load
   * @param callback Receive callback when loading done
   */
  protected void getAssignmentsReport(int page, Action1<List<SubjectReportExtended>> callback) {
    eclassController.getWebService()
        .getAssignmentsHistory(getAssignmentsOptions(eclassController.getCurrentUser(), page, null))
        .flatMap(new Func1<Response<ResponseBody>, Observable<List<SubjectReportExtended>>>() {
          @Override public Observable<List<SubjectReportExtended>> call(
              Response<ResponseBody> reportsHtmlResponse) {
            String html = "";
            try {
              html = reportsHtmlResponse.body().string();
            } catch (IOException e) {
              e.printStackTrace();
            }

            ListRecordReportParser parser = new ListRecordReportParser();
            return Observable.from(parser.parse(html))
                .map(new Func1<SubjectReport, SubjectReportExtended>() {
                  @Override public SubjectReportExtended call(SubjectReport report) {
                    return new SubjectReportExtended(report);
                  }
                })
                .toList();
          }
        })
        .compose(this.<List<SubjectReportExtended>>bindToLifecycle())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(callback, new Action1<Throwable>() {
          @Override public void call(Throwable err) {
            pagintator.onPageDidNotLoaded();
            Timber.e(err, "Oops!");

            if (getView() != null && err instanceof IOException) {
              Snackbar.make(getView(),
                  Utils.getExceptionDetailHumanReadableMessage(getContext(), (IOException) err),
                  Snackbar.LENGTH_LONG).show();
            }
          }
        });
  }

  private static class AssignmentsReportsAdapter
      extends RecyclerView.Adapter<SubjectAssignmentReportViewHolder> {

    private List<SubjectReportExtended> subjectsReports = Collections.emptyList();
    private boolean itemsLoading;

    public List<SubjectReportExtended> getItems() {
      return subjectsReports;
    }

    public void setItemsLoading(boolean itemsLoading) {
      this.itemsLoading = itemsLoading;
    }

    public void addSubjectsReports(List<SubjectReportExtended> reports) {
      if (reports.isEmpty()) {
        return;
      }

      if (subjectsReports == Collections.EMPTY_LIST) {
        subjectsReports = new ArrayList<>();
      }

      int positionStart = getItemCount();

      itemsLoading = false;

      subjectsReports.addAll(reports);
      notifyItemRangeInserted(positionStart, reports.size());
    }

    public void clear() {
      if (subjectsReports.isEmpty()) {
        return;
      }

      int itemsCount = subjectsReports.size();
      subjectsReports.clear();
      notifyItemRangeRemoved(0, itemsCount);
    }

    @Override
    public SubjectAssignmentReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return SubjectAssignmentReportViewHolder.create(parent, viewType);
    }

    @Override public void onBindViewHolder(SubjectAssignmentReportViewHolder holder, int position) {
      holder.bind(subjectsReports.get(position));
      holder.itemView.setBackgroundColor(position % 2 == 0 ? 0xFFFAFAFA : 0xFFE1F5FE);
    }

    @Override public int getItemCount() {
      return subjectsReports.size() + (itemsLoading ? 1 : 0);
    }
  }

  private static class SubjectAssignmentReportViewHolder extends ViewHolder {
    private final static int COLOR_SCORE_PUBLIC = 0xFF009688;
    private final static int COLOR_SCORE_PRIVATE = 0xFF757575;

    public static SubjectAssignmentReportViewHolder create(ViewGroup parent, int type) {

      return new SubjectAssignmentReportViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.subject_assignmnet_report_item_view, parent, false));
    }

    TextView attachmentName;
    TextView subjectName;
    TextView submissionDate;
    TextView score;

    SubjectReportExtended subjectReport;

    public SubjectAssignmentReportViewHolder(View itemView) {
      super(itemView);

      attachmentName = (TextView) itemView.findViewById(R.id.attachment_name);
      subjectName = (TextView) itemView.findViewById(R.id.subject_name);
      submissionDate = (TextView) itemView.findViewById(R.id.submission_date);
      score = (TextView) itemView.findViewById(R.id.recieved_score);

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          AppCompatActivity activity = (AppCompatActivity) v.getContext();

          activity.getSupportFragmentManager()
              .beginTransaction()
              .add(R.id.app_content_view, AssignmentDetailFragment.newInstance(subjectReport),
                  AssignmentDetailFragment.TAG)
              .addToBackStack(AssignmentDetailFragment.TAG)
              .commit();
        }
      });
    }

    public void bind(SubjectReportExtended report) {
      subjectReport = report;
      attachmentName.setVisibility(TextUtils.isEmpty(report.getName()) ? View.GONE : View.VISIBLE);

      if (attachmentName.getVisibility() == View.VISIBLE) {
        attachmentName.setText(report.getName());
      }

      subjectName.setText(report.getSubjectName());
      submissionDate.setText(report.getSubmissionDateFormatted());

      if (Double.isNaN(report.getScore())) {
        score.setTextColor(COLOR_SCORE_PRIVATE);
        score.setText(report.getScoreText());
      } else {
        score.setTextColor(COLOR_SCORE_PUBLIC);
        score.setText(String.valueOf((int) report.getScore()));
      }
    }
  }

  public static class SubjectReportExtended extends SubjectReport implements Parcelable {
    private static final SimpleDateFormat DAY_OF_MONTH =
        new SimpleDateFormat("dd, MMMM", Locale.getDefault());
    private static final SimpleDateFormat GENERAL_DATE =
        new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

    private static final Calendar todayCalendar = Calendar.getInstance();

    private String dateFormatted;

    public SubjectReportExtended(SubjectReport report) {
      setName(report.getName());
      setSubjectName(report.getSubjectName());
      setSubmissionDate(report.getSubmissionDate());
      setAttachmentLink(report.getAttachmentLink());
      setScore(report.getScore());
      setScoreText(report.getScoreText());
      setSemester(report.getSemester());
      setYear(report.getYear());
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

    @Override public String toString() {
      return "SubjectReportExtended{" +
          "dateFormatted='" + dateFormatted + '\'' +
          "} " + super.toString();
    }
  }
}
