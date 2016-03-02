package com.pmease.gitplex.core.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.pmease.commons.hibernate.AbstractEntity;

@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"g_user_id", "g_organization_id"})
})
public class Membership extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account organization;
	
	private boolean admin;
	
	private Set<String> joinedTeams = new LinkedHashSet<>();

	/*
	 * Optimistic lock is necessary to ensure database integrity when update 
	 * joinedTeams upon team renaming/deletion
	 */
	@Version
	private long version;
	
	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
		this.user = user;
	}

	public Account getOrganization() {
		return organization;
	}

	public void setOrganization(Account organization) {
		this.organization = organization;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public Set<String> getJoinedTeams() {
		return joinedTeams;
	}

	public void setJoinedTeams(Set<String> joinedTeams) {
		this.joinedTeams = joinedTeams;
	}

	public long getVersion() {
		return version;
	}

}
