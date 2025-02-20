package io.onedev.server.search.entitytext;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;

public interface PullRequestTextManager {

	List<Long> query(@Nullable Project project, String queryString, int count);
			
	boolean matches(PullRequest request, String queryString);

}
