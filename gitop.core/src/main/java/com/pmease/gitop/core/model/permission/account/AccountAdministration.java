package com.pmease.gitop.core.model.permission.account;


/**
 * Account administrators can do anything inside an account.
 * 
 * @author robin
 *
 */
public class AccountAdministration implements AccountWideOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof AccountAdministration 
				|| new WriteToAccount().can(operation)
				|| OperationOfRepositorySet.ofRepositoryAdmin("**").can(operation);
	}

}
