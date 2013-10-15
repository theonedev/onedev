package com.pmease.gitop.web.common.datatype;

import java.util.Locale;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class BooleanType extends AbstractDataType {

  @Override
  public String asString(Object from, String pattern, Locale locale) {
    Boolean b = (Boolean) typeCast(from);
    if (Strings.isNullOrEmpty(pattern)) {
      return b.toString();
    }
    
    String[] tokens = Iterables.toArray(Splitter.on(":").limit(2).omitEmptyStrings().split(pattern), String.class);
    return b ? tokens[0] : tokens[1];
  }

  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }
    
    if (Strings.isNullOrEmpty(pattern)) {
      if ("TRUE".equalsIgnoreCase(value)) {
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
    }
    
    String[] tokens = Iterables.toArray(Splitter.on(":").limit(2).omitEmptyStrings().split(pattern), String.class);
    if (Objects.equal(value, tokens[0])) {
      return Boolean.TRUE;
    } else {
      return Boolean.FALSE;
    }
  }

  @Override
  public Class<?> getReturnClass() {
    return Boolean.class;
  }

  @Override
  public Object typeCast(Object from) {
    if (from == null){
      return Boolean.FALSE;
    } 
    
    if (from instanceof Boolean) {
      return from;
    }
    
    if (from instanceof Number) {
      return ((Number) from).intValue() > 0;
    }
    
    if (from instanceof String) {
      if ("TRUE".equalsIgnoreCase((String) from)) {
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
    }
    
    throw new TypeCastException(this, from);
  }

  @Override
  public String getTypeName() {
    return "BOOLEAN";
  }

}
