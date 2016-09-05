package com.pmease.gitplex.web;

import java.util.Collection;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ExpectedExceptionContribution {
	Collection<Class<? extends Exception>> getExpectedExceptionClasses(); 
}
