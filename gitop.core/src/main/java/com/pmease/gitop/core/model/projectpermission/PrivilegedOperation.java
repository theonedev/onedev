package com.pmease.gitop.core.model.projectpermission;

/**
 * This interface stands for an operation requires permissions. 
 *
 */
public interface PrivilegedOperation {
	/**
	 * Whether or not users with permission to perform current operation can perform 
	 * specified operation.  
	 */
	boolean can(PrivilegedOperation operation);
}
