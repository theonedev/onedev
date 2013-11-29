package com.pmease.gitop.web.common.soy.impl.codec;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.data.restricted.BooleanData;
import com.google.template.soy.data.restricted.FloatData;
import com.google.template.soy.data.restricted.IntegerData;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.StringData;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;
import com.pmease.gitop.web.common.soy.api.SoyDataMapper;

@Singleton
@SuppressWarnings("rawtypes")
class DefaultSoyDataCodecFactory implements SoyDataCodecFactory {
  final Set<SoyDataCodec> codecs;
  final Injector injector;
  
  @Inject
  DefaultSoyDataCodecFactory(Injector injector, Set<SoyDataCodec> codecs) {
    this.codecs = codecs;
    this.injector = injector;
  }

  @Override
  public SoyDataCodec<?> getCodec(Object obj) {
    if (obj == null) {
      return injector.getInstance(NullValueCodec.class);
    }
    
    return getCodec(obj.getClass());
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public SoyDataCodec<?> getCodec(Class<?> clazz) {
    for (SoyDataCodec each : codecs) {
      if (each.canEncode(clazz)) {
        return each;
      }
    }
    
    if (clazz.isAnnotationPresent(SoyDataMapper.class)) {
      SoyDataMapper mapper = clazz.getAnnotation(SoyDataMapper.class);
      return injector.getInstance(mapper.codec());
    }
    
    return getLazyObjectCodec();
  }
  
  private SoyDataCodec getLazyObjectCodec() {
    return injector.getInstance(PojoCodec.class);
  }
  
  @Override
  public SoyDataCodec<?> getCodec(SoyData data) {
    if (data == null || data == NullData.INSTANCE) {
      return injector.getInstance(NullValueCodec.class);
    }
    
    if (data instanceof LazySoyMapData) {
      return getLazyObjectCodec();
    } else if (data instanceof SoyMapData) {
      return injector.getInstance(MapCodec.class);
    } else if (data instanceof SoyListData) {
      return injector.getInstance(ListCodec.class);
    } else if (data instanceof EnumData) {
      return injector.getInstance(EnumCodec.class);
    } else if (data instanceof StringData) {
      return injector.getInstance(StringCodec.class);
    } else if (data instanceof BooleanData) {
      return injector.getInstance(BooleanCodec.class);
    } else if (data instanceof IntegerData) {
      return injector.getInstance(IntegerCodec.class);
    } else if (data instanceof FloatData) {
      return injector.getInstance(DoubleCodec.class);
    } else {
      throw new UnsupportedOperationException("Unknown soy data [" + data + "]");
    }
  }
}