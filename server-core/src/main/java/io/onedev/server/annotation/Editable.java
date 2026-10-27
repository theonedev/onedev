package io.onedev.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Editable {

	/**
	 * Property group name. The bean class may define a static, no-argument
	 * {@code Map<String, String> getGroupDescriptions()} method to supply descriptions
	 * displayed below group names in the bean editor. Map keys are the original
	 * group names; descriptions should handle translation if needed.
	 */
	String group() default "";
	
	String icon() default "";
	
	String name() default "";
	
	String placeholder() default "";
	
	String placeholderProvider() default "";
	
	String topPlaceholder() default "";
	
	String topPlaceholderProvider() default "";
	
	boolean displayPlaceholderAsValue() default false;

	String autocomplete() default "";
	
	int order() default 0;
	
	String description() default "";
	
	String descriptionProvider() default "";

	boolean translatable() default true;
	
	boolean hidden() default false;
}
