/*
 * Copyright OneDev (c) 2005-2008,
 * Date: Feb 24, 2008
 * Time: 4:29:05 PM
 * All rights reserved.
 * 
 * Revision: $Id: Multiline.java 1209 2008-07-28 00:16:18Z robin $
 */
package io.onedev.server.web.editable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Multiline {
}