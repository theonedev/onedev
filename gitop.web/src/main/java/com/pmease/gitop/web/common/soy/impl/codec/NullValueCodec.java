package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;

class NullValueCodec extends BasicTypeCodec<Object> {

  @Override
  public Object decode(SoyData data) {
    return null;
  }

  @Override
  public boolean canEncode(Class<?> clazz) {
    return false;
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return false;
  }

}
