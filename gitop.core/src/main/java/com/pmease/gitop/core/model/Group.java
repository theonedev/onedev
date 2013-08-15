package com.pmease.gitop.core.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.persistence.AbstractEntity;

@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("serial")
public class Group extends AbstractEntity implements Permission {

	@Column(nullable=false, unique=true)
	private String name;
	
	private String description;
	
	@OneToMany(mappedBy="group")
	private Collection<Membership> memberships;

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

	@Override
	public boolean implies(Permission permission) {
		return false;
	}

}
