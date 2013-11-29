package com.pmease.gitop.web.common.soy.impl.codec;

import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyMapData;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;
import com.pmease.gitop.web.util.Classes;

@SuppressWarnings("rawtypes")
class MapCodec implements SoyDataCodec<Map>{

  final SoyDataCodecFactory codecs;
  
  @Inject
  MapCodec(SoyDataCodecFactory codecs) {
    this.codecs = codecs;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public SoyData encode(Map map) {
    SoyMapData soyMapData = new SoyMapData();
    for (Object obj : map.entrySet()) {
      Map.Entry entry = (Map.Entry) obj;
      String key = entry.getKey().toString();
      Object value = entry.getValue();
      SoyDataCodec codec = codecs.getCodec(value);
      SoyData soyValue = codec.encode(value);
      soyMapData.put(key, soyValue);
    }
    
    return soyMapData;
  }

  @Override
  public Map decode(SoyData data) {
    if (data instanceof SoyMapData) {
      return Maps.transformValues(((SoyMapData) data).asMap(), new Function<SoyData, Object>() {

        @Override
        public Object apply(SoyData input) {
          return codecs.getCodec(input).decode(input);
        }
      });
    }
    
    throw new UnsupportedOperationException("from data [" + data + "] to map");
  }

  @Override
  public boolean canEncode(Class<?> clazz) {
    return Classes.isAssignableFrom(Map.class, clazz);
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return Classes.isAssignableFrom(SoyMapData.class, type);
  }
}
