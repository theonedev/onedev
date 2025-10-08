package io.onedev.server.service;

import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.AutoMerge;
import io.onedev.server.model.support.pullrequest.MergeStrategy;

public interface PullRequestChangeService extends EntityService<PullRequestChange> {

	void create(PullRequestChange change, @Nullable String note);
	
	void changeMergeStrategy(User user, PullRequest request, MergeStrategy mergeStrategy);
	
	void changeAutoMerge(User user, PullRequest request, AutoMerge autoMerge);
	
	void changeTitle(User user, PullRequest request, String title);

	void changeDescription(User user, PullRequest request, @Nullable String description);
	
	void changeTargetBranch(User user, PullRequest request, String targetBranch);
	
	List<PullRequestChange> query(User submitter, Date fromDate, Date toDate);

}
