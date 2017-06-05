package com.gitplex.server.util.editable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserChoice {
	
	public enum Type{ALL, PROJECT_READER, PROJECT_WRITER, PROJECT_ADMINISTRATOR} 
	
	Type type() default Type.ALL;
}
