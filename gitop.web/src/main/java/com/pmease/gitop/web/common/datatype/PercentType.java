package com.pmease.gitop.web.common.datatype;

import java.util.Locale;

import org.apache.commons.validator.routines.PercentValidator;

public class PercentType extends AbstractNumericType {

  @Override
  public String asString(Object from, String pattern, Locale locale) {
    Double d = (Double) typeCast(from);
    return PercentValidator.getInstance().format(d, pattern, locale);
  }

  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    return PercentValidator.getInstance().validate(value, pattern, locale).doubleValue();
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
    return "PERCENT";
  }

}
