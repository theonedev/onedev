package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.core.permission.operation.PrivilegedOperation;
import com.pmease.gitop.core.permission.operation.Read;

@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"owner", "name"})
})
@SuppressWarnings("serial")
public class Team extends AbstractEntity implements Permission {

	@ManyToOne
	@JoinColumn(nullable=false)
	private User owner;

	@Column(nullable=false)
	private String name;
	
	private String description;
	
	private boolean anonymous;
	
	private boolean register;

	@Column(nullable=false)
	private PrivilegedOperation operation = new Read();
	
	@OneToMany(mappedBy="team")
	private Collection<TeamMembership> memberships = new ArrayList<TeamMembership>();
	
	@OneToMany(mappedBy="team")
	private Collection<Authorization> authorizations = new ArrayList<Authorization>();

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

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

	public PrivilegedOperation getOperation() {
		return operation;
	}

	public void setOperation(PrivilegedOperation operation) {
		this.operation = operation;
	}

	public Collection<TeamMembership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<TeamMembership> memberships) {
		this.memberships = memberships;
	}

	public Collection<Authorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<Authorization> authorizations) {
		this.authorizations = authorizations;
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof ObjectPermission) {
			ObjectPermission objectPermission = (ObjectPermission) permission;
			
			for (Authorization each: getAuthorizations()) {
				if (each.getRepository().has(objectPermission.getObject()))
					return each.getOperation().can(objectPermission.getOperation());
			}

			if (getOwner().has(objectPermission.getObject()))
				return getOperation().can(objectPermission.getOperation());
		} 
		
		return false;
	}

}
