package com.pmease.gitop.web.common.soy.impl.codec;

import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.restricted.StringData;
import com.pmease.gitop.web.util.Classes;

class StringCodec extends BasicTypeCodec<String> {

  @Override
  public String decode(SoyData data) {
    return data.stringValue();
  }

  @Override
  public boolean canDecode(Class<? extends SoyData> type) {
    return Classes.isAssignableFrom(StringData.class, type);
  }
}
