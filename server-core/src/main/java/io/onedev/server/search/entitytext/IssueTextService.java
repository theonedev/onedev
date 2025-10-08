package io.onedev.server.search.entitytext;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScope;

public interface IssueTextService {

	List<Long> query(@Nullable ProjectScope projectScope, String queryString, int count);
			
	boolean matches(Issue issue, String queryString);

}
