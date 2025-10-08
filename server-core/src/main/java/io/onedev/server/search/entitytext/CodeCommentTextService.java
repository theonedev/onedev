package io.onedev.server.search.entitytext;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.CodeComment;

public interface CodeCommentTextService {

	List<Long> query(@Nullable Project project, String queryString, int count);
			
	boolean matches(CodeComment comment, String queryString);

}
