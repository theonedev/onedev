package io.onedev.server.rest.jersey;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = ValidQueryParamsValidator.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidQueryParams {
    String message() default "{org.hibernate.validator.referenceguide.chapter06." +
            "crossparameter.ConsistentDateParameters.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
