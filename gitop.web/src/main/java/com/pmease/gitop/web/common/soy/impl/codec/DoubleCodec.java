package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.restricted.FloatData;
import com.pmease.gitop.web.util.Classes;

class DoubleCodec extends BasicTypeCodec<Double> {

  @Override
  public Double decode(SoyData data) {
    return data.floatValue();
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return Classes.isAssignableFrom(FloatData.class, type);
  }
}
