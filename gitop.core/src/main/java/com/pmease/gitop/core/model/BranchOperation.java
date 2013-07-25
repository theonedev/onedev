package com.pmease.gitop.core.model;

/**
 * This class represents operation against a certain repository branch. Typical 
 * branch operations include pull and push.
 *  
 * @author robin
 *
 */
public interface BranchOperation {
	/**
	 * Checks whether or not passed operation is a subset of current operation 
	 * as parameter.
	 * <p>
	 * This will be used to check if a certain operation instance should match a permission entry. 
	 * For instance, push operation with file path <code>src/com/example/Core.java</code> is a 
	 * subset of push operation with file path <code>src/&#42;&#42;/&#42;.java</code>
	 *    
	 * @param branchOperation
	 * 			the operation to be checked if it is a subset of current operation
	 * @return
	 * 			true if passed operation is a subset of current operation; false otherwise
	 */
	boolean implies(BranchOperation branchOperation);
}
