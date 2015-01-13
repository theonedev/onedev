package com.pmease.gitplex.core.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"branch", "user"})
})
public class BranchWatch extends AbstractEntity {

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Branch branch;
	
	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
