package com.ozodrukh.instant.eclass.reports;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

public class BaseHolder<Entry> extends RecyclerView.ViewHolder {

  protected ViewDataBinding dataBinding;

  public BaseHolder(ViewDataBinding binder) {
    super(binder.getRoot());

    dataBinding = binder;
  }

  public void setEntry(int position, Entry entry){

  }

}
