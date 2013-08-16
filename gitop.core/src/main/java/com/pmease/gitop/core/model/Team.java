package com.pmease.gitop.core.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.model.permission.ObjectPermission;
import com.pmease.gitop.core.model.permission.operation.PrivilegedOperation;

@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"owner", "name"})
})
@SuppressWarnings("serial")
public class Team extends AbstractEntity implements Permission {

	private User owner;

	@Column(nullable=false, unique=true)
	private String name;
	
	private String description;
	
	private PrivilegedOperation operation;
	
	@OneToMany(mappedBy="team")
	private Collection<TeamMembership> memberships;
	
	@OneToMany(mappedBy="subject")
	private Collection<RepositoryAuthorization> repositoryAuthorizations;

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

	public Collection<RepositoryAuthorization> getRepositoryAuthorizations() {
		return repositoryAuthorizations;
	}

	public void setRepositoryAuthorizations(
			Collection<RepositoryAuthorization> repositoryAuthorizations) {
		this.repositoryAuthorizations = repositoryAuthorizations;
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof ObjectPermission) {
			ObjectPermission objectPermission = (ObjectPermission) permission;
			
			for (RepositoryAuthorization each: getRepositoryAuthorizations()) {
				PrivilegedOperation operation = each.getOperation().operationOf(
						objectPermission.getObject(), each.getObject());
				
				if (operation != null)
					return operation.can(objectPermission.getOperation());
			}

			if (getOwner().has(objectPermission.getObject()))
				return getOperation().can(objectPermission.getOperation());
		} 
		
		return false;
	}

}
