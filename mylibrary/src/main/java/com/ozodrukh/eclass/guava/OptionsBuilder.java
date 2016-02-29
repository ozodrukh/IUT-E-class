package com.ozodrukh.eclass.guava;

import android.util.ArrayMap;
import java.util.Map;

public class OptionsBuilder<K, V>{
  private ArrayMap<K, V> options;

  public OptionsBuilder() {
    options = new ArrayMap<>();
  }

  public OptionsBuilder<K, V> put(K key, V value){
    options.put(key, value);
    return this;
  }

  public Map<K, V> build(){
    return options;
  }
}