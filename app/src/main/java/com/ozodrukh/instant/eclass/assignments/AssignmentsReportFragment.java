package com.ozodrukh.instant.eclass.assignments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.ozodrukh.eclass.InhaEclassController;
import com.ozodrukh.eclass.ReportParser.ListRecordReportParser;
import com.ozodrukh.eclass.Timber;
import com.ozodrukh.eclass.entity.SubjectReport;
import com.ozodrukh.instant.eclass.BaseFragment;
import com.ozodrukh.instant.eclass.R;
import com.ozodrukh.instant.eclass.utils.EndlessPagination;
import com.ozodrukh.instant.eclass.utils.AndroidUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.ozodrukh.eclass.InhaEclassWebService.Utils.getAssignmentsOptions;

public class AssignmentsReportFragment extends BaseFragment {
  public static final String TAG = "fragment:assignments-report";

  private RecyclerView reportsListView;
  private EndlessPagination pagination;
  private AssignmentsReportsAdapter adapter;
  private InhaEclassController eclassController;

  private Action1<List<SubjectReportExtended>> onAssignmentsReportLoaded =
      new Action1<List<SubjectReportExtended>>() {
        @Override public void call(List<SubjectReportExtended> reports) {
          pagination.setReceiveNotifications(!reports.isEmpty());
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
                      pagination.setThreshold(visibleItemsCount + 4);
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
          if (AndroidUtils.isNetworkAvailable(getContext())) {
            getAssignmentsReport(page, onAssignmentsReportLoaded);
            return false;
          }
          return true;
        }
      };

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return reportsListView = new RecyclerView(inflater.getContext());
  }

  @Override public void onViewCreated(View view, Bundle state) {
    super.onViewCreated(view, state);
    final Context context = getContext();
    List<SubjectReportExtended> items = Collections.emptyList();

    if (state != null) {
      pagination = new EndlessPagination(reportsListView, onThresholdReachListener,
          state.getBoolean("receiveNotifications", true),
          state.getInt("scrollItemsThreshold", EndlessPagination.THRESHOLD),
          state.getInt("pagesLoaded", 0));

      items = state.getParcelableArrayList("adapter:items");

      if (items == null) items = Collections.emptyList();
    }

    eclassController = InhaEclassController.getInstance();

    reportsListView.setItemAnimator(new DefaultItemAnimator());
    reportsListView.setLayoutManager(new LinearLayoutManager(context));
    reportsListView.setAdapter(adapter = new AssignmentsReportsAdapter());
    reportsListView.addItemDecoration(new BottomDivider(getContext()));

    adapter.addSubjectsReports(items);

    pagination = pagination != null ? pagination
        : new EndlessPagination(reportsListView, onThresholdReachListener);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    // Pagination state
    outState.putInt("pagesLoaded", pagination.getPage());
    outState.putInt("scrollItemsThreshold", pagination.getThreshold());
    outState.putBoolean("receiveNotifications", pagination.canSendNotifications());

    // Fragment state
    if (!adapter.getItems().isEmpty()) {
      outState.putParcelableArrayList("adapter:items",
          (ArrayList<? extends Parcelable>) adapter.getItems());
    }
  }

  /**
   * Refreshes UI by cleaning data and by resetting paginator that handles
   * 0-page as cue to load data
   */
  public void requestRecreate() {
    adapter.clear();
    pagination.setPage(reportsListView, 0);
  }

  /**
   * @param page Page load
   * @param callback Receive callback when loading done
   */
  protected void getAssignmentsReport(int page, Action1<List<SubjectReportExtended>> callback) {
    adapter.setItemsLoading(true);

    eclassController.getWebService()
        .getAssignmentsReport(getAssignmentsOptions(eclassController.getCurrentUser(), page, null))
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
            pagination.onPageDidNotLoaded();
            Timber.e(err, "Oops!");

            if (getView() != null && err instanceof IOException) {
              Snackbar.make(getView(),
                  AndroidUtils.getExceptionDetailHumanReadableMessage(getContext(), (IOException) err),
                  Snackbar.LENGTH_LONG).show();
            }
          }
        });
  }

  private static class AssignmentsReportsAdapter
      extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SubjectReportExtended> subjectsReports = new ArrayList<>();
    private boolean itemsLoading;

    public List<SubjectReportExtended> getItems() {
      return subjectsReports;
    }

    public void setItemsLoading(boolean itemsLoading) {
      this.itemsLoading = itemsLoading;

      if (itemsLoading) {
        notifyItemInserted(subjectsReports.size());
      } else {
        notifyItemRemoved(subjectsReports.size());
      }
    }

    public void addSubjectsReports(List<SubjectReportExtended> reports) {
      if (reports.isEmpty()) {
        return;
      }

      int positionStart = getItemCount();
      setItemsLoading(false);

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

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      if (viewType == 0) {
        return SubjectAssignmentReportViewHolder.create(parent, viewType);
      } else {
        LayoutInflater factory = LayoutInflater.from(parent.getContext());
        return new RecyclerView.ViewHolder(factory.inflate(R.layout.progress_bar, parent, false)) {
        };
      }
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      if (holder instanceof SubjectAssignmentReportViewHolder) {
        SubjectAssignmentReportViewHolder sh = (SubjectAssignmentReportViewHolder) holder;
        sh.bind(subjectsReports.get(position));
      }
    }

    @Override public int getItemViewType(int position) {
      return itemsLoading && position == subjectsReports.size() ? 1 : 0;
    }

    @Override public int getItemCount() {
      return subjectsReports.size() + (itemsLoading ? 1 : 0);
    }
  }

  static class BottomDivider extends RecyclerView.ItemDecoration {

    static final Paint paint = new Paint();

    public BottomDivider(Context context) {
      paint.setColor(0x56777777);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(2);
    }

    @Override public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
      final int childCount = parent.getChildCount();
      for (int i = 0; i < childCount; i++) {
        View child = parent.getChildAt(i);
        c.drawLine(child.getLeft(), child.getBottom(), child.getRight(), child.getBottom(), paint);
      }
    }
  }
}
