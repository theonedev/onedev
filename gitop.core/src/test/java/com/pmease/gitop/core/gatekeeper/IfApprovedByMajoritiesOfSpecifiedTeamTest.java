package com.pmease.gitop.core.gatekeeper;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;

public class IfApprovedByMajoritiesOfSpecifiedTeamTest extends AbstractGitTest {

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
		membership.getUser().setName("user1");
		team.getMemberships().add(membership);
		membership = new Membership();
		membership.setTeam(team);
		membership.setUser(new User());
		membership.getUser().setId(2L);
		membership.getUser().setName("user2");
		team.getMemberships().add(membership);
		membership = new Membership();
		membership.setTeam(team);
		membership.setUser(new User());
		membership.getUser().setId(3L);
		membership.getUser().setName("user3");
		team.getMemberships().add(membership);
		
		Mockito.when(teamManager.load(1L)).thenReturn(team);
	}

	@Test
	public void testCheckFile() {
		IfApprovedByMajoritiesOfSpecifiedTeam gateKeeper = new IfApprovedByMajoritiesOfSpecifiedTeam();
		gateKeeper.setTeamId(1L);
		
		User user1 = new User();
		user1.setId(1L);
		Branch branch1 = new Branch();
		branch1.setId(1L);
		Assert.assertTrue(gateKeeper.checkFile(user1, branch1, "src/file").isPending());
	}

	@SuppressWarnings("serial")
	@Test
	public void testCheckRequest() {
		final Git git = new Git(FileUtils.createTempDir());
		try {
			git.init(false);

			FileUtils.touchFile(new File(git.repoDir(), "file1"));
			git.add("file1");
			git.commit("add file1", false, false);
			
			git.checkout("master", "dev");
			FileUtils.touchFile(new File(git.repoDir(), "file2"));
			git.add("file2");
			git.commit("add file2", false, false);
			
			IfApprovedByMajoritiesOfSpecifiedTeam gateKeeper = new IfApprovedByMajoritiesOfSpecifiedTeam();
			gateKeeper.setTeamId(1L);

			PullRequest request = new PullRequest();
			request.setId(1L);
			request.setTarget(new Branch());
			request.getTarget().setName("master");
			request.getTarget().setProject(new Repository() {

				@Override
				public Git code() {
					return git;
				}
				
			});
			request.getTarget().getProject().setId(1L);
			request.getTarget().getProject().setOwner(new User());
			request.getTarget().getProject().getOwner().setId(1L);
			
			request.setSource(new Branch());
			request.getSource().setName("dev");
			request.getSource().setProject(new Repository() {

				@Override
				public Git code() {
					return git;
				}
				
			});
			
			request.setSubmittedBy(new User());
			request.getSubmittedBy().setId(2L);
			request.getSubmittedBy().setName("user2");
			
			PullRequestUpdate update = new PullRequestUpdate();
			update.setHeadCommit(git.parseRevision("dev", true));
			update.setId(1L);
			update.setRequest(request);
			request.getUpdates().add(update);
			
			Assert.assertTrue(gateKeeper.checkRequest(request).isPending());
			Collection<User> candidates = new HashSet<>();
			User user = new User();
			user.setId(1L);
			user.setName("user1");
			candidates.add(user);
			user = new User();
			user.setId(2L);
			user.setName("user2");
			candidates.add(user);
			user = new User();
			user.setId(3L);
			user.setName("user3");
			candidates.add(user);
			Assert.assertEquals("user1", request.getVoteInvitations().iterator().next().getVoter().getName());
			
			Vote vote = new Vote();
			vote.setId(1L);
			vote.setResult(Vote.Result.APPROVE);
			vote.setUpdate(update);
			vote.setVoter(new User());
			vote.getVoter().setId(1L);
			vote.getVoter().setName("user1");
			update.getVotes().add(vote);
			
			Assert.assertTrue(gateKeeper.checkRequest(request).isApproved());
		} finally {
			FileUtils.deleteDir(git.repoDir());
		}
	}

	@Override
	protected void teardown() {
		super.teardown();
	}

}
