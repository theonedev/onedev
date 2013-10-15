package com.pmease.gitop.web.common.datatype;

import java.util.Locale;

import org.apache.commons.validator.routines.BigDecimalValidator;

public abstract class AbstractNumericType extends AbstractDataType {

  @Override
  public boolean isNumericType() {
    return true;
  }

  protected Number toNumber(Object from) {
    if (from == null) {
      return 0;
    }
    
    if (from instanceof Number) {
      return (Number) from;
    }
    
    if (from instanceof String) {
      String s = (String) from;
      if ("NaN".equalsIgnoreCase(s)) {
        return Double.NaN;
      }
      
      return BigDecimalValidator.getInstance().validate(s, null, Locale.getDefault());
    }
    
    if (from instanceof Boolean) {
      return ((Boolean) from) ? 1 : 0;
    }
    
    throw new TypeCastException(this, from);
  }
}
