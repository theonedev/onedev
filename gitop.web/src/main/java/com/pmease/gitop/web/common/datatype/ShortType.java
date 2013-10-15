package com.pmease.gitop.web.common.datatype;

import java.util.Locale;

import org.apache.commons.validator.routines.ShortValidator;

import com.google.common.base.Strings;

public class ShortType extends AbstractNumericType {

  @Override
  public String asString(Object from, String pattern, Locale locale) {
    Short s = (Short) typeCast(from);
    return ShortValidator.getInstance().format(s, pattern, locale);
  }

  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    if (Strings.isNullOrEmpty(value)) {
      return 0;
    }
    
    return ShortValidator.getInstance().validate(value, pattern, locale);
  }

  @Override
  public Class<?> getReturnClass() {
    return Short.class;
  }

  @Override
  public Object typeCast(Object from) {
    return toNumber(from).shortValue();
  }

  @Override
  public String getTypeName() {
    return "SHORT";
  }

}
