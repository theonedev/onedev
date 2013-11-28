package com.pmease.gitop.web.common.soy.impl.codec;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyListData;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;

public class ArrayCodec implements SoyDataCodec<Object> {

  final Injector injector;
  
  @Inject
  ArrayCodec(Injector injector) {
    this.injector = injector;
  }
  
  @Override
  public SoyData encode(Object object) {
    List<Object> list = Lists.newArrayList((Object[]) object);
    
    return injector.getInstance(ListCodec.class).encode(list);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object decode(SoyData data) {
    if (data instanceof SoyListData) {
      List<Object> objects = (List<Object>) injector.getInstance(ListCodec.class).decode(data);
      return objects.toArray(new Object[objects.size()]);
    }
    
    throw new UnsupportedOperationException("from [" + data + "] to array");
  }

  @Override
  public boolean canEncode(Class<?> clazz) {
    return clazz.isArray();
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return false;
  }
}
