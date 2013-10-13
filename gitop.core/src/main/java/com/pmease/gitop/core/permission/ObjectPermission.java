package com.pmease.gitop.core.permission;

import org.apache.shiro.authz.Permission;

import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.object.ProtectedObject;
import com.pmease.gitop.core.permission.operation.GeneralOperation;
import com.pmease.gitop.core.permission.operation.PrivilegedOperation;

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
		return new ObjectPermission(user, GeneralOperation.ADMIN);
	}
	
	public static ObjectPermission ofUserRead(User user) {
		return new ObjectPermission(user, GeneralOperation.READ);
	}

	public static ObjectPermission ofUserWrite(User user) {
		return new ObjectPermission(user, GeneralOperation.WRITE);
	}

	public static ObjectPermission ofProjectAdmin(Project project) {
		return new ObjectPermission(project, GeneralOperation.ADMIN);
	}

	public static ObjectPermission ofProjectRead(Project project) {
		return new ObjectPermission(project, GeneralOperation.READ);
	}

	public static ObjectPermission ofProjectWrite(Project project) {
		return new ObjectPermission(project, GeneralOperation.WRITE);
	}

}
