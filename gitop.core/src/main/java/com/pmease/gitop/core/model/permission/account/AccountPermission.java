package com.pmease.gitop.core.model.permission.account;

import com.pmease.gitop.core.model.Account;

/**
 * This interface serves as a mark up interface to indicate that all permissions
 * implementing this interface are project level permissions. 
 * 
 * @author robin
 *
 */
public class AccountPermission {
	
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
		return new AccountPermission(account, new OperationOfRepositorySet(repositoryName, new RepositoryAdministration()));
	}

	public static AccountPermission ofRepositoryRead(Account account, String repositoryName) {
		return new AccountPermission(account, new OperationOfRepositorySet(repositoryName, new ReadFromRepository()));
	}

	public static AccountPermission ofRepositoryWrite(Account account, String repositoryName) {
		return new AccountPermission(account, new OperationOfRepositorySet(repositoryName, new WriteToRepository()));
	}

	public static AccountPermission ofBranchAdmin(Account account, String repositoryName, String branchName) {
		return new AccountPermission(account, new OperationOfRepositorySet(repositoryName, new OperationOfBranchSet(branchName, new BranchAdministration())));
	}

	public static AccountPermission ofBranchRead(Account account, String repositoryName, String branchName) {
		return new AccountPermission(account, new OperationOfRepositorySet(repositoryName, new OperationOfBranchSet(branchName, new ReadFromBranch())));
	}

	public static AccountPermission ofBranchWrite(Account account, String repositoryName, String branchName) {
		return new AccountPermission(account, new OperationOfRepositorySet(repositoryName, new OperationOfBranchSet(branchName, new WriteToBranch("**"))));
	}

	public static AccountPermission ofBranchWrite(Account account, String repositoryName, String branchName, String filePath) {
		return new AccountPermission(account, new OperationOfRepositorySet(repositoryName, new OperationOfBranchSet(branchName, new WriteToBranch(filePath))));
	}

}
