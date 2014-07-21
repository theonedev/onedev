package com.pmease.gitplex.web.common.datatype;

import java.util.Locale;

import org.apache.commons.validator.routines.LongValidator;

public class LongType extends AbstractNumericType {

  @Override
  public String asString(Object from, String pattern, Locale locale) {
    Long l = (Long) typeCast(from);
    return LongValidator.getInstance().format(l, pattern, locale);
  }

  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    return LongValidator.getInstance().validate(value, pattern, locale);
  }

  @Override
  public Class<?> getReturnClass() {
    return Long.class;
  }

  @Override
  public Object typeCast(Object from) {
    return toNumber(from).longValue();
  }

  @Override
  public String getTypeName() {
    return "LONG";
  }

}
