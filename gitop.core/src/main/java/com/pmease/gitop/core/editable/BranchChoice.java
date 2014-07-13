package com.pmease.gitop.core.editable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BranchChoice {

	public enum Scope {AFFINAL, LOCAL};
	
	Scope value() default Scope.LOCAL; 
}
