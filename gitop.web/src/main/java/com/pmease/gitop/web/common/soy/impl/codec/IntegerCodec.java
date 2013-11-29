package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.restricted.IntegerData;
import com.pmease.gitop.web.util.Classes;

public class IntegerCodec extends BasicTypeCodec<Integer> {

  @Override
  public Integer decode(SoyData data) {
    return data.integerValue();
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return Classes.isAssignableFrom(IntegerData.class, type);
  }
}
