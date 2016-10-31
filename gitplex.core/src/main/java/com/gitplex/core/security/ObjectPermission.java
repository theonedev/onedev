package com.gitplex.core.security;

import org.apache.shiro.authz.Permission;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.security.privilege.AccountPrivilege;
import com.gitplex.core.security.privilege.DepotPrivilege;
import com.gitplex.core.security.privilege.Privilege;
import com.gitplex.core.security.privilege.SystemAdmin;
import com.gitplex.core.security.protectedobject.ProtectedObject;
import com.gitplex.core.security.protectedobject.SystemObject;

/**
 * This class represents permissions to operate an account and its belongings.
 *  
 * @author robin
 *
 */
public class ObjectPermission implements Permission {
	
	private ProtectedObject object;
	
	private Privilege privilege;

	public ObjectPermission(ProtectedObject object, Privilege privilege) {
		this.object = object;
		this.privilege = privilege;
	}
	
	public ProtectedObject getObject() {
		return object;
	}
	
	public Privilege getPrivilege() {
		return privilege;
	}

	public void setPrivilege(Privilege privilege) {
		this.privilege = privilege;
	}

	public void setObject(ProtectedObject object) {
		this.object = object;
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof ObjectPermission) {
			ObjectPermission objectPermission = (ObjectPermission) permission;
			return getObject().has(objectPermission.getObject()) 
					&& getPrivilege().can(objectPermission.getPrivilege());
		} else {
			return false;
		}
	}

	public static ObjectPermission ofDepotAdmin(Depot depot) {
		return new ObjectPermission(depot, DepotPrivilege.ADMIN);
	}

	public static ObjectPermission ofDepotRead(Depot depot) {
		return new ObjectPermission(depot, DepotPrivilege.READ);
	}

	public static ObjectPermission ofDepotWrite(Depot depot) {
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
