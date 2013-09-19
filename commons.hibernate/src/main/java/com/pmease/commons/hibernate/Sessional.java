/*
 * Copyright PMEase (c) 2005-2008,
 * Date: Feb 24, 2008
 * Time: 4:29:05 PM
 * All rights reserved.
 * 
 * Revision: $Id: Transactional.java 1209 2008-07-28 00:16:18Z robin $
 */
package com.pmease.commons.hibernate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sessional {
	boolean transactional() default false;
}
