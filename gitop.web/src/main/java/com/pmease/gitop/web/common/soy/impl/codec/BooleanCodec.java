package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.restricted.BooleanData;
import com.pmease.gitop.web.util.Classes;

class BooleanCodec extends BasicTypeCodec<Boolean> {

  @Override
  public Boolean decode(SoyData data) {
    return data.booleanValue();
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return Classes.isAssignableFrom(BooleanData.class, type);
  }

//  @Override
//  public boolean canHandle(Class<?> clazz) {
//    return Classes.isAssignableFrom(Boolean.class, clazz);
//  }

}
