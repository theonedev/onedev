package com.pmease.gitop.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitop.core.permission.operation.UserOperation;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"individual", "user"})
})
public class UserAuthorizationByIndividual extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private User individual;	

	@ManyToOne
	@JoinColumn(nullable=false)
	private User user;
	
	private UserOperation authorizedOperation = UserOperation.READ;
	
	public UserOperation getAuthorizedOperation() {
		return authorizedOperation;
	}

	public void setAuthorizedOperation(UserOperation authorizedOperation) {
		this.authorizedOperation = authorizedOperation;
	}

	public User getIndividual() {
		return individual;
	}

	public void setIndividual(User individual) {
		this.individual = individual;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
