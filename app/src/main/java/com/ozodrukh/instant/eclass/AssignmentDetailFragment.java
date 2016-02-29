package com.ozodrukh.instant.eclass;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ozodrukh.eclass.entity.SubjectReport;

public class AssignmentDetailFragment extends BaseFragment {
  public static final String TAG = "fragment:AssignmentDetail";

  private static final String KEY_ASSIGNMENT = "assignment:self";
  private static final String KEY_SUBJECT_REPORT = "assignment:report";
  private static final String KEY_SUBJECT = "assignment:subject";

  public static AssignmentDetailFragment newInstance(SubjectReport subjectReport) {
    Bundle args = new Bundle();
    args.putParcelable(KEY_SUBJECT_REPORT, subjectReport);

    AssignmentDetailFragment detailView = new AssignmentDetailFragment();
    detailView.setArguments(args);
    return detailView;
  }

  @Override protected View onCreateView(LayoutInflater inflater, ViewGroup parent) {
    return super.onCreateView(inflater, parent);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Bundle bundle = getArguments();

    SubjectReport subjectReport = bundle.getParcelable(KEY_SUBJECT_REPORT);
    if (subjectReport == null) {
      Snackbar.make(view, "Cannot display assignment due nothing to show",
          Snackbar.LENGTH_LONG).show();

      getFragmentManager().beginTransaction()
          .hide(this)
          .commit();

      return;
    }

    Activity activity = (Activity) getContext();

    Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
    toolbar.setTitle(subjectReport.getSubjectName());
    toolbar.setSubtitle(subjectReport.getName());
  }
}
