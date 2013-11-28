package com.pmease.gitop.web.common.soy.impl.codec;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.google.common.util.concurrent.Callables;
import com.google.template.soy.data.SoyData;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;
import com.pmease.gitop.web.common.soy.impl.SoyDataConverter;
import com.pmease.gitop.web.util.Classes;

public class PojoCodec implements SoyDataCodec<Object> {

  final SoyDataConverter convert;
  
  @Inject
  PojoCodec(SoyDataConverter convert) {
    this.convert = convert;
  }
  
  @Override
  public SoyData encode(Object object) {
    return new LazySoyMapData(makeCallable(object), convert);
  }

  private Callable<?> makeCallable(final Object value) {
    if (value instanceof Callable) {
      return (Callable<?>) value;
    }

    return Callables.returning(value);
  }

  @Override
  public Object decode(SoyData data) {
    if (data instanceof LazySoyMapData) {
      return ((LazySoyMapData) data).getDelegate();
    }
    
    throw new UnsupportedOperationException("from [" + data + "] to object");
  }

  @Override
  public boolean canEncode(Class<?> clazz) {
    // Always return false, we'll handle pojo case manually, see
    // {@class DefaultSoyDataCodecFactory}
    return false;
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return Classes.isAssignableFrom(LazySoyMapData.class, type);
  }
}
