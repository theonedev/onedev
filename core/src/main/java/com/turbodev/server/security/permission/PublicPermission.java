package com.turbodev.server.security.permission;

import org.apache.shiro.authz.Permission;

import com.turbodev.server.security.ProjectPrivilege;

public class PublicPermission implements Permission {

	@Override
	public boolean implies(Permission p) {
		if (p instanceof ProjectPermission) {
			ProjectPermission projectPermission = (ProjectPermission) p;
			return projectPermission.getProject().isPublicRead() 
					&& projectPermission.getPrivilege() == ProjectPrivilege.READ;
		} else {
			return p instanceof PublicPermission;
		}
	}

}
