package com.gitplex.server.core.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.gitplex.commons.hibernate.AbstractEntity;

@Entity
@Table(
		indexes={@Index(columnList="g_user_id"), @Index(columnList="g_organization_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_user_id", "g_organization_id"})
})
public class OrganizationMembership extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account organization;
	
	private boolean admin;
	
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

}
