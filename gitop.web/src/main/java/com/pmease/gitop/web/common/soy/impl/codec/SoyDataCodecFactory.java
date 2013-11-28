package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;

public interface SoyDataCodecFactory {
  SoyDataCodec<?> getCodec(Object obj);
  SoyDataCodec<?> getCodec(Class<?> clazz);
  SoyDataCodec<?> getCodec(SoyData data);
}
