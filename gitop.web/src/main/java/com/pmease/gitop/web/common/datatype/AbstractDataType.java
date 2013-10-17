package com.pmease.gitop.web.common.datatype;

import java.util.Locale;

public abstract class AbstractDataType implements DataType {

  @Override
  public String asString(Object from, String pattern) {
    return asString(from, pattern, Locale.getDefault());
  }

  @Override
  public String asString(Object from) {
    return asString(from, null);
  }

  @Override
  public Object fromString(String value, String pattern) {
    return fromString(value, pattern, Locale.getDefault());
  }

  @Override
  public Object fromString(String value) {
    return fromString(value, null);
  }

  @Override
  public boolean isNumericType() {
    return false;
  }
}
