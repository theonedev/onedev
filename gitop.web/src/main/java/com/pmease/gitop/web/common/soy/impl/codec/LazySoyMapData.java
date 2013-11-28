package com.pmease.gitop.web.common.soy.impl.codec;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import org.apache.commons.beanutils.PropertyUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.data.restricted.NullData;
import com.pmease.gitop.web.common.soy.impl.SoyDataConverter;

class LazySoyMapData extends SoyMapData {
  private final LoadingCache<String, SoyData> cache;
  private final LazyReference<Object> delegate;

  public LazySoyMapData(final Callable<?> objectSource, final SoyDataConverter soyDataConverter) {
    super();
    cache = CacheBuilder.newBuilder().build(new CacheLoader<String, SoyData>() {
      @Override
      public SoyData load(final String from) {
        Object value;
        try {
          value = "class".equals(from) ? null : PropertyUtils.getProperty(delegate.get(), from);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
          throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
          value = null;
        }
        return soyDataConverter.toSoyData(value);
      }
    });

    this.delegate = new LazyReference<Object>() {
      @Override
      protected Object create() throws Exception {
        return objectSource.call();
      }
    };
  }

  public Object getDelegate() {
    return delegate.get();
  }

  @Override
  public SoyData getSingle(String key) {
    SoyData soyData = super.getSingle(key);
    if (soyData != null && soyData != NullData.INSTANCE) {
      return soyData;
    } else {
      return cache.getUnchecked(key);
    }
  }

  @Override
  public String toString() {
    return getDelegate().toString();
  }

}
