package com.ozodrukh.instant.eclass.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ozodrukh.instant.eclass.BaseFragment;
import com.ozodrukh.instant.eclass.R;
import com.ozodrukh.instant.eclass.utils.EndlessPagination;

public class ReportsListFragment extends BaseFragment
    implements EndlessPagination.OnThresholdReachListener {

  private RecyclerView recyclerView;
  private EndlessPagination endlessPagination;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return recyclerView = new RecyclerView(inflater.getContext());
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // For further inheritance if some fragment want's to override
    // onCreateView, he just set id and everything will work smooth
    if (recyclerView == null) {
      recyclerView = (RecyclerView) view.findViewById(R.id.reports_recycler_view);

      if (recyclerView == null) {
        throw new NullPointerException(
            "RecyclerView should has id=\"reports_recycler_view\", but not found");
      }
    }

    setupRecyclerView(recyclerView);
  }

  public EndlessPagination getEndlessPagination() {
    return endlessPagination;
  }

  protected void setupRecyclerView(RecyclerView recyclerView) {
    endlessPagination = new EndlessPagination(recyclerView, this, true, /* threshold */ 10, 0);

    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
  }

  /**
   * Used to clean & load new data from scratch
   */
  public void clearReportsFragment() {
    //TODO clear adapter
    endlessPagination.setPage(0);
  }

  @Override public boolean onThresholdReached(int page) {
    return false;
  }
}
