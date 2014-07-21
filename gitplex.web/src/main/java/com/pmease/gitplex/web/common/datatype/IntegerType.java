package com.pmease.gitplex.web.common.datatype;

import java.util.Locale;

import org.apache.commons.validator.routines.IntegerValidator;

public class IntegerType extends AbstractNumericType {

  @Override
  public String asString(Object from, String pattern, Locale locale) {
    Integer i = (Integer) typeCast(from);
    return IntegerValidator.getInstance().format(i, pattern, locale);
  }

  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    return IntegerValidator.getInstance().validate(value, pattern, locale);
  }

  @Override
  public Class<?> getReturnClass() {
    return Integer.class;
  }

  @Override
  public Object typeCast(Object from) {
    return toNumber(from).intValue();
  }

  @Override
  public String getTypeName() {
    return "INTEGER";
  }

}
