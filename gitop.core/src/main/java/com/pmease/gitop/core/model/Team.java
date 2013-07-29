package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.shiro.authz.Permission;
import org.hibernate.annotations.FetchMode;

import com.google.common.base.Optional;
import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.model.permission.account.AccountOperation;
import com.pmease.gitop.core.model.permission.account.AccountPermission;
import com.pmease.gitop.core.model.permission.account.AccountWideOperation;
import com.pmease.gitop.core.model.permission.account.OperationOfRepositorySet;
import com.pmease.gitop.core.model.permission.account.PrivilegedOperation;
import com.pmease.gitop.core.model.permission.account.ReadFromAccount;

/**
 * Every user can define its teams to authorize permissions to his/her repositories. 
 * Other users can join the the team to gain access to these repositories.
 *  
 * @author robin
 *
 */
@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"account", "name"})
})
@SuppressWarnings("serial")
public class Team extends AbstractEntity implements Permission {

	@Column(nullable=false)
	private String name;
	
	private String description;
	
	private Optional<? extends AccountWideOperation> authorizedAccountWideOperation = Optional.of(new ReadFromAccount());
	
	private List<OperationOfRepositorySet> authorizedRepositoryOperations = new ArrayList<OperationOfRepositorySet>();
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_TEAM_ACC")
	private Account account;

	@OneToMany(mappedBy="team")
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

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Collection<Membership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<Membership> memberships) {
		this.memberships = memberships;
	}

	public Optional<? extends AccountWideOperation> getAuthorizedAccountWideOperation() {
		return authorizedAccountWideOperation;
	}

	public void setAuthorizedAccountWideOperation(
			Optional<? extends AccountWideOperation> authorizedAccountWideOperation) {
		this.authorizedAccountWideOperation = authorizedAccountWideOperation;
	}

	public List<OperationOfRepositorySet> getAuthorizedRepositoryOperations() {
		return authorizedRepositoryOperations;
	}

	public void setAuthorizedRepositoryOperations(
			List<OperationOfRepositorySet> authorizedRepositoryOperations) {
		this.authorizedRepositoryOperations = authorizedRepositoryOperations;
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof AccountPermission) {
			AccountPermission accountPermission = (AccountPermission) permission;
			if (accountPermission.getAccount().getId().equals(getAccount().getId())) {
				if (getAuthorizedAccountWideOperation().isPresent()) {
					AccountOperation accountAction = getAuthorizedAccountWideOperation().get();
					if (accountAction.can(accountPermission.getOperation()))
						return true;
				}
				for (PrivilegedOperation action: getAuthorizedRepositoryOperations()) {
					if (action.can(accountPermission.getOperation()))
						return true;
				}
				
				return false;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
