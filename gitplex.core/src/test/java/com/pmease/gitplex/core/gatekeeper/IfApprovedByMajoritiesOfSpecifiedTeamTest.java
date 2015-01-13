package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;

public class IfApprovedByMajoritiesOfSpecifiedTeamTest extends AbstractGitTest {

	@Mock
	private Dao dao;
	
	@Override
	protected void setup() {
		super.setup();
		
		Mockito.when(AppLoader.getInstance(Dao.class)).thenReturn(dao);
		Branch branch1 = new Branch();
		branch1.setId(1L);
		branch1.setName("branch1");
		Mockito.when(dao.load(Branch.class, 1L)).thenReturn(branch1);
		
		Branch branch2 = new Branch();
		branch2.setId(2L);
		branch2.setName("branch2");
		Mockito.when(dao.load(Branch.class, 2L)).thenReturn(branch2);
		
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
		
		Mockito.when(dao.load(Team.class, 1L)).thenReturn(team);
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
		addFileAndCommit("file1", "", "add file1");
		
		git.checkout("master", "dev");
		
		addFileAndCommit("file2", "", "add file2");
		
		IfApprovedByMajoritiesOfSpecifiedTeam gateKeeper = new IfApprovedByMajoritiesOfSpecifiedTeam();
		gateKeeper.setTeamId(1L);

		PullRequest request = new PullRequest();
		request.setId(1L);
		request.setTarget(new Branch());
		request.getTarget().setName("master");
		request.getTarget().setHeadCommitHash(git.parseRevision("master", true));
		request.getTarget().setRepository(new Repository() {

			@Override
			public Git git() {
				return git;
			}
			
		});
		request.getTarget().getRepository().setId(1L);
		request.getTarget().getRepository().setOwner(new User());
		request.getTarget().getRepository().getOwner().setId(1L);
		
		request.setSource(new Branch());
		request.getSource().setName("dev");
		request.getSource().setHeadCommitHash(git.parseRevision("dev", true));
		request.getSource().setRepository(new Repository() {

			@Override
			public Git git() {
				return git;
			}
			
		});
		
		request.setSubmitter(new User());
		request.getSubmitter().setId(2L);
		request.getSubmitter().setName("user2");
		request.setBaseCommitHash(git.calcMergeBase("dev", "master"));

		final Collection<Review> reviews = new ArrayList<>();

		PullRequestUpdate update = new PullRequestUpdate() {

			@Override
			public Collection<Review> getReviews() {
				return reviews;
			}
			
		};
		update.setHeadCommitHash(git.parseRevision("dev", true));
		update.setId(1L);
		update.setRequest(request);
		request.addUpdate(update);
		
		Review review = new Review();
		review.setId(1L);
		review.setResult(Review.Result.APPROVE);
		review.setUpdate(update);
		review.setReviewer(new User());
		review.getReviewer().setId(1L);
		review.getReviewer().setName("user1");
		reviews.add(review);

		Assert.assertTrue(gateKeeper.checkRequest(request).isPending());

		Assert.assertEquals("user3", request.getReviewInvitations().iterator().next().getReviewer().getName());
		
		review = new Review();
		review.setId(2L);
		review.setResult(Review.Result.APPROVE);
		review.setUpdate(update);
		review.setReviewer(new User());
		review.getReviewer().setId(3L);
		review.getReviewer().setName("user3");
		reviews.add(review);

		Assert.assertTrue(gateKeeper.checkRequest(request).isPassed());
	}

}
