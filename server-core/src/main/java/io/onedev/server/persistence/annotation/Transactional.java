package io.onedev.server.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation will not work for below cases:
 * <ul>
 * <li>Methods of restful resources</li>
 * <li>Methods of private service mthods</li>
 * </ul>
 * Transactional
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Transactional {
}
