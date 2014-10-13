package com.pmease.gitplex.core.editable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BranchChoice {

	public enum Scope {
		GLOBAL, // choose from all accessible branches in the system  
		AFFINAL, // choose from accessible branches with forking relationship with current repository  
		LOCAL // choose from branches in current repository
	};
	
	Scope value() default Scope.LOCAL; 
}
