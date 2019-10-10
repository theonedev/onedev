package io.onedev.server.util.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.util.validation.DnsNameValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=DnsNameValidator.class) 
public @interface DnsName {

	boolean interpolative() default false;
	
    String message() default "can only contain alphanumberic characters or '-', and can only "
			+ "start and end with alphanumeric characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
