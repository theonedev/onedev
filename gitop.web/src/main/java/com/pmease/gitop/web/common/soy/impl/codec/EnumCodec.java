package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;
import com.pmease.gitop.web.util.Classes;

@SuppressWarnings("rawtypes")
class EnumCodec implements SoyDataCodec<Enum>{

  @Override
  public SoyData encode(Enum object) {
    return new EnumData(object);
  }

  @Override
  public Enum decode(SoyData data) {
    if (data instanceof EnumData) {
      return ((EnumData) data).getValue();
    }
    
    throw new UnsupportedOperationException("Unable to convert soydata [" + data + "] to enum");
  }

  @Override
  public boolean canEncode(Class<?> clazz) {
    return clazz.isEnum() || Enum.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return Classes.isAssignableFrom(EnumData.class, type);
  }

}
