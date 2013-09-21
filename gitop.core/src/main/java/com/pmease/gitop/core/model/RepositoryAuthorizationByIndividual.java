package com.pmease.gitop.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitop.core.permission.operation.RepositoryOperation;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"individual", "repository"})
})
public class RepositoryAuthorizationByIndividual extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private User individual;	

	@ManyToOne
	@JoinColumn(nullable=false)
	private Repository repository;
	
	private RepositoryOperation authorizedOperation = RepositoryOperation.READ;
	
	public RepositoryOperation getAuthorizedOperation() {
		return authorizedOperation;
	}

	public void setAuthorizedOperation(RepositoryOperation authorizedOperation) {
		this.authorizedOperation = authorizedOperation;
	}

	public User getIndividual() {
		return individual;
	}

	public void setIndividual(User individual) {
		this.individual = individual;
	}
	
	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
