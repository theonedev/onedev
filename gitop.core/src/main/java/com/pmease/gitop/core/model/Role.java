package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.model.permission.system.SystemPermission;

@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("serial")
public class Role extends AbstractEntity implements Permission {

	@Column(nullable=false, unique=true)
	private String name;
	
	private String description;
	
	private List<SystemPermission> permissions = new ArrayList<SystemPermission>();
	
	@OneToMany(mappedBy="role")
	private Collection<Authorization> authorizations;

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

	public List<SystemPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<SystemPermission> permissions) {
		this.permissions = permissions;
	}

	public Collection<Authorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<Authorization> authorizations) {
		this.authorizations = authorizations;
	}

	@Override
	public boolean implies(Permission permission) {
		for (SystemPermission each: permissions) {
			if (each.implies(permission))
				return true;
		}
		return false;
	}

}
