package com.pmease.gitop.core.model.permission.account;

/**
 * This interface stands for an operation requires permissions. 
 *
 */
public interface PrivilegedOperation {
	/**
	 * Whether or not if an user has permission to perform current operation 
	 * can also perform specified operation.
	 * 
	 * @param operation
	 * 			the operation to check against
	 * @return
	 * 			true if user has permission to perform current operation can also 
	 * 			perform specified operation; false otherwise 
	 */
	boolean can(PrivilegedOperation operation);
}
