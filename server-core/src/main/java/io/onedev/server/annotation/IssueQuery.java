package io.onedev.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.validation.validator.IssueQueryValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=IssueQueryValidator.class) 
public @interface IssueQuery {
	boolean withProjectCriteria() default true;
	
	boolean withCurrentProjectCriteria() default false;
	
	boolean withCurrentUserCriteria() default false;

	boolean withCurrentIssueCriteria() default false;
	
	boolean withCurrentBuildCriteria() default false;
	
	boolean withCurrentPullRequestCriteria() default false;
	
	boolean withCurrentCommitCriteria() default false;
	
	boolean withStateCriteria() default true;
	
	boolean withOrder() default true;
	
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
