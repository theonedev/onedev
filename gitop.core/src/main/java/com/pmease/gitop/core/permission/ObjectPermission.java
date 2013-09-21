package com.pmease.gitop.core.permission;

import org.apache.shiro.authz.Permission;

import com.pmease.gitop.core.model.Repository;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.object.ProtectedObject;
import com.pmease.gitop.core.permission.object.SystemObject;
import com.pmease.gitop.core.permission.operation.PrivilegedOperation;
import com.pmease.gitop.core.permission.operation.RepositoryOperation;
import com.pmease.gitop.core.permission.operation.SystemOperation;
import com.pmease.gitop.core.permission.operation.UserOperation;

/**
 * This class represents permissions to operate an account and its belongings.
 *  
 * @author robin
 *
 */
public class ObjectPermission implements Permission {
	
	private ProtectedObject object;
	
	private PrivilegedOperation operation;

	public ObjectPermission(ProtectedObject object, PrivilegedOperation operation) {
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
	public boolean implies(Permission permission) {
		if (permission instanceof ObjectPermission) {
			ObjectPermission objectPermission = (ObjectPermission) permission;
			return getObject().has(objectPermission.getObject()) 
					&& getOperation().can(objectPermission.getOperation());
		} else {
			return false;
		}
	}

	public static ObjectPermission ofUserAdmin(User user) {
		return new ObjectPermission(user, UserOperation.ADMINISTRATION);
	}
	
	public static ObjectPermission ofUserRead(User user) {
		return new ObjectPermission(user, UserOperation.READ);
	}

	public static ObjectPermission ofUserWrite(User user) {
		return new ObjectPermission(user, UserOperation.WRITE);
	}

	public static ObjectPermission ofRepositoryAdmin(Repository repository) {
		return new ObjectPermission(repository, RepositoryOperation.ADMINISTRATION);
	}

	public static ObjectPermission ofRepositoryRead(Repository repository) {
		return new ObjectPermission(repository, RepositoryOperation.READ);
	}

	public static ObjectPermission ofRepositoryWrite(Repository repository) {
		return new ObjectPermission(repository, RepositoryOperation.WRITE);
	}

	public static ObjectPermission ofSystem(SystemOperation operation) {
		return new ObjectPermission(new SystemObject(), operation);
	}
	
}
