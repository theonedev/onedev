package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;
import com.pmease.gitop.web.util.Classes;
import com.pmease.gitop.web.util.Generics;

abstract class BasicTypeCodec<T> implements SoyDataCodec<T> {

  final Class<T> clazz;
  
  @SuppressWarnings("unchecked")
  BasicTypeCodec() {
    clazz = (Class<T>) Generics.getTypeParameter(getClass());
  }
  
  @Override
  public SoyData encode(T object) {
    return SoyData.createFromExistingData(object);
  }
  
  @Override
  public boolean canEncode(Class<?> kls) {
    return Classes.isAssignableFrom(clazz, kls);
  }
}
