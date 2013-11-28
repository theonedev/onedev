package com.pmease.gitop.web.common.soy.impl;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.parseinfo.SoyTemplateInfo;
import com.google.template.soy.shared.SoyCssRenamingMap;
import com.google.template.soy.tofu.SoyTofu;
import com.pmease.gitop.web.common.soy.api.SoyTemplateRenderer;
import com.pmease.gitop.web.common.soy.api.TemplateKey;
import com.pmease.gitop.web.util.UrlUtils;

@Singleton
public class SoyManager implements SoyTemplateRenderer {
  private static final SoyTofu DIDNOTCOMPILE = new NullTofu();

  static class TofuInfo {
    final SoyTofu tofu;
    final long lastModified;
    
    TofuInfo(SoyTofu tofu, long lastModified) {
      this.tofu = tofu;
      this.lastModified = lastModified;
    }
  }

  private final LoadingCache<String, TofuInfo> tofuCache;

  private final Injector injector;
  private final Stage stage;
  private final SoyFileSetSupplier filesets;
  private final SoyDataConverter converter; 
  private static final String DEFAULT_TOFU = SoyManager.class.getName() + ".tofu";

  @Inject
  SoyManager(final Injector injector,
             final Stage stage, 
             final SoyFileSetSupplier filesets, 
             final SoyDataConverter converter) {
    this.injector = injector;
    this.stage = stage;
    this.filesets = filesets;
    this.converter = converter;
    
    tofuCache = CacheBuilder.newBuilder().build(new CacheLoader<String, TofuInfo>() {

      @Override
      public TofuInfo load(String key) throws Exception {
        SoyFileSet.Builder builder = getSoyFileSetBuilder();
        Set<URL> urls = filesets.get();
        long lastModified = UrlUtils.getLastModified(urls);
        for (URL each : urls) {
          builder.add(each);
        }
        
        SoyTofu tofu = builder.build().compileToTofu();
        if (tofu != null) {
          return new TofuInfo(tofu, lastModified);
        } else {
          return new TofuInfo(DIDNOTCOMPILE, -1);
        }
      }
    });
  }

  private SoyFileSet.Builder getSoyFileSetBuilder() {
    return injector.getInstance(SoyFileSet.Builder.class);
  }
  
  private SoyTofu.Renderer getRenderer(TemplateKey key) {
    if (isDev()) {
      updateTofuCache(DEFAULT_TOFU);
    }
    
    SoyTofu tofu = tofuCache.getUnchecked(DEFAULT_TOFU).tofu;
    if (tofu == null) {
      tofu = DIDNOTCOMPILE;
    }
    
    return tofu.newRenderer(key.toString());
  }
  
  private void updateTofuCache(String key) {
    Set<URL> urls = filesets.get();
    long lastModified = UrlUtils.getLastModified(urls);
    TofuInfo info = tofuCache.getUnchecked(key);
    if (info.lastModified < lastModified || lastModified <= 0) {
      tofuCache.invalidate(key);
    }
  }
  
  private boolean isDev() {
    return stage == Stage.DEVELOPMENT;
  }
  
  @Override
  public void render(TemplateKey key, Appendable out, Map<String, ?> data, Map<String, ?> ijData) {
    SoyTofu.Renderer renderer = getRenderer(key);
    renderer.setData((SoyMapData) converter.toSoyData(data))
            .setIjData((SoyMapData) converter.toSoyData(ijData))
            .render(out);
  }

  @Override
  public void render(TemplateKey key, Appendable out, Map<String, ?> data) {
    render(key, out, data, Collections.<String, Object>emptyMap());
  }

  /*
   * Placeholder SoyTofu implementation to go into ConcurrentMap that can't take null.
   */
  private static class NullTofu implements SoyTofu {
    @Override
    public String getNamespace() {
      return null;
    }

    @Override
    public SoyTofu forNamespace(String namespace) {
      return null;
    }

    @Override
    public String render(SoyTemplateInfo templateInfo, Map<String, ?> data, SoyMsgBundle msgBundle) {
      return null;
    }

    @Override
    public String render(SoyTemplateInfo templateInfo, SoyMapData data, SoyMsgBundle msgBundle) {
      return null;
    }

    @Override
    public String render(String templateName, Map<String, ?> data, SoyMsgBundle msgBundle) {
      return null;
    }

    @Override
    public String render(String templateName, SoyMapData data, SoyMsgBundle msgBundle) {
      return null;
    }

    @Override
    public boolean isCaching() {
      return false;
    }

    @Override
    public Renderer newRenderer(SoyTemplateInfo soyTemplateInfo) {
      return null;
    }

    @Override
    public Renderer newRenderer(String s) {
      return null;
    }

    @Override
    public void addToCache(@Nullable SoyMsgBundle msgBundle,
        @Nullable SoyCssRenamingMap cssRenamingMap) {

    }

    @Override
    public ImmutableSortedSet<String> getUsedIjParamsForTemplate(SoyTemplateInfo templateInfo) {
      return null;
    }

    @Override
    public ImmutableSortedSet<String> getUsedIjParamsForTemplate(String templateName) {
      return null;
    }
  }
}
