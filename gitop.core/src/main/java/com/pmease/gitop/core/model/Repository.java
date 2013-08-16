package com.pmease.gitop.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.FetchMode;

import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.model.gatekeeper.GateKeeper;
import com.pmease.gitop.core.model.permission.object.ProtectedObject;
import com.pmease.gitop.core.model.permission.object.RepositoryBelonging;
import com.pmease.gitop.core.model.permission.object.UserBelonging;

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

	@Column(nullable=true)
	private GateKeeper gateKeeper;

	@Override
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
	public boolean has(ProtectedObject object) {
		if (object instanceof Repository) {
			Repository repository = (Repository) object;
			return repository.getId().equals(getId());
		} else if (object instanceof RepositoryBelonging) {
			RepositoryBelonging repositoryBelonging = (RepositoryBelonging) object;
			return repositoryBelonging.getOwner().getId().equals(getId());
		} else {
			return false;
		}
	}

}
