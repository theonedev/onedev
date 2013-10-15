package com.pmease.gitop.web.common.datatype;

import java.util.Locale;

import org.apache.commons.validator.routines.DoubleValidator;

public class DoubleType extends AbstractNumericType {

  @Override
  public String asString(Object from, String pattern, Locale locale) {
    Double d = (Double) typeCast(from);
    return DoubleValidator.getInstance().format(d, pattern, locale);
  }

  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    return DoubleValidator.getInstance().validate(value, pattern, locale);
  }

  @Override
  public Class<?> getReturnClass() {
    return Double.class;
  }

  @Override
  public Object typeCast(Object from) {
    return toNumber(from).doubleValue();
  }

  @Override
  public String getTypeName() {
    return "DOUBLE";
  }

}
