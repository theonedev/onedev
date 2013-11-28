package com.pmease.gitop.web.common.soy.impl;

import com.google.template.soy.data.SoyData;

public interface SoyDataConverter {
  
  SoyData toSoyData(Object obj);
  
  Object fromSoyData(SoyData data);
  
}
