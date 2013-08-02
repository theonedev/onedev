package com.pmease.gitop.core.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.pmease.gitop.core.model.permission.account.AccountAdministration;
import com.pmease.gitop.core.model.permission.account.AccountPermission;
import com.pmease.gitop.core.model.permission.account.OperationOfRepositorySet;
import com.pmease.gitop.core.model.permission.account.ReadFromAccount;
import com.pmease.gitop.core.model.permission.account.WriteToAccount;
import com.pmease.gitop.core.model.permission.system.AdminAllAccounts;
import com.pmease.gitop.core.model.permission.system.ReadFromAllAccounts;
import com.pmease.gitop.core.model.permission.system.SystemAdministration;
import com.pmease.gitop.core.model.permission.system.WriteToAllAccounts;

public class PermissionTest {

	@Test
	public void shouldHandleAccountAdminPermissionAppropriately() {
		Account account = new Account();
		account.setId(100L);
		
		Team team = new Team();
		team.setAccount(account);
		
		team.setAuthorizedAccountWideOperation(new AccountAdministration());
		
		assertTrue(team.implies(AccountPermission.ofAccountAdmin(account)));
		assertTrue(team.implies(AccountPermission.ofAccountWrite(account)));
		assertTrue(team.implies(AccountPermission.ofAccountRead(account)));
		assertTrue(team.implies(AccountPermission.ofRepositoryAdmin(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofRepositoryWrite(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofRepositoryRead(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofBranchAdmin(account, "repo1", "branch1")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1")));
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "branch1")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1", "src/file")));

		assertFalse(team.implies(new SystemAdministration()));
		assertFalse(team.implies(new AdminAllAccounts()));
		assertFalse(team.implies(new WriteToAllAccounts()));
		assertFalse(team.implies(new ReadFromAllAccounts()));
	}

	@Test
	public void shouldHandleAccountWriterPermissionAppropriately() {
		Account account = new Account();
		account.setId(100L);
		
		Team team = new Team();
		team.setAccount(account);
		
		team.setAuthorizedAccountWideOperation(new WriteToAccount());
		
		assertTrue(team.implies(AccountPermission.ofAccountWrite(account)));
		assertTrue(team.implies(AccountPermission.ofAccountRead(account)));
		assertTrue(team.implies(AccountPermission.ofRepositoryWrite(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofRepositoryRead(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1")));
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "branch1")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1", "src/file")));

		assertFalse(team.implies(AccountPermission.ofAccountAdmin(account)));
		assertFalse(team.implies(AccountPermission.ofRepositoryAdmin(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofBranchAdmin(account, "repo1", "branch1")));
	}

	@Test
	public void shouldHandleAccountReaderPermissionAppropriately() {
		Account account = new Account();
		account.setId(100L);
		
		Team team = new Team();
		team.setAccount(account);
		
		team.setAuthorizedAccountWideOperation(new ReadFromAccount());
		
		assertTrue(team.implies(AccountPermission.ofAccountRead(account)));
		assertTrue(team.implies(AccountPermission.ofRepositoryRead(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "branch1")));

		assertFalse(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1", "src/file")));
		assertFalse(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1")));
		assertFalse(team.implies(AccountPermission.ofRepositoryWrite(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofAccountWrite(account)));
		assertFalse(team.implies(AccountPermission.ofAccountAdmin(account)));
		assertFalse(team.implies(AccountPermission.ofRepositoryAdmin(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofBranchAdmin(account, "repo1", "branch1")));
	}
	
	@Test
	public void shouldHandleRepositoryAdminPermissionAppropriately() {
		Account account = new Account();
		account.setId(100L);
		
		Team team = new Team();
		team.setAccount(account);
		team.setAuthorizedAccountWideOperation(null);
		
		team.getAuthorizedRepositoryOperations().add(OperationOfRepositorySet.ofRepositoryAdmin("-repo2, *"));
		
		assertTrue(team.implies(AccountPermission.ofRepositoryAdmin(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofBranchAdmin(account, "repo1", "branch1")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1", "src/file")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1")));
		assertTrue(team.implies(AccountPermission.ofRepositoryWrite(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofRepositoryRead(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "branch1")));

		assertFalse(team.implies(AccountPermission.ofAccountRead(account)));
		assertFalse(team.implies(AccountPermission.ofAccountWrite(account)));
		assertFalse(team.implies(AccountPermission.ofAccountAdmin(account)));
		assertFalse(team.implies(AccountPermission.ofRepositoryRead(account, "repo2")));
	}
	
	@Test
	public void shouldHandleRepositoryWritePermissionAppropriately() {
		Account account = new Account();
		account.setId(100L);
		
		Team team = new Team();
		team.setAccount(account);
		team.setAuthorizedAccountWideOperation(null);
		
		team.getAuthorizedRepositoryOperations().add(OperationOfRepositorySet.ofRepositoryWrite("-repo2, *"));
		
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1", "src/file")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1")));
		assertTrue(team.implies(AccountPermission.ofRepositoryWrite(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofRepositoryRead(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "branch1")));

		assertFalse(team.implies(AccountPermission.ofBranchAdmin(account, "repo1", "branch1")));
		assertFalse(team.implies(AccountPermission.ofRepositoryAdmin(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofAccountRead(account)));
		assertFalse(team.implies(AccountPermission.ofAccountWrite(account)));
		assertFalse(team.implies(AccountPermission.ofAccountAdmin(account)));
		assertFalse(team.implies(AccountPermission.ofRepositoryRead(account, "repo2")));
	}
	
	@Test
	public void shouldHandleRepositoryReadPermissionAppropriately() {
		Account account = new Account();
		account.setId(100L);
		
		Team team = new Team();
		team.setAccount(account);
		team.setAuthorizedAccountWideOperation(null);
		
		team.getAuthorizedRepositoryOperations().add(OperationOfRepositorySet.ofRepositoryRead("-repo2, *"));
		
		assertTrue(team.implies(AccountPermission.ofRepositoryRead(account, "repo1")));
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "branch1")));

		assertFalse(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1", "src/file")));
		assertFalse(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1")));
		assertFalse(team.implies(AccountPermission.ofRepositoryWrite(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofBranchAdmin(account, "repo1", "branch1")));
		assertFalse(team.implies(AccountPermission.ofRepositoryAdmin(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofAccountRead(account)));
		assertFalse(team.implies(AccountPermission.ofAccountWrite(account)));
		assertFalse(team.implies(AccountPermission.ofAccountAdmin(account)));
		assertFalse(team.implies(AccountPermission.ofRepositoryRead(account, "repo2")));
	}

	@Test
	public void shouldHandleBranchAdminPermissionAppropriately() {
		Account account = new Account();
		account.setId(100L);
		
		Team team = new Team();
		team.setAccount(account);
		team.setAuthorizedAccountWideOperation(null);
		
		team.getAuthorizedRepositoryOperations().add(OperationOfRepositorySet.ofBranchAdmin("-repo2, *", "**/release"));
		
		assertTrue(team.implies(AccountPermission.ofBranchAdmin(account, "repo1", "5.0/release")));
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "test/release")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "1.0/release", "src/file")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "2.0/release")));
		
		assertFalse(team.implies(AccountPermission.ofBranchAdmin(account, "repo2", "release")));
		assertFalse(team.implies(AccountPermission.ofRepositoryWrite(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofRepositoryRead(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofBranchAdmin(account, "repo1", "branch1")));
		assertFalse(team.implies(AccountPermission.ofAccountRead(account)));
		assertFalse(team.implies(AccountPermission.ofAccountWrite(account)));
		assertFalse(team.implies(AccountPermission.ofAccountAdmin(account)));
		assertFalse(team.implies(AccountPermission.ofRepositoryAdmin(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofRepositoryRead(account, "repo2")));
	}

	@Test
	public void shouldHandleBranchWritePermissionAppropriately() {
		Account account = new Account();
		account.setId(100L);
		
		Team team = new Team();
		team.setAccount(account);
		team.setAuthorizedAccountWideOperation(null);
		
		team.getAuthorizedRepositoryOperations().add(OperationOfRepositorySet.ofBranchWrite("-repo2, *", "**/release", "-**/*.java, **"));
		
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "test/release")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "1.0/release", "src/file")));
		
		assertFalse(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "2.0/release", "test.java")));
		assertFalse(team.implies(AccountPermission.ofBranchAdmin(account, "repo1", "1.0/release")));
		assertFalse(team.implies(AccountPermission.ofRepositoryWrite(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofRepositoryRead(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofBranchAdmin(account, "repo1", "branch1")));
		assertFalse(team.implies(AccountPermission.ofAccountRead(account)));
		assertFalse(team.implies(AccountPermission.ofAccountWrite(account)));
		assertFalse(team.implies(AccountPermission.ofAccountAdmin(account)));
		assertFalse(team.implies(AccountPermission.ofRepositoryAdmin(account, "repo1")));
		assertFalse(team.implies(AccountPermission.ofRepositoryRead(account, "repo2")));
	}

	@Test
	public void shouldHandleMultipleTeamPermissionsAppropriately() {
		Account account = new Account();
		account.setId(100L);
		
		Team team = new Team();
		team.setAccount(account);
		team.setAuthorizedAccountWideOperation(null);
		
		team.getAuthorizedRepositoryOperations().add(OperationOfRepositorySet.ofBranchWrite("repo1", "branch1"));
		team.getAuthorizedRepositoryOperations().add(OperationOfRepositorySet.ofBranchRead("repo1", "branch2"));
		
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "branch1")));
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "branch2")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1")));
		
		assertFalse(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch2")));
		
		team.setAuthorizedAccountWideOperation(new ReadFromAccount());
		
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo1", "branch1")));
		assertTrue(team.implies(AccountPermission.ofBranchRead(account, "repo2", "branch2")));
		assertTrue(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1")));
		
		assertFalse(team.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch2")));
	}
	
	@Test
	public void systemAdministratorShouldBeAbleToDoAnything() {
		assertTrue(new SystemAdministration().implies(new SystemAdministration()));
		assertTrue(new SystemAdministration().implies(new WriteToAllAccounts()));
		assertTrue(new SystemAdministration().implies(new ReadFromAllAccounts()));

		Account account = new Account();
		account.setId(100L);
		assertTrue(new SystemAdministration().implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1")));
	}
	
	@Test
	public void shouldHandleAllAccountWriterPermissionAppropriately() {
		WriteToAllAccounts writeToAllAccounts = new WriteToAllAccounts();
		
		Account account = new Account();
		account.setId(100L);

		assertTrue(writeToAllAccounts.implies(new WriteToAllAccounts()));
		assertTrue(writeToAllAccounts.implies(new ReadFromAllAccounts()));
		assertTrue(writeToAllAccounts.implies(AccountPermission.ofAccountWrite(account)));
		assertTrue(writeToAllAccounts.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1", "src/file")));
		
		assertFalse(writeToAllAccounts.implies(AccountPermission.ofAccountAdmin(account)));
		assertFalse(writeToAllAccounts.implies(AccountPermission.ofBranchAdmin(account, "repo1", "branch1")));
		assertFalse(writeToAllAccounts.implies(new SystemAdministration()));
	}
	
	@Test
	public void shouldHandleAllAccountReaderPermissionAppropriately() {
		ReadFromAllAccounts readFromAllAccounts = new ReadFromAllAccounts();
		
		Account account = new Account();
		account.setId(100L);

		assertTrue(readFromAllAccounts.implies(new ReadFromAllAccounts()));
		assertTrue(readFromAllAccounts.implies(AccountPermission.ofBranchRead(account, "repo1", "branch1")));
		
		assertFalse(readFromAllAccounts.implies(AccountPermission.ofBranchWrite(account, "repo1", "branch1", "src/file")));
		assertFalse(readFromAllAccounts.implies(AccountPermission.ofAccountWrite(account)));
		assertFalse(readFromAllAccounts.implies(AccountPermission.ofAccountAdmin(account)));
		assertFalse(readFromAllAccounts.implies(AccountPermission.ofBranchAdmin(account, "repo1", "branch1")));
		assertFalse(readFromAllAccounts.implies(new WriteToAllAccounts()));
		assertFalse(readFromAllAccounts.implies(new SystemAdministration()));
	}

}
