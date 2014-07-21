package com.pmease.gitplex.web.common.datatype;

import java.util.Locale;

public class StringType extends AbstractDataType {

  @Override
  public String asString(Object from, String pattern, Locale locale) {
    if (from == null || (from instanceof String)) {
      return (String) from;
    }
    
    return from.toString();
  }

  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    return value;
  }

  @Override
  public Class<?> getReturnClass() {
    return String.class;
  }

  @Override
  public Object typeCast(Object from) {
    if (from == null || (from instanceof String)) {
      return from;
    }
    
    return from.toString();
  }

  @Override
  public String getTypeName() {
    return "STRING";
  }

}
