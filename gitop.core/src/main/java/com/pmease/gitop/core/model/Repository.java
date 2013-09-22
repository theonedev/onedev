package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.gatekeeper.GateKeeper;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.core.permission.object.ProtectedObject;
import com.pmease.gitop.core.permission.object.UserBelonging;
import com.pmease.gitop.core.permission.operation.RepositoryOperation;

@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"owner", "name"})
})
@SuppressWarnings("serial")
@Editable
public class Repository extends AbstractEntity implements UserBelonging {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private User owner;

	@Column(nullable=false)
	private String name;
	
	private String description;

	private boolean publiclyAccessible;
	
	@Column(nullable=false)
	private RepositoryOperation defaultAuthorizedOperation;
	
	private GateKeeper gateKeeper;

	@OneToMany(mappedBy="repository")
	private Collection<RepositoryAuthorizationByTeam> authorizationsByTeam = 
			new ArrayList<RepositoryAuthorizationByTeam>();

	@OneToMany(mappedBy="repository")
	private Collection<RepositoryAuthorizationByIndividual> authorizationsByIndividual = 
			new ArrayList<RepositoryAuthorizationByIndividual>();

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@Editable(description=
			"Specify name of the repository. It will be used to identify the repository when accessing via Git.")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(description="Specify description of the repository.")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(name="Public", description=
			"If a repository is made public, it will be able to be browsed/pulled by anonymous users.")
	public boolean isPubliclyAccessible() {
		return publiclyAccessible;
	}

	public void setPubliclyAccessible(boolean publiclyAccessible) {
		this.publiclyAccessible = publiclyAccessible;
	}

	public RepositoryOperation getDefaultAuthorizedOperation() {
		return defaultAuthorizedOperation;
	}

	public void setDefaultAuthorizedOperation(
			RepositoryOperation defaultAuthorizedOperation) {
		this.defaultAuthorizedOperation = defaultAuthorizedOperation;
	}

	@Editable(
			name="Accept Merge Requests If", 
			description="Optionally define gate keeper to accept merge requests under certain condition.")
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

	public Collection<RepositoryAuthorizationByTeam> getAuthorizationsByTeam() {
		return authorizationsByTeam;
	}

	public void setAuthorizationsByTeam(
			Collection<RepositoryAuthorizationByTeam> authorizationsByTeam) {
		this.authorizationsByTeam = authorizationsByTeam;
	}

	public Collection<RepositoryAuthorizationByIndividual> getAuthorizationsByIndividual() {
		return authorizationsByIndividual;
	}

	public void setAuthorizationsByIndividual(
			Collection<RepositoryAuthorizationByIndividual> authorizationsByIndividual) {
		this.authorizationsByIndividual = authorizationsByIndividual;
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

	public Collection<User> findAuthorizedUsers(RepositoryOperation operation) {
		Set<User> authorizedUsers = new HashSet<User>();
		for (User user: Gitop.getInstance(UserManager.class).query(null)) {
			if (user.asSubject().isPermitted(new ObjectPermission(this, operation)))
				authorizedUsers.add(user);
		}
		return authorizedUsers;
	}
	
}
