package com.pmease.gitop.web.common.datatype;

import java.util.Locale;

import org.apache.commons.validator.routines.ByteValidator;

public class ByteType extends AbstractNumericType {

  @Override
  public String asString(Object from, String pattern, Locale locale) {
    Byte b = (Byte) typeCast(from);
    return ByteValidator.getInstance().format(b, pattern, locale);
  }

  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    return ByteValidator.getInstance().validate(value, pattern, locale);
  }

  @Override
  public Object typeCast(Object from) {
    Number number = toNumber(from);
    return number.byteValue();
  }
  
  @Override
  public Class<?> getReturnClass() {
    return Byte.class;
  }

  @Override
  public String getTypeName() {
    return "BYTE";
  }

}
