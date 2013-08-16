package com.pmease.gitop.core.model.permission;

import org.apache.shiro.authz.Permission;

import com.pmease.gitop.core.model.permission.object.ProtectedObject;
import com.pmease.gitop.core.model.permission.operation.PrivilegedOperation;

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

}
