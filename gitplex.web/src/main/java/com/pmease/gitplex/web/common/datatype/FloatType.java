package com.pmease.gitplex.web.common.datatype;

import java.util.Locale;

import org.apache.commons.validator.routines.FloatValidator;

public class FloatType extends AbstractNumericType {

  @Override
  public String asString(Object from, String pattern, Locale locale) {
    Float f = (Float) typeCast(from);
    return FloatValidator.getInstance().format(f, pattern, locale);
  }

  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    return FloatValidator.getInstance().validate(value, pattern, locale);
  }

  @Override
  public Class<?> getReturnClass() {
    return Float.class;
  }

  @Override
  public Object typeCast(Object from) {
    return toNumber(from).floatValue();
  }

  @Override
  public String getTypeName() {
    return "FLOAT";
  }

}
