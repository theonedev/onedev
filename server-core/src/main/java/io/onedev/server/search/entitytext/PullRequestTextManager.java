package io.onedev.server.search.entitytext;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;

public interface PullRequestTextManager {

	long count(@Nullable Project project, String queryString);
	
	List<PullRequest> query(@Nullable Project project, String queryString, 
			boolean loadReviewsAndBuilds, int firstResult, int maxResults);
	
}
