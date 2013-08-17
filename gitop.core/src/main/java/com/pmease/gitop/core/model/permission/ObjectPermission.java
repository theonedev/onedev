package com.pmease.gitop.core.model.permission;

import org.apache.shiro.authz.Permission;

import com.pmease.gitop.core.model.Repository;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.permission.object.ProtectedBranches;
import com.pmease.gitop.core.model.permission.object.ProtectedObject;
import com.pmease.gitop.core.model.permission.object.SystemObject;
import com.pmease.gitop.core.model.permission.operation.Administration;
import com.pmease.gitop.core.model.permission.operation.PrivilegedOperation;
import com.pmease.gitop.core.model.permission.operation.Read;
import com.pmease.gitop.core.model.permission.operation.Write;
import com.pmease.gitop.core.model.permission.operation.WriteToBranch;

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
		return new ObjectPermission(user, new Administration());
	}
	
	public static ObjectPermission ofUserRead(User user) {
		return new ObjectPermission(user, new Read());
	}

	public static ObjectPermission ofUserWrite(User user) {
		return new ObjectPermission(user, new Write());
	}

	public static ObjectPermission ofRepositoryAdmin(Repository repository) {
		return new ObjectPermission(repository, new Administration());
	}

	public static ObjectPermission ofRepositoryRead(Repository repository) {
		return new ObjectPermission(repository, new Read());
	}

	public static ObjectPermission ofRepositoryWrite(Repository repository) {
		return new ObjectPermission(repository, new Write());
	}

	public static ObjectPermission ofBranchAdmin(Repository repository, String branchName) {
		return new ObjectPermission(new ProtectedBranches(repository, branchName), new Administration());
	}

	public static ObjectPermission ofBranchRead(Repository repository, String branchName) {
		return new ObjectPermission(new ProtectedBranches(repository, branchName), new Read());
	}

	public static ObjectPermission ofBranchWrite(Repository repository, String branchName) {
		return new ObjectPermission(new ProtectedBranches(repository, branchName), new WriteToBranch("**"));
	}

	public static ObjectPermission ofBranchWrite(Repository repository, String branchName, String filePath) {
		return new ObjectPermission(new ProtectedBranches(repository, branchName), new WriteToBranch(filePath));
	}

	public static ObjectPermission ofSystem(PrivilegedOperation operation) {
		return new ObjectPermission(new SystemObject(), operation);
	}
	
}
