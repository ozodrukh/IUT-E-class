package com.ozodrukh.instant.eclass.assignments;

import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ozodrukh.eclass.InhaEclassController;
import com.ozodrukh.eclass.ReportParser;
import com.ozodrukh.eclass.entity.Assignment;
import com.ozodrukh.instant.eclass.BR;
import com.ozodrukh.instant.eclass.R;
import com.ozodrukh.instant.eclass.databinding.SubjectAssignmentReportItemViewBinding;
import com.ozodrukh.instant.eclass.permission.Police;
import com.ozodrukh.instant.eclass.utils.RxUtils;
import com.ozodrukh.instant.eclass.utils.Truss;
import java.io.File;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SubjectAssignmentReportViewHolder extends RecyclerView.ViewHolder {

  public static SubjectAssignmentReportViewHolder create(ViewGroup parent, int type) {
    return new SubjectAssignmentReportViewHolder(
        DataBindingUtil.<SubjectAssignmentReportItemViewBinding>inflate(
            LayoutInflater.from(parent.getContext()), R.layout.subject_assignment_report_item_view,
            parent, false));
  }

  SubjectAssignmentReportItemViewBinding binding;

  public SubjectAssignmentReportViewHolder(SubjectAssignmentReportItemViewBinding binding) {
    super(binding.getRoot());
    this.binding = binding;
    this.binding.setVariable(BR.viewHolder, this);
  }

  public void bind(final SubjectReportExtended report) {
    binding.setVariable(BR.subjectReport, report);
  }

  public void handleAttachmentLink(View view) {
    final ContextWrapper context = (ContextWrapper) view.getContext();
    Police.with(context)
        .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .withListener(new Runnable() {
          @Override public void run() {
            File baseDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "E-class");

            if (!baseDir.exists() && !baseDir.mkdirs()) {
              throw new RuntimeException("No access to storage");
            }

            File destination = new File(baseDir, binding.getSubjectReport().getName());

            Intent intent = new Intent(context, AttachmentDownloaderService.class);
            intent.putExtra(AttachmentDownloaderService.EXTRA_ASSIGNMENT_SUBJECT,
                binding.getSubjectReport());
            intent.putExtra(AttachmentDownloaderService.EXTRA_DESTINATION, destination.getPath());
            context.startService(intent);
          }
        })
        .check();
  }

  public void handlePrivateScore(View view) {
    InhaEclassController.getInstance()
        .getWebService()
        .getAssignmentsReport(binding.getSubjectReport().getDetailAttrs())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .flatMap(new Func1<Response<ResponseBody>, Observable<Assignment>>() {
          @Override
          public Observable<Assignment> call(Response<ResponseBody> responseBodyResponse) {
            try {
              return Observable.just(new ReportParser.ReportStuServeletParser().parse(
                  responseBodyResponse.body().string()));
            } catch (IOException e) {
              e.printStackTrace();
              return Observable.error(e);
            } finally {
              responseBodyResponse.body().close();
            }
          }
        })
        .subscribe(new Action1<Assignment>() {
          @Override public void call(Assignment assignment) {
            Activity activity = (Activity) itemView.getContext();

            Truss userScoreText = new Truss().append("Your assignment score is ")
                .pushSpan(new RelativeSizeSpan(1.6f))
                .pushSpan(new StyleSpan(Typeface.BOLD))
                .pushSpan(new ForegroundColorSpan(
                    ContextCompat.getColor(itemView.getContext(), R.color.colorAccent)));

            if (assignment.isTotalScoreUnavailable()) {
              userScoreText.append(assignment.getTotalScoreText()).popSpans();
            } else {
              userScoreText.append(String.valueOf(assignment.getTotalScore()))
                  .popSpans()
                  .append(" out of ")
                  .pushSpan(new RelativeSizeSpan(1.2f))
                  .append(String.valueOf(assignment.getMaxScore()))
                  .popSpan()
                  .build();
            }

            Snackbar.make(activity.findViewById(R.id.app_content_view), userScoreText.build(),
                Snackbar.LENGTH_LONG).show();
          }
        }, RxUtils.handleIOExceptions(itemView.getContext()));
  }
}