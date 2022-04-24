package io.onedev.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.web.page.project.commits.CommitDetailPage;

public class CommitParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public CommitParam(boolean optional) {
		super(CommitDetailPage.PARAM_COMMIT, optional);
	}

	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		RevCommit commit = ParsedUrl.getProject(paramValues).getRevCommit(matchWith, false);
		if (commit != null) 
			suggestions.put(commit.getShortMessage(), matchWith);
		else
			suggestions.put("Input branch/tag/commit to show", null);
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		return false;
	}

}
