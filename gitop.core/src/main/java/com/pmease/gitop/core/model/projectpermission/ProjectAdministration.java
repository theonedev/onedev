package com.pmease.gitop.core.model.projectpermission;


/**
 * Project administrators can do anything inside a project.
 * 
 * @author robin
 *
 */
public class ProjectAdministration implements WholeProjectOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof ProjectAdministration 
				|| new PushToProject().can(operation);
	}

}
