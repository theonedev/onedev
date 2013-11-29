package com.pmease.gitop.web.common.soy.impl.codec;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import com.pmease.gitop.web.common.soy.api.SoyDataCodec;

public class SoyDataCodecModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SoyDataCodecFactory.class).to(DefaultSoyDataCodecFactory.class);
    
    requireBinding(SoyDataCodecFactory.class);
    register(binder(), BooleanCodec.class);
    register(binder(), DoubleCodec.class);
    register(binder(), EnumCodec.class);
    register(binder(), FloatCodec.class);
    register(binder(), IntegerCodec.class);
    register(binder(), ListCodec.class);
    register(binder(), ArrayCodec.class);
    register(binder(), PojoCodec.class);
    register(binder(), LongCodec.class);
    register(binder(), MapCodec.class);
    register(binder(), NullValueCodec.class);
    register(binder(), StringCodec.class);
  }
  
  public static void register(Binder binder, Class<? extends SoyDataCodec<?>> codecClass) {
    Multibinder.newSetBinder(binder, SoyDataCodec.class)
      .addBinding().to(codecClass).in(Singleton.class);
  }
}
