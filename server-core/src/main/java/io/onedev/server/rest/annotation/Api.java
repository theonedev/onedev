package io.onedev.server.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Api {

	boolean exclude() default false;
	
	String exampleProvider() default "";
	
	String description() default "";
	
	int order() default 100;
	
	String permission() default "";
	
}
