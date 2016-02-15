package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Depot;
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
		Assert.assertTrue(gateKeeper.checkFile(user1, new Depot(), "branch1", "src/file").isPending());
	}

	@SuppressWarnings("serial")
	@Test
	public void testCheckRequest() {
		addFileAndCommit("file1", "", "add file1");
		
		git.checkout("master", "dev");
		
		addFileAndCommit("file2", "", "add file2");
		
		IfApprovedByMajoritiesOfSpecifiedTeam gateKeeper = new IfApprovedByMajoritiesOfSpecifiedTeam();
		gateKeeper.setTeamId(1L);

		Depot targetRepo = new Depot() {

			@Override
			public Git git() {
				return git;
			}
			
		};
		targetRepo.setId(1L);
		targetRepo.setOwner(new User());
		targetRepo.getOwner().setId(1L);
		targetRepo.cacheObjectId("master", ObjectId.fromString(git.parseRevision("master", true)));
		
		PullRequest request = new PullRequest();
		request.setId(1L);
		request.setTargetDepot(targetRepo);
		request.setTargetBranch("master");
		
		Depot sourceRepo = new Depot() {
			
			@Override
			public Git git() {
				return git;
			}
			
		};
		sourceRepo.cacheObjectId("dev", ObjectId.fromString(git.parseRevision("dev", true)));
		request.setSourceDepot(sourceRepo);
		request.setSourceBranch("dev");
		
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
