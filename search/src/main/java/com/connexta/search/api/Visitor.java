package com.connexta.search.api;

public interface Visitor {
  void addAttribute(String attribute);
  void addOperator(String operator);
  void addValue(String value);
  void addCombiner(Combiner combiner);
}
