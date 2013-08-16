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
import com.pmease.gitop.core.model.permission.RepositoryOperation;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"subject", "object"})
})
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class RepositoryAuthorization extends AbstractEntity {

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	private Team subject;	

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	private Repository object;
	
	@Column(nullable=false)
	private RepositoryOperation operation;

	public Team getSubject() {
		return subject;
	}

	public void setSubject(Team subject) {
		this.subject = subject;
	}
	
	public Repository getObject() {
		return object;
	}

	public void setObject(Repository object) {
		this.object = object;
	}

	public RepositoryOperation getOperation() {
		return operation;
	}

	public void setOperation(RepositoryOperation operation) {
		this.operation = operation;
	}

}
