package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
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

import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.model.permission.account.AccountPermission;
import com.pmease.gitop.core.model.permission.account.AccountWideOperation;
import com.pmease.gitop.core.model.permission.account.OperationOfRepositorySet;
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
	
	private AccountWideOperation authorizedAccountWideOperation = new ReadFromAccount();
	
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

	/**
	 * Get authorized account wide operation.
	 * 
	 * @return
	 * 			null if account wide operation is not authorized
	 */
	public AccountWideOperation getAuthorizedAccountWideOperation() {
		return authorizedAccountWideOperation;
	}

	/**
	 * Specify authorized account wide operation for this team. 
	 * 
	 * @param authorizedAccountWideOperation
	 * 			null to do not authorize any account wide operation
	 */
	public void setAuthorizedAccountWideOperation(@Nullable AccountWideOperation authorizedAccountWideOperation) {
		this.authorizedAccountWideOperation = authorizedAccountWideOperation;
	}

	/**
	 * Get list of authorized repository operations.
	 * 
	 * @return
	 * 			list of authorized repository operations
	 */
	public List<OperationOfRepositorySet> getAuthorizedRepositoryOperations() {
		return authorizedRepositoryOperations;
	}

	/**
	 * Specify list of authorized repository operations.
	 * 
	 * @param authorizedRepositoryOperations
	 *			list of authorized repository operations 
	 */
	public void setAuthorizedRepositoryOperations(
			List<OperationOfRepositorySet> authorizedRepositoryOperations) {
		this.authorizedRepositoryOperations = authorizedRepositoryOperations;
	}

	@Override
	public boolean implies(Permission permission) {
		if (getAuthorizedAccountWideOperation() != null 
				&& new AccountPermission(getAccount(), getAuthorizedAccountWideOperation()).implies(permission)) {
			return true;
		}
		for (OperationOfRepositorySet operation: getAuthorizedRepositoryOperations()) {
			if (new AccountPermission(getAccount(), operation).implies(permission))
				return true;
		}
		
		return false;
	}

}
