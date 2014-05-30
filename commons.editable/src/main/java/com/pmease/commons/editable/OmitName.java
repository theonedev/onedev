package com.pmease.commons.editable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OmitName {
	
	public enum Place {ALL, EDITOR, VIEWER};
	
	Place value() default Place.ALL;
}
