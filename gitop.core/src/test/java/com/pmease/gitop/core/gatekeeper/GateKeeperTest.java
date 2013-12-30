package com.pmease.gitop.core.gatekeeper;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.AppLoaderMocker;
import com.pmease.gitop.core.gatekeeper.DirectoryProtection.Entry;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;

public class GateKeeperTest extends AppLoaderMocker {

	@Mock
	private BranchManager branchManager;
	
	@Override
	protected void setup() {
		Mockito.when(AppLoader.getInstance(BranchManager.class)).thenReturn(branchManager);
		Branch branch1 = new Branch();
		branch1.setId(1L);
		branch1.setName("branch1");
		Mockito.when(branchManager.load(1L)).thenReturn(branch1);
		
		Branch branch2 = new Branch();
		branch2.setId(2L);
		branch2.setName("branch2");
		Mockito.when(branchManager.load(2L)).thenReturn(branch2);
	}

	@Test
	public void testCheckFile() {
		DirectoryProtection directoryProtection = new DirectoryProtection();
		directoryProtection.setBranchIds(Lists.newArrayList(1L));
		Entry entry = new Entry();
		entry.setDirectory("src/**");
		entry.setTeamId(1L);
		directoryProtection.getEntries().add(entry);
		
		Team team = new Team();
		team.setId(1L);
		User user = new User();
		user.setId(1L);
		Membership membership = new Membership();
		membership.setUser(user);
		membership.setTeam(team);
		user.getMemberships().add(membership);
		team.getMemberships().add(membership);
		
		Branch branch = new Branch();
		branch.setId(1L);
		assertTrue(directoryProtection.checkFile(user, branch, "src/test.java").isAccepted());
	}

	@Override
	protected void teardown() {
	}

}
