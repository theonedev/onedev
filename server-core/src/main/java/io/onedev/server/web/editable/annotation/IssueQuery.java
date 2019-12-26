package io.onedev.server.web.editable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.util.validation.IssueQueryValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=IssueQueryValidator.class) 
public @interface IssueQuery {
	boolean withCurrentUserCriteria();
	
	boolean withCurrentBuildCriteria();
	
	boolean withCurrentPullRequestCriteria();
	
	boolean withCurrentCommitCriteria();
	
	boolean withOrder() default true;
	
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
