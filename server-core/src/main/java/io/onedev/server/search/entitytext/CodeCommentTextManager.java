package io.onedev.server.search.entitytext;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;

public interface CodeCommentTextManager {

	long count(@Nullable Project project, String queryString);
	
	List<CodeComment> query(@Nullable Project project, String queryString, 
			int firstResult, int maxResults);
	
}
