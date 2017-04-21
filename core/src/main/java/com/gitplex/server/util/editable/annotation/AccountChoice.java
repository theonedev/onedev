package com.gitplex.server.util.editable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountChoice {
	
	public enum Type{ORGANIZATION, USER, DEPOT_READER, DEPOT_WRITER, DEPOT_ADMINISTRATOR} 
	
	Type type() default Type.USER;
}
