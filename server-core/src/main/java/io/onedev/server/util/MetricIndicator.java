package io.onedev.server.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetricIndicator {
	
	String name() default "";
	
	int maxValue() default Integer.MIN_VALUE;
	
	int minValue() default Integer.MAX_VALUE;
	
	String valueFormatter() default "";
	
	String group() default "";
	
	String color() default "#8950FC";
	
	int order() default 100;
}
