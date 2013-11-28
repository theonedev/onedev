package com.pmease.gitop.web.common.soy.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.template.soy.data.SoyData;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;
import com.pmease.gitop.web.common.soy.impl.codec.SoyDataCodecFactory;

@Singleton
class DefaultSoyDataConverter implements SoyDataConverter {

  private final SoyDataCodecFactory codecs;

  @Inject
  DefaultSoyDataConverter(SoyDataCodecFactory codecs) {
    this.codecs = codecs;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public SoyData toSoyData(Object obj) {
    SoyDataCodec codec = codecs.getCodec(obj);
    return codec.encode(obj);
  }

  @Override
  public Object fromSoyData(SoyData data) {
    SoyDataCodec<?> codec = codecs.getCodec(data);
    return codec.decode(data);
  }
}
