package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;
import com.pmease.gitop.web.util.Classes;

class LongCodec implements SoyDataCodec<Long> {

  @Override
  public SoyData encode(Long object) {
    if (object >= Integer.MIN_VALUE && object <= Integer.MAX_VALUE) {
      return SoyData.createFromExistingData(object.intValue());
    } else {
      return SoyData.createFromExistingData(object.doubleValue());
    }
  }

  @Override
  public Long decode(SoyData data) {
    return ((Number) data.numberValue()).longValue();
  }

  @Override
  public boolean canEncode(Class<?> clazz) {
    return Classes.isAssignableFrom(Long.class, clazz);
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return false;
  }
}
