package com.pmease.gitplex.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.jgit.lib.FileMode;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathChoice {

	int[] value() default FileMode.TYPE_FILE;
	
}
