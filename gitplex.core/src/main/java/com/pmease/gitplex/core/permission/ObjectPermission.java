package com.pmease.gitplex.core.permission;

import org.apache.shiro.authz.Permission;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.permission.object.ProtectedObject;
import com.pmease.gitplex.core.permission.object.SystemObject;
import com.pmease.gitplex.core.permission.privilege.AccountPrivilege;
import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;
import com.pmease.gitplex.core.permission.privilege.Privilege;
import com.pmease.gitplex.core.permission.privilege.SystemAdmin;

/**
 * This class represents permissions to operate an account and its belongings.
 *  
 * @author robin
 *
 */
public class ObjectPermission implements Permission {
	
	private ProtectedObject object;
	
	private Privilege operation;

	public ObjectPermission(ProtectedObject object, Privilege operation) {
		this.object = object;
		this.operation = operation;
	}
	
	public ProtectedObject getObject() {
		return object;
	}
	
	public Privilege getOperation() {
		return operation;
	}

	public void setOperation(Privilege operation) {
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

	public static ObjectPermission ofDepotAdmin(Depot depot) {
		return new ObjectPermission(depot, DepotPrivilege.ADMIN);
	}

	public static ObjectPermission ofDepotPull(Depot depot) {
		return new ObjectPermission(depot, DepotPrivilege.READ);
	}

	public static ObjectPermission ofDepotPush(Depot depot) {
		return new ObjectPermission(depot, DepotPrivilege.WRITE);
	}

	public static ObjectPermission ofAccountAdmin(Account account) {
		return new ObjectPermission(account, AccountPrivilege.ADMIN);
	}
	
	public static ObjectPermission ofAccountAccess(Account account) {
		return new ObjectPermission(account, AccountPrivilege.ACCESS);
	}
	
	public static ObjectPermission ofSystemAdmin() {
		return new ObjectPermission(new SystemObject(), new SystemAdmin());
	}
	
}
