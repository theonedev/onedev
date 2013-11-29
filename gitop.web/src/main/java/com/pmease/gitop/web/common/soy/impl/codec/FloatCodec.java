package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.restricted.FloatData;
import com.pmease.gitop.web.util.Classes;

class FloatCodec extends BasicTypeCodec<Float> {

  @Override
  public Float decode(SoyData data) {
    return new Float(data.floatValue());
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return Classes.isAssignableFrom(FloatData.class, type);
  }
}
