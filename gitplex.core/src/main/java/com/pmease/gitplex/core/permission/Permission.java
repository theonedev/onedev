package com.pmease.gitplex.core.permission;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.object.ProtectedObject;
import com.pmease.gitplex.core.permission.object.SystemObject;
import com.pmease.gitplex.core.permission.operation.GeneralOperation;
import com.pmease.gitplex.core.permission.operation.PrivilegedOperation;
import com.pmease.gitplex.core.permission.operation.SystemOperation;

/**
 * This class represents permissions to operate an account and its belongings.
 *  
 * @author robin
 *
 */
public class Permission implements org.apache.shiro.authz.Permission {
	
	private ProtectedObject object;
	
	private PrivilegedOperation operation;

	public Permission(ProtectedObject object, PrivilegedOperation operation) {
		this.object = object;
		this.operation = operation;
	}
	
	public ProtectedObject getObject() {
		return object;
	}
	
	public PrivilegedOperation getOperation() {
		return operation;
	}

	public void setOperation(PrivilegedOperation operation) {
		this.operation = operation;
	}

	public void setObject(ProtectedObject object) {
		this.object = object;
	}

	@Override
	public boolean implies(org.apache.shiro.authz.Permission permission) {
		if (permission instanceof Permission) {
			Permission objectPermission = (Permission) permission;
			return getObject().has(objectPermission.getObject()) 
					&& getOperation().can(objectPermission.getOperation());
		} else {
			return false;
		}
	}

	public static Permission ofUserAdmin(User user) {
		return new Permission(user, GeneralOperation.ADMIN);
	}
	
	public static Permission ofUserRead(User user) {
		return new Permission(user, GeneralOperation.READ);
	}

	public static Permission ofUserWrite(User user) {
		return new Permission(user, GeneralOperation.WRITE);
	}

	public static Permission ofRepositoryAdmin(Repository repository) {
		return new Permission(repository, GeneralOperation.ADMIN);
	}

	public static Permission ofRepositoryRead(Repository repository) {
		return new Permission(repository, GeneralOperation.READ);
	}

	public static Permission ofRepositoryWrite(Repository repository) {
		return new Permission(repository, GeneralOperation.WRITE);
	}

	public static Permission ofSystemAdmin() {
	    return new Permission(new SystemObject(), SystemOperation.ADMINISTRATION);
	}
}
