package com.pmease.gitop.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitop.core.permission.operation.PrivilegedOperation;
import com.pmease.gitop.core.permission.operation.Read;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"team", "repository"})
})
public class Authorization extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private Team team;	

	@ManyToOne
	@JoinColumn(nullable=false)
	private Repository repository;
	
	private PrivilegedOperation operation = new Read();
	
	public PrivilegedOperation getOperation() {
		return operation;
	}

	public void setOperation(PrivilegedOperation operation) {
		this.operation = operation;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
	
	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
