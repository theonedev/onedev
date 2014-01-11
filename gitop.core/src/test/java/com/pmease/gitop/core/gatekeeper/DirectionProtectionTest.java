package com.pmease.gitop.core.gatekeeper;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitop.core.gatekeeper.helper.branchselection.SpecifyTargetBranchesByIds;
import com.pmease.gitop.core.gatekeeper.helper.pathselection.SpecifyTargetPathsByDirectories;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;

public class DirectionProtectionTest extends AbstractGitTest {

	@Mock
	private BranchManager branchManager;
	
	@Mock
	private TeamManager teamManager;
	
	@Override
	protected void setup() {
		super.setup();
		
		Mockito.when(AppLoader.getInstance(BranchManager.class)).thenReturn(branchManager);
		Branch branch1 = new Branch();
		branch1.setId(1L);
		branch1.setName("branch1");
		Mockito.when(branchManager.load(1L)).thenReturn(branch1);
		
		Branch branch2 = new Branch();
		branch2.setId(2L);
		branch2.setName("branch2");
		Mockito.when(branchManager.load(2L)).thenReturn(branch2);
		
		Mockito.when(AppLoader.getInstance(TeamManager.class)).thenReturn(teamManager);
		
		Team team = new Team();
		team.setName("team");
		team.setId(1L);
		Membership membership = new Membership();
		membership.setTeam(team);
		membership.setUser(new User());
		membership.getUser().setId(1L);
		team.getMemberships().add(membership);
		Mockito.when(teamManager.load(1L)).thenReturn(team);
	}

	@Test
	public void testCheckFile() {
		DirectoryAndFileProtection pathProtection = new DirectoryAndFileProtection();
		SpecifyTargetBranchesByIds branchSelection = new SpecifyTargetBranchesByIds();
		branchSelection.setBranchIds(Lists.newArrayList(1L));
		pathProtection.setBranchSelection(branchSelection);
		
		SpecifyTargetPathsByDirectories pathSelection = new SpecifyTargetPathsByDirectories();
		pathSelection.setDirectories(Lists.newArrayList("src"));
		pathProtection.setPathSelection(pathSelection);
		
		pathProtection.setTeamIds(Lists.newArrayList(1L));
		
		Team team = new Team();
		team.setId(1L);
		User user1 = new User();
		user1.setId(1L);
		Membership membership = new Membership();
		membership.setUser(user1);
		membership.setTeam(team);
		user1.getMemberships().add(membership);
		team.getMemberships().add(membership);
		
		User user2 = new User();
		user2.setId(2L);
		
		Branch branch1 = new Branch();
		branch1.setId(1L);
		Branch branch2 = new Branch();
		branch2.setId(2L);
		assertTrue(pathProtection.checkFile(user1, branch1, "src/test.java").isAccepted());
		assertTrue(pathProtection.checkFile(user1, branch1, "doc/test.pdf").isAccepted());
		assertTrue(pathProtection.checkFile(user2, branch1, "src/test.java").isPending());
		assertTrue(pathProtection.checkFile(user2, branch2, "src/test.java").isAccepted());
	}

	@SuppressWarnings("serial")
	@Test
	public void testCheckCommit() {
	    File tempDir = FileUtils.createTempDir();
		
		try {
			DirectoryAndFileProtection pathProtection = new DirectoryAndFileProtection();
			SpecifyTargetBranchesByIds branchSelection = new SpecifyTargetBranchesByIds();
			branchSelection.setBranchIds(Lists.newArrayList(1L));
			pathProtection.setBranchSelection(branchSelection);
			
			SpecifyTargetPathsByDirectories pathSelection = new SpecifyTargetPathsByDirectories();
			pathSelection.setDirectories(Lists.newArrayList("src"));
			pathProtection.setPathSelection(pathSelection);
			
			pathProtection.setTeamIds(Lists.newArrayList(1L));

			final Git git = new Git(tempDir);
			git.init(false);
			
			FileUtils.touchFile(new File(tempDir, "src/file1"));
			git.add("src/file1");
			git.commit("add src/file1", false, false);

			git.checkout("master", "branch1");
			
			Branch master = new Branch();
			master.setName("master");
			master.setId(1L);
			master.setProject(new Project() {

				@Override
				public Git code() {
					return git;
				}
				
			});

			User user1 = new User();
			user1.setId(1L);
			
			User user2 = new User();
			user2.setId(2L);

			FileUtils.touchFile(new File(tempDir, "src/file2"));
			git.add("src/file2");
			git.commit("add src/file2", false, false);
			
			assertTrue(pathProtection.checkCommit(user1, master, git.parseRevision("branch1", true)).isAccepted());
			assertTrue(pathProtection.checkCommit(user2, master, git.parseRevision("branch1", true)).isPending());
			
			git.checkout("master", "branch2");

			FileUtils.touchFile(new File(tempDir, "docs/file2"));
			git.add("docs/file2");
			git.commit("add docs/file2", false, false);
			
			assertTrue(pathProtection.checkCommit(user1, master, git.parseRevision("branch2", true)).isAccepted());
			assertTrue(pathProtection.checkCommit(user2, master, git.parseRevision("branch2", true)).isAccepted());
		} finally {
			FileUtils.deleteDir(tempDir);;
		}
	}

	@Override
	protected void teardown() {
		super.teardown();
	}

}
