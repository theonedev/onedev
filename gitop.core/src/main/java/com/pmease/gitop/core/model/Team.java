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
import com.pmease.gitop.core.permission.operation.UserOperation;

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
	private UserOperation authorizedOperation = UserOperation.READ;
	
	@OneToMany(mappedBy="team")
	private Collection<TeamMembership> memberships = new ArrayList<TeamMembership>();
	
	@OneToMany(mappedBy="team")
	private Collection<RepositoryAuthorization> repositoryAuthorizations = new ArrayList<RepositoryAuthorization>();

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

	public UserOperation getAuthorizedOperation() {
		return authorizedOperation;
	}

	public void setAuthorizedOperation(UserOperation authorizedOeration) {
		this.authorizedOperation = authorizedOeration;
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

	public void setAuthorizations(Collection<RepositoryAuthorization> repositoryAuthorizations) {
		this.repositoryAuthorizations = repositoryAuthorizations;
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof ObjectPermission) {
			ObjectPermission objectPermission = (ObjectPermission) permission;
			
			for (RepositoryAuthorization each: getRepositoryAuthorizations()) {
				if (each.getRepository().has(objectPermission.getObject()))
					return each.getAuthorizedOperation().can(objectPermission.getOperation());
			}

			if (getOwner().has(objectPermission.getObject()))
				return getAuthorizedOperation().can(objectPermission.getOperation());
		} 
		
		return false;
	}

}
