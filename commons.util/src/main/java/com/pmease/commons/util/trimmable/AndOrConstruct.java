package com.pmease.commons.util.trimmable;

import java.util.List;

/**
 * A simple interface for a And/Or data structure. 
 * <p>
 * @author robin
 *
 */
public interface AndOrConstruct {

	Object getSelf();
	
	List<?> getMembers();
	
}
