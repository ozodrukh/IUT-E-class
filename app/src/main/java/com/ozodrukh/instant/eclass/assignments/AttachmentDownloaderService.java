package com.ozodrukh.instant.eclass.assignments;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.ozodrukh.eclass.InhaEclassController;
import com.ozodrukh.eclass.InhaEclassWebService;
import com.ozodrukh.eclass.entity.SubjectReport;
import com.ozodrukh.instant.eclass.R;
import com.ozodrukh.instant.eclass.utils.IOUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class AttachmentDownloaderService extends Service {
  /**
   * if permission wasn't granted at download beginning then it will be requested
   * from activity, and service will await till it granting request
   */
  public final static int MSG_START_DOWNLOADING_SERVICE = 1;

  public final static int NOTIFICATION_ID = 123_000;

  public final static String EXTRA_ASSIGNMENT_SUBJECT = "arg:attachment_info";
  public final static String EXTRA_DESTINATION = "arg:destination";

  private Handler handler;
  private HandlerThread handlerThread;
  private Handler.Callback serviceHandlerCallback = new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      if (msg.what == MSG_START_DOWNLOADING_SERVICE && !requests.isEmpty()) {
        if (checkCallingOrSelfPermission(READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
            && checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
          startAttachmentDownloading(requests.pop());
          return true;
        }
      }
      return false;
    }
  };

  private ArrayDeque<AttachmentRequest> requests = new ArrayDeque<>();
  private AtomicInteger attachmentsDownloaded = new AtomicInteger(0);

  public AttachmentDownloaderService() {
  }

  @Override public void onCreate() {
    super.onCreate();
    handlerThread = new HandlerThread("AttachmentDownloader", Process.THREAD_PRIORITY_BACKGROUND);
    handlerThread.start();

    handler = new Handler(handlerThread.getLooper(), serviceHandlerCallback);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    handlerThread.quit();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent == null) {
      return START_NOT_STICKY;
    }

    requests.push(new AttachmentRequest(intent));

    handler.sendEmptyMessage(MSG_START_DOWNLOADING_SERVICE);
    return START_STICKY;
  }

  @Override public IBinder onBind(Intent intent) {
    return new Messenger(handler).getBinder();
  }

  protected void startAttachmentDownloading(AttachmentRequest attachmentRequest) {
    Notification foregroundNotification =
        new Notification.Builder(this).setSmallIcon(R.drawable.ic_cloud_download_black_24dp)
            .build();

    startForeground(0, foregroundNotification);

    SubjectReportExtended attachment = attachmentRequest.attachment;
    File destination = attachmentRequest.destination;

    Notification notification =
        new NotificationCompat.Builder(this).setContentTitle(attachment.getName())
            .setSmallIcon(R.drawable.ic_cloud_download_black_24dp)
            .setProgress(0, 0, true)
            .build();

    NotificationManagerCompat.from(this)
        .notify(NOTIFICATION_ID + attachmentsDownloaded.get(), notification);

    final String attachmentLink = attachment.getAttachmentLink();
    final Request request;
    /*
      Parse attachment link, in source they invoke javascript function and there
      they building form and sending it, we need to take arguments of function
      and pass it
     */
    int s = attachmentLink.indexOf("javascript:download(");
    int e = attachmentLink.indexOf(");");
    if (s >= 0 && e >= 0) {
      s += "javascript:download(".length();

      String[] attrs = attachmentLink.substring(s, e).split(",");
      for (int i = 0; i < attrs.length; i++) {
        attrs[i] = SubjectReport.clearQuotes(attrs[i]);
      }

      // Build form from function arguments
      request = new Request.Builder().url(HttpUrl.parse(
          InhaEclassWebService.ENDPOINT + "/servlet/controller.library.DownloadServlet"))
          .post(new FormBody.Builder().add("p_subj", attrs[1])
              .add("p_year", attrs[2])
              .add("p_subjseq", attrs[3])
              .add("p_class", attrs[4])
              .add("p_ordseq", attrs[5])
              .add("p_filepath",
                  String.format("\\%s%s\\%s\\%s\\%s\\%s", attrs[2], attrs[3], attrs[0], attrs[1],
                      attrs[4], attrs[5]))
              .add("p_realfile", attrs[7])
              .add("p_savefile", attrs[6])
              .build())
          .build();
    } else {
      stopForeground(true);
      return;
    }

    OkHttpClient client = InhaEclassController.getInstance().getOkHttpClient();
    try {
      okhttp3.Response response = client.newCall(request).execute();
      if (response.isSuccessful()) {
        // Looks like permission is not given, therefore we throw an error
        if (!destination.exists() && !destination.createNewFile()) {
          throw new FileNotFoundException("File not created, check out permission to write");
        }

        // Coping file to local storage and close streams
        FileOutputStream fos = new FileOutputStream(destination);
        try {
          IOUtils.copy(response.body().byteStream(), fos);
        } finally {
          IOUtils.closeQuietly(fos);
          response.body().close();
        }
      }
    } catch (IOException error) {
      error.printStackTrace();
    }

    notification = buildDownloadedFileActions(destination,
        new NotificationCompat.Builder(this).setContentTitle(attachment.getName())
            .setContentText("Download complete")
            .setSmallIcon(R.drawable.ic_cloud_download_black_24dp)
            .setGroup("assignment_attachments_complete")
            .setAutoCancel(false)).build();

    NotificationManagerCompat.from(this)
        .notify(NOTIFICATION_ID + attachmentsDownloaded.get(), notification);

    attachmentsDownloaded.incrementAndGet();
    if (!requests.isEmpty()) {
      startAttachmentDownloading(requests.pop());
    } else {
      stopForeground(false);
    }
  }

  NotificationCompat.Builder buildDownloadedFileActions(File destination,
      NotificationCompat.Builder builder) {

    builder.addAction(0, "Open", PendingIntent.getActivity(this, 0,
        new Intent(Intent.ACTION_VIEW).setData(Uri.fromFile(destination)),
        PendingIntent.FLAG_ONE_SHOT));

    Intent shareIntent =
        new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, Uri.fromFile(destination));

    builder.addAction(0, "Share",
        PendingIntent.getActivity(this, 0, Intent.createChooser(shareIntent, "Share via"), 0));

    return builder;
  }

  static class AttachmentRequest {
    SubjectReportExtended attachment;
    File destination;

    public AttachmentRequest(Intent intent) {
      if (!intent.hasExtra(EXTRA_ASSIGNMENT_SUBJECT)) {
        throw new NullPointerException("attachment is not given");
      }

      attachment = intent.getParcelableExtra(EXTRA_ASSIGNMENT_SUBJECT);
      destination = new File(intent.getStringExtra(EXTRA_DESTINATION));
    }
  }
}
