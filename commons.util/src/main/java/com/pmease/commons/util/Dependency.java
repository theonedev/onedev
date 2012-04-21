package com.pmease.commons.util;

import java.util.Set;

public interface Dependency {
	
	/**
	 * Get identifier of the dependent object.
	 * @return
	 */
	String getId();
	
	/**
	 * Get set of identifiers of other dependency objects this dependency object directly depends on. 
	 * @return 
	 * 			Should not be null. In case of no dependencies, please return an empty collection 
	 * 			instead of a null value. 
	 */
	Set<String> getDependencyIds();
	
}
