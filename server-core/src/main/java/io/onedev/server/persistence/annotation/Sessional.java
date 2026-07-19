package io.onedev.server.persistence.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation will not work for below cases:
 * <ul>
 * <li>Methods of restful resources (but resource methods already run inside session via other mechanisms)</li>
 * <li>Methods of private service mthods</li>
 * </ul>
 * Transactional
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sessional {
}
