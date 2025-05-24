package io.onedev.server.entitymanager;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.AutoMerge;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestChangeManager extends EntityManager<PullRequestChange> {

	void create(PullRequestChange change, @Nullable String note);
	
	void changeMergeStrategy(PullRequest request, MergeStrategy mergeStrategy);
	
	void changeAutoMerge(PullRequest request, AutoMerge autoMerge);
	
	void changeTitle(PullRequest request, String title);

	void changeDescription(PullRequest request, @Nullable String description);
	
	void changeTargetBranch(PullRequest request, String targetBranch);
	
	List<PullRequestChange> query(User submitter, Date fromDate, Date toDate);

}
