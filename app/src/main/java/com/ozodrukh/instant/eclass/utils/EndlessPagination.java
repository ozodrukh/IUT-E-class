package com.ozodrukh.instant.eclass.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.ozodrukh.eclass.Timber;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class EndlessPagination extends RecyclerView.OnScrollListener {
  public static final int THRESHOLD = 7;

  private LinearLayoutManager layout;
  private OnThresholdReachListener listener;

  private boolean receiveNotifications;
  private int threshold, page;

  public EndlessPagination(RecyclerView view, OnThresholdReachListener listener,
      boolean receiveNotifications, int threshold, int page) {
    if (view == null) {
      throw new NullPointerException("RecyclerView is null");
    }

    if (listener == null) {
      throw new NullPointerException("Listener is null");
    }

    this.page = page;
    this.listener = listener;
    this.threshold = threshold;
    this.layout = (LinearLayoutManager) view.getLayoutManager();
    this.receiveNotifications = receiveNotifications;

    view.addOnScrollListener(this);

    // zero page means that view requires to load first page
    if(page == 0) {
      // load first page
      onScrollStateChanged(null, SCROLL_STATE_IDLE);
    }
  }

  public EndlessPagination(RecyclerView view, OnThresholdReachListener listener) {
    this(view, listener, true, THRESHOLD, 0);
  }

  public void setReceiveNotifications(boolean sendNotifications) {
    this.receiveNotifications = sendNotifications;
    // try load next page if empty space is left
    onScrollStateChanged(null, SCROLL_STATE_IDLE);
  }

  public void setPage(RecyclerView recyclerView, int pageNumber){
    this.page = pageNumber;

    // zero page means that view requires to load first page
    if(page == 0) {
      // load first page
      onScrollStateChanged(recyclerView, SCROLL_STATE_IDLE);
    }
  }

  public boolean canSendNotifications() {
    return receiveNotifications;
  }

  public void onPageDidNotLoaded(){
    page -= 1;
  }

  public int getPage() {
    return page;
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public int getThreshold() {
    return threshold;
  }

  @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    if (!canSendNotifications()) {
      return;
    }

    if(layout == null){
      layout = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    Timber.d(
        "state = %d, threshold = %d, totalItemsCount = %d, firstVisibleIndex = %d,\nfirstCompletelyVisible=%s, "
            + "lastVisibleIndex = %d, lastCompletelyVisibleIndex=%s", newState, threshold,
        layout.getItemCount(), layout.findFirstVisibleItemPosition(),
        layout.findFirstCompletelyVisibleItemPosition(), layout.findLastVisibleItemPosition(),
        layout.findLastCompletelyVisibleItemPosition());

    if (threshold >= layout.getItemCount() - layout.findLastCompletelyVisibleItemPosition()) {
      receiveNotifications = listener.onThresholdReached(++page);
      Timber.d("is notification was used as is %s", receiveNotifications ? "NO" : "YES");
    }
  }

  public interface OnThresholdReachListener {
    /**
     * @param page page number
     * @return true to continue receive notifications of reaching threshold,
     * otherwise false
     */
    boolean onThresholdReached(int page);
  }
}