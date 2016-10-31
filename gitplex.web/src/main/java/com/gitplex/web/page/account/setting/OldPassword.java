/*
 * Copyright GitPlex Inc.,
 * Date: 2008-2-28
 * All rights reserved.
 *
 * Revision: $Id: PathElement.java 1209 2008-07-28 00:16:18Z robin $
 */
package com.gitplex.web.page.account.setting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author robin
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=OldPasswordValidator.class) 
@interface OldPassword {

	String message() default "Old password does not match";
	
	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
