package com.ozodrukh.instant.eclass;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.trello.rxlifecycle.components.support.RxDialogFragment;

public class BaseFragment extends RxDialogFragment{

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return onCreateView(inflater, container);
  }

  /**
   * Called inside {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
   * to wrap content with sliding view feature
   *
   * @param inflater The LayoutInflater object that can be used to inflate
   * any views in the fragment,
   * @param parent If non-null, this is the parent view that the fragment's
   * UI should be attached to.  The fragment should not add the view itself,
   * but this can be used to generate the LayoutParams of the view.
   *
   * @return Return the View for the fragment's UI, or null.
   */
  protected View onCreateView(LayoutInflater inflater, ViewGroup parent){
    return null;
  }

}
