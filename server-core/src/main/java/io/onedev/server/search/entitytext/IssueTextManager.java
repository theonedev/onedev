package io.onedev.server.search.entitytext;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScope;

public interface IssueTextManager {

	long count(@Nullable ProjectScope projectScope, String queryString);
	
	List<Issue> query(@Nullable ProjectScope projectScope, String queryString, 
			boolean loadFieldsAndLinks, int firstResult, int maxResults);
	
}
