package com.pmease.gitop.web.common.soy.api;

import com.google.template.soy.data.SoyData;

/**
 * Encode/decode the data to/from the soy data. The concrete implementations should be thread-safe.
 * The concrete implementation should be registered to guice.
 * 
 */
public interface SoyDataCodec<T> {

  /**
   * Encode an object to soy data
   * 
   * @param object
   * @return encoded soy data
   */
  SoyData encode(T object);
  
  /**
   * Decode the soy data to an object
   * 
   * @param data
   * @return decoded object
   */
  T decode(SoyData data);
  
  /**
   * Returns whether this codec can encode the type or not
   * 
   * @param type
   * @return
   */
  boolean canEncode(Class<?> type);
  
  /**
   * Returns whether this codec can decode the type or not
   * 
   * @param type
   * @return
   */
  boolean canDecode(Class<? extends SoyData> type);
}