package com.pmease.gitop.core.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class TeamTest {

	@Test
	public void shouldOnlyPermitAuthorizedBranchOperations() {
		Team team = new Team();
		
		BranchPermission permission = new BranchPermission();
		permission.setRepositoryNamePattern("*");
		permission.setBranchNamePattern("master");
		permission.setBranchOperation(new PullFromBranch());
		permission.setAllow(false);
		team.getBranchPermissions().add(permission);
		
		permission = new BranchPermission();
		permission.setRepositoryNamePattern("*");
		permission.setBranchNamePattern("*");
		permission.setBranchOperation(new PullFromBranch());
		permission.setAllow(true);
		team.getBranchPermissions().add(permission);
		
		permission = new BranchPermission();
		permission.setRepositoryNamePattern("*");
		permission.setBranchNamePattern("qa");
		permission.setBranchOperation(new PullFromBranch());
		permission.setAllow(false);
		team.getBranchPermissions().add(permission);

		assertFalse(team.permits("repo1", "master", new PullFromBranch()));
		assertTrue(team.permits("repo1", "development", new PullFromBranch()));
		assertTrue(team.permits("repo1", "qa", new PullFromBranch()));
		assertNull(team.permits("repo1", "master", new PushToBranch()));
		assertNull(team.permits("repo1", "devleopment", new PushToBranch()));
	
		team.getBranchPermissions().clear();
		
		permission = new BranchPermission();
		permission.setRepositoryNamePattern("*");
		permission.setBranchNamePattern("*");
		PushToBranch pushToBranch = new PushToBranch();
		pushToBranch.setFilePath("doc/**");
		permission.setBranchOperation(pushToBranch);
		permission.setAllow(true);
		team.getBranchPermissions().add(permission);

		permission = new BranchPermission();
		permission.setRepositoryNamePattern("*");
		permission.setBranchNamePattern("*");
		pushToBranch = new PushToBranch();
		pushToBranch.setFilePath("**");
		permission.setBranchOperation(pushToBranch);
		permission.setAllow(false);
		team.getBranchPermissions().add(permission);
		
		pushToBranch = new PushToBranch();
		pushToBranch.setFilePath("doc/user_guide.pdf");
		assertTrue(team.permits("repo2", "master", pushToBranch));
		
		pushToBranch = new PushToBranch();
		pushToBranch.setFilePath("src/program/Core.java");
		assertFalse(team.permits("repo2", "master", pushToBranch));
		
		assertNull(team.permits("repo2", "master", new PullFromBranch()));
		
		team.getBranchPermissions().clear();
		
		permission = new BranchPermission();
		permission.setRepositoryNamePattern("repo?");
		permission.setBranchNamePattern("*");
		pushToBranch = new PushToBranch();
		pushToBranch.setFilePath("doc/**");
		permission.setBranchOperation(pushToBranch);
		permission.setAllow(true);
		team.getBranchPermissions().add(permission);

		permission = new BranchPermission();
		permission.setRepositoryNamePattern("*");
		permission.setBranchNamePattern("*");
		permission.setBranchOperation(new PullFromBranch());
		permission.setAllow(false);
		team.getBranchPermissions().add(permission);
		
		pushToBranch = new PushToBranch();
		pushToBranch.setFilePath("doc/api/index.html");
		assertTrue(team.permits("repo2", "master", pushToBranch));
		assertFalse(team.permits("myrepo", "master", new PullFromBranch()));
		assertNull(team.permits("myrepo", "master", pushToBranch));
	}

}
