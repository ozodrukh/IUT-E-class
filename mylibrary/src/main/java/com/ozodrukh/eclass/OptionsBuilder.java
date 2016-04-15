package com.ozodrukh.eclass;

import java.util.HashMap;
import java.util.Map;

public class OptionsBuilder<K, V>{
  private Map<K, V> options;

  public OptionsBuilder() {
    options = new HashMap<>();
  }

  public OptionsBuilder<K, V> put(K key, V value){
    options.put(key, value);
    return this;
  }

  public Map<K, V> build(){
    return options;
  }
}