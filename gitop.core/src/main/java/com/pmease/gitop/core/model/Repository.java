package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.FetchMode;

import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.gatekeeper.GateKeeper;
import com.pmease.gitop.core.permission.object.ProtectedObject;
import com.pmease.gitop.core.permission.object.UserBelonging;
import com.pmease.gitop.core.permission.operation.PrivilegedOperation;

@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"owner", "name"})
})
@SuppressWarnings("serial")
public class Repository extends AbstractEntity implements UserBelonging {
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	private User owner;

	@Column(nullable=false)
	private String name;
	
	private String description;

	private GateKeeper gateKeeper;

	@OneToMany(mappedBy="repository")
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

	public GateKeeper getGateKeeper() {
		return gateKeeper;
	}

	public void setGateKeeper(GateKeeper gateKeeper) {
		this.gateKeeper = gateKeeper;
	}

	@Override
	public User getUser() {
		return getOwner();
	}

	public Collection<Authorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<Authorization> authorizations) {
		this.authorizations = authorizations;
	}

	@Override
	public boolean has(ProtectedObject object) {
		if (object instanceof Repository) {
			Repository repository = (Repository) object;
			return repository.getId().equals(getId());
		} else {
			return false;
		}
	}

	public Collection<User> findAuthorizedUsers(PrivilegedOperation operation) {
		Map<Long, Boolean> authorizationMap = new HashMap<Long, Boolean>();
		for (Authorization authorization: getAuthorizations()) {
			authorizationMap.put(authorization.getTeam().getId(), authorization.getOperation().can(operation));
		}
		
		Collection<Team> teams = new HashSet<Team>();
		for (Team team: getOwner().getTeams()) {
			Boolean authorized = authorizationMap.get(team.getId());
			if (authorized != null) {
				if (authorized)
					teams.add(team);
				else
					continue;
			} else {
				if (team.getOperation().can(operation))
					teams.add(team);
			}
		}
		
		Collection<User> users = new HashSet<User>();
		for (Team team: teams) {
			for (TeamMembership membership: team.getMemberships())
				users.add(membership.getUser());
		}
		
		return users;
	}
	
}
