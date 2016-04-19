package com.ozodrukh.instant.eclass.reports;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayAdapter<Entry, ViewHolder extends BaseHolder<Entry>>
    extends RecyclerView.Adapter<ViewHolder> {

  private List<Entry> entries;

  public ArrayAdapter(Entry[] entries){
    this();
    this.entries.addAll(Arrays.asList(entries));
  }

  public ArrayAdapter(){
    this(new ArrayList<Entry>());
  }

  public ArrayAdapter(List<Entry> entries) {
    this.entries = entries;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return null;
  }

  @Override public void onBindViewHolder(ViewHolder viewHolder, int position) {

  }

  /**
   * @return true if items in adapters list has 0 size, not by {@link #getItemCount()}
   */
  public boolean isEmpty() {
    return entries.isEmpty();
  }

  @Override public int getItemCount() {
    return entries.size();
  }
}
