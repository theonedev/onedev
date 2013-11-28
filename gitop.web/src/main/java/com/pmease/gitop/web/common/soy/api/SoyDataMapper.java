package com.pmease.gitop.web.common.soy.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as requiring a custom mapper to convert it to SoyData. 
 * 
 * @since 1.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SoyDataMapper {
  @SuppressWarnings("rawtypes")
  Class<? extends SoyDataCodec> codec();
}