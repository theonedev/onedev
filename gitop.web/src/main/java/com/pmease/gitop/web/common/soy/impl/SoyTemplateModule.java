package com.pmease.gitop.web.common.soy.impl;

import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.template.soy.SoyModule;
import com.google.template.soy.xliffmsgplugin.XliffMsgPluginModule;
import com.pmease.gitop.web.common.soy.api.SoyTemplateRenderer;
import com.pmease.gitop.web.common.soy.impl.codec.SoyDataCodecModule;
import com.pmease.gitop.web.common.soy.impl.functions.SoyFunctionsModule;

public class SoyTemplateModule extends AbstractModule {

  private final Set<String> autoConfigPackages;
  
  public SoyTemplateModule(Set<String> autoConfigPackages) {
    this.autoConfigPackages = ImmutableSet.<String>copyOf(autoConfigPackages);
  }
  
  @Override
  protected void configure() {
    install(new SoyModule());
    install(new XliffMsgPluginModule());
    
    install(new SoyDataCodecModule());
    install(new SoyFunctionsModule());
  
    bind(Key.get(new TypeLiteral<Set<String>>() {}, Names.named("CFG_SOY_PACKAGES")))
    .toInstance(autoConfigPackages);

    bind(SoyFileSetSupplier.class).to(ClasspathFileSetSupplier.class);
    bind(SoyTemplateRenderer.class).to(SoyManager.class).in(Singleton.class);
    bind(SoyDataConverter.class).to(DefaultSoyDataConverter.class).in(Singleton.class);
  }

}
