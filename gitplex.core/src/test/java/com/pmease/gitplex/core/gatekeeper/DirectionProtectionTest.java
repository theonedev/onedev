package com.pmease.gitplex.core.gatekeeper;

import static org.junit.Assert.assertTrue;

import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitplex.core.gatekeeper.helper.branchselection.SpecifyTargetBranchesByNames;
import com.pmease.gitplex.core.gatekeeper.helper.pathselection.SpecifyTargetPathsByDirectories;
import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;

public class DirectionProtectionTest extends AbstractGitTest {

	@Mock
	private Dao dao;
	
	@Override
	protected void setup() {
		super.setup();
		
		Mockito.when(AppLoader.getInstance(Dao.class)).thenReturn(dao);
		Team team = new Team();
		team.setName("team");
		team.setId(1L);
		Membership membership = new Membership();
		membership.setTeam(team);
		membership.setUser(new User());
		membership.getUser().setId(1L);
		team.getMemberships().add(membership);
		Mockito.when(dao.load(Team.class, 1L)).thenReturn(team);
	}

	@Test
	public void testCheckFile() {
		DirectoryAndFileProtection pathProtection = new DirectoryAndFileProtection();
		SpecifyTargetBranchesByNames branchSelection = new SpecifyTargetBranchesByNames();
		branchSelection.setBranches(Lists.newArrayList("branch1"));
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
		
		Repository repo = new Repository();
		assertTrue(pathProtection.checkFile(user1, repo, "branch1", "src/test.java").isPassed());
		assertTrue(pathProtection.checkFile(user1, repo, "branch1", "doc/test.pdf").isPassed());
		assertTrue(pathProtection.checkFile(user2, repo, "branch1", "src/test.java").isPending());
		assertTrue(pathProtection.checkFile(user2, repo, "branch2", "src/test.java").isPassed());
	}

	@SuppressWarnings("serial")
	@Test
	public void testCheckCommit() {
		DirectoryAndFileProtection pathProtection = new DirectoryAndFileProtection();
		SpecifyTargetBranchesByNames branchSelection = new SpecifyTargetBranchesByNames();
		branchSelection.setBranches(Lists.newArrayList("branch1"));
		pathProtection.setBranchSelection(branchSelection);
		
		SpecifyTargetPathsByDirectories pathSelection = new SpecifyTargetPathsByDirectories();
		pathSelection.setDirectories(Lists.newArrayList("src"));
		pathProtection.setPathSelection(pathSelection);
		
		pathProtection.setTeamIds(Lists.newArrayList(1L));

		addFileAndCommit("src/file1", "", "add src/file1");

		git.checkout("master", "branch1");
		git.checkout("master", "branch2");

		Repository repo = new Repository() {

			@Override
			public Git git() {
				return git;
			}
			
		};
		repo.cacheObjectId("branch1", ObjectId.fromString(git.parseRevision("branch1", true)));

		User user1 = new User();
		user1.setId(1L);
		
		User user2 = new User();
		user2.setId(2L);

		addFileAndCommit("src/file2", "", "add src/file2");
		
		assertTrue(pathProtection.checkCommit(user1, repo, "branch1", git.parseRevision("branch2", true)).isPassed());
		assertTrue(pathProtection.checkCommit(user2, repo, "branch1", git.parseRevision("branch2", true)).isPending());

		git.checkout("master", "branch3");

		addFileAndCommit("docs/file2", "", "add docs/file2");
		
		assertTrue(pathProtection.checkCommit(user1, repo, "branch1", git.parseRevision("branch3", true)).isPassed());
		assertTrue(pathProtection.checkCommit(user2, repo, "branch1", git.parseRevision("branch3", true)).isPassed());
	}

}
