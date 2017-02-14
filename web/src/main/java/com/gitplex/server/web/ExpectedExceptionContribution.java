package com.gitplex.server.web;

import java.util.Collection;

import com.gitplex.calla.loader.ExtensionPoint;

@ExtensionPoint
public interface ExpectedExceptionContribution {
	Collection<Class<? extends Exception>> getExpectedExceptionClasses(); 
}
