package com.pmease.gitplex.web.common.datatype;

import java.util.Locale;

public interface DataType {

  String asString(Object from, String pattern, Locale locale);
  
  String asString(Object from, String pattern);
  
  String asString(Object from);
  
  Object fromString(String value, String pattern, Locale locale);
  
  Object fromString(String value, String pattern);
  
  Object fromString(String value);
  
  Class<?> getReturnClass();
  
  boolean isNumericType();
  
  Object typeCast(Object from);
  
  String getTypeName();
}
