package com.gitplex.commons.util;

import java.util.Set;

public interface DependencyAware<K> {
	
	/**
	 * Get key of the dependent object.
	 * @return
	 */
	K getId();
	
	/**
	 * Get set of keys of other dependency objects this dependency object directly depends on. 
	 * @return 
	 * 			Should not be null. In case of no dependencies, please return an empty collection 
	 * 			instead of a null value. 
	 */
	Set<K> getDependencies();
	
}
