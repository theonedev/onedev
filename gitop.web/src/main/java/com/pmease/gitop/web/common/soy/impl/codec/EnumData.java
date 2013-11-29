package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.restricted.StringData;

class EnumData extends SoyData {

  private final Enum<?> value;

  public EnumData(Enum<?> value) {
    this.value = value;
  }

  public Enum<?> getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.name();
  }

  @Override
  public boolean toBoolean() {
    return true;
  }

  @Override
  public boolean equals(Object other) {
    return this == other 
        || (other instanceof EnumData && ((EnumData) other).getValue() == value)
        || (other instanceof StringData && value.name().equals(((StringData) other).getValue()));
  }
}
