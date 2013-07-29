package com.pmease.gitop.core.model.permission.account;

/**
 * This interface stands for an operation requires permissions. 
 *
 */
public interface PrivilegedOperation {
	/**
	 * Whether or not accounts with permission to perform current operation can perform 
	 * specified operation.  
	 */
	boolean can(PrivilegedOperation operation);
}
