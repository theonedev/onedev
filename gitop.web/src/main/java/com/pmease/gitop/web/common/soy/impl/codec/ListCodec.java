package com.pmease.gitop.web.common.soy.impl.codec;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyListData;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;
import com.pmease.gitop.web.util.Classes;

@SuppressWarnings("rawtypes")
public class ListCodec implements SoyDataCodec<Iterable> {

  final SoyDataCodecFactory codecs;
  
  @Inject
  ListCodec(SoyDataCodecFactory codecs) {
    this.codecs = codecs;
  }

  @SuppressWarnings("unchecked")
  @Override
  public SoyData encode(Iterable list) {
    SoyListData listData = new SoyListData();
    for (Object o : list) {
      SoyDataCodec codec = codecs.getCodec(o);
      listData.add(codec.encode(o));
    }
    
    return listData;
  }

  @Override
  public Iterable decode(SoyData data) {
    if (data instanceof SoyListData) {
      return Lists.transform(((SoyListData) data).asList(), new Function<SoyData, Object>() {
        @Override
        public Object apply(SoyData from) {
          return codecs.getCodec(from).decode(from);
        }
      });
    }
    
    throw new UnsupportedOperationException("from [" + data + "] to list");
  }

  @Override
  public boolean canEncode(Class<?> clazz) {
    return Classes.isAssignableFrom(Iterable.class, clazz);
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return Classes.isAssignableFrom(SoyListData.class, type);
  }
}
