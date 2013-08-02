package com.pmease.gitop.core.model.permission.account;

import org.apache.shiro.authz.Permission;

import com.pmease.gitop.core.model.Account;

/**
 * This class represents permissions to operate an account and its belongings.
 *  
 * @author robin
 *
 */
public class AccountPermission implements Permission {
	
	private Account account;
	
	private AccountOperation operation;

	public AccountPermission(Account account, AccountOperation operation) {
		this.account = account;
		this.operation = operation;
	}
	
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public AccountOperation getOperation() {
		return operation;
	}

	public void setOperation(AccountOperation operation) {
		this.operation = operation;
	}

	public static AccountPermission ofAccountAdmin(Account account) {
		return new AccountPermission(account, new AccountAdministration());
	}
	
	public static AccountPermission ofAccountRead(Account account) {
		return new AccountPermission(account, new ReadFromAccount());
	}

	public static AccountPermission ofAccountWrite(Account account) {
		return new AccountPermission(account, new WriteToAccount());
	}

	public static AccountPermission ofRepositoryAdmin(Account account, String repositoryName) {
		return new AccountPermission(account, OperationOfRepositorySet.ofRepositoryAdmin(repositoryName));
	}

	public static AccountPermission ofRepositoryRead(Account account, String repositoryName) {
		return new AccountPermission(account, OperationOfRepositorySet.ofRepositoryRead(repositoryName));
	}

	public static AccountPermission ofRepositoryWrite(Account account, String repositoryName) {
		return new AccountPermission(account, OperationOfRepositorySet.ofRepositoryWrite(repositoryName));
	}

	public static AccountPermission ofBranchAdmin(Account account, String repositoryName, String branchName) {
		return new AccountPermission(account, OperationOfRepositorySet.ofBranchAdmin(repositoryName, branchName));
	}

	public static AccountPermission ofBranchRead(Account account, String repositoryName, String branchName) {
		return new AccountPermission(account, OperationOfRepositorySet.ofBranchRead(repositoryName, branchName));
	}

	public static AccountPermission ofBranchWrite(Account account, String repositoryName, String branchName) {
		return new AccountPermission(account, OperationOfRepositorySet.ofBranchWrite(repositoryName, branchName, "**"));
	}

	public static AccountPermission ofBranchWrite(Account account, String repositoryName, String branchName, String filePath) {
		return new AccountPermission(account, OperationOfRepositorySet.ofBranchWrite(repositoryName, branchName, filePath));
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof AccountPermission) {
			AccountPermission accountPermission = (AccountPermission) permission;
			if (getAccount().getId().equals(accountPermission.getAccount().getId())) {
				return getOperation().can(accountPermission.getOperation());
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
