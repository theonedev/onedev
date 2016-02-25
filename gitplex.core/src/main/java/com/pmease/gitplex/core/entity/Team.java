package com.pmease.gitplex.core.entity;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitplex.core.permission.operation.DepotOperation;
import com.pmease.gitplex.core.util.validation.TeamName;

@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"g_owner_id", "name"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)

/* 
 * use dynamic update as we do not want to change name while persisting team, 
 * as name of team can be referenced in other places, and we want to update 
 * these references in a transaction via TeamManager.rename 
 */  
@DynamicUpdate
public class Team extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String FQN_SEPARATOR = "/";
	
	public static final String ANONYMOUS = "Anonymous";
	public static final String OWNERS = "Owners";
	public static final String LOGGEDIN = "Logged-In";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User owner;

	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	private DepotOperation authorizedOperation = DepotOperation.NO_ACCESS;
	
	@OneToMany(mappedBy="team")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<Membership> memberships = new ArrayList<Membership>();
	
	@OneToMany(mappedBy="team")
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Collection<Authorization> authorizations = new ArrayList<Authorization>();

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@TeamName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DepotOperation getAuthorizedOperation() {
		return authorizedOperation;
	}

	public void setAuthorizedOperation(DepotOperation authorizedOeration) {
		this.authorizedOperation = authorizedOeration;
	}

	public Collection<Membership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<Membership> memberships) {
		this.memberships = memberships;
	}

	public Collection<Authorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<Authorization> authorizations) {
		this.authorizations = authorizations;
	}

	public boolean isAnonymous() {
		return ANONYMOUS.equalsIgnoreCase(getName());
	}
	
	public boolean isOwners() {
		return OWNERS.equalsIgnoreCase(getName());
	}
	
	public boolean isLoggedIn() {
		return LOGGEDIN.equalsIgnoreCase(getName());
	}
	
	public boolean isBuiltIn() {
		return isAnonymous() || isOwners() || isLoggedIn();
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("name", getName())
				.add("owner", getOwner().getName())
				.toString();
	}

}
