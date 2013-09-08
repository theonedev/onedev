package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.core.permission.object.SystemObject;
import com.pmease.gitop.core.permission.operation.PrivilegedOperation;

@Entity
@SuppressWarnings("serial")
public class Role extends AbstractEntity implements Permission {

	@Column(nullable=false, unique=true)
	private String name;
	
	private String description;
	
	private boolean anonymous;
	
	private boolean register;

	@Column(nullable=false)
	private ArrayList<PrivilegedOperation> operations = new ArrayList<PrivilegedOperation>();
	
	@OneToMany(mappedBy="role")
	private Collection<RoleMembership> memberships = new ArrayList<RoleMembership>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	public boolean isRegister() {
		return register;
	}

	public void setRegister(boolean register) {
		this.register = register;
	}

	public Collection<RoleMembership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<RoleMembership> memberships) {
		this.memberships = memberships;
	}

	public ArrayList<PrivilegedOperation> getOperations() {
		return operations;
	}

	public void setOperations(ArrayList<PrivilegedOperation> operations) {
		this.operations = operations;
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof ObjectPermission) {
			ObjectPermission objectPermission = (ObjectPermission) permission;
			if (new SystemObject().has(objectPermission.getObject())) {
				for (PrivilegedOperation each: getOperations()) {
					if (each.can(objectPermission.getOperation()))
						return true;
				}
			}
		}
		return false;
	}

}
