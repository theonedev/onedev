package com.pmease.gitplex.web.common.datatype;

import java.util.Locale;

@SuppressWarnings("rawtypes")
public class EnumType extends AbstractDataType {

  private final Class<? extends Enum> enumClass;
  
  public EnumType(Class<? extends Enum> enumClass) {
    this.enumClass = enumClass;
  }
  
  public static EnumType of(Class<? extends Enum> clazz) {
    return new EnumType(clazz);
  }
  
  @Override
  public String asString(Object from, String pattern, Locale locale) {
    Enum<?> e = (Enum<?>) typeCast(from);
    if (e == null) {
      return null;
    } else {
      return e.toString();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object fromString(String value, String pattern, Locale locale) {
    return Enum.valueOf(enumClass, value);
  }

  @Override
  public Class<?> getReturnClass() {
    return Enum.class;
  }

  @Override
  public Object typeCast(Object from) {
    if (from == null || from instanceof Enum) {
      return from;
    }
    
    if (from instanceof String) {
      return fromString((String) from);
    }
    
    if (from instanceof Integer) {
      int i = (Integer) from;
      return enumClass.getEnumConstants()[i];
    }
    
    throw new TypeCastException(this, from);
  }

  @Override
  public String getTypeName() {
    return "ENUM";
  }

}
