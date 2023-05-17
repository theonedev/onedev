package io.onedev.server.web.component.commandpalette;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.search.code.CodeSearchManager;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.query.FileQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import org.eclipse.jgit.lib.ObjectId;

import java.util.LinkedHashMap;
import java.util.Map;

public class RevisionAndPathParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "revision-and-path";
	
	public RevisionAndPathParam(boolean optional) {
		super(NAME, optional);
	}
	
	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		if (matchWith.length() == 0) {
			suggestions.put("Type to search in default branch", null);
		} else {
			Project project = ParsedUrl.getProject(paramValues);
			if (project.getDefaultBranch() != null) {
				CodeSearchManager codeSearchManager = OneDev.getInstance(CodeSearchManager.class);
				ObjectId commitId = project.getObjectId(project.getDefaultBranch(), true);
				var query = new FileQuery.Builder("*" + matchWith + "*")
						.count(count)
						.build();
				try {
					for (QueryHit hit: codeSearchManager.search(project, commitId, query)) {
						String revisionAndPath = project.getDefaultBranch() + "/" + hit.getBlobPath();
						suggestions.put(revisionAndPath, revisionAndPath);
					}
				} catch (TooGeneralQueryException e) {
					suggestions.put("Query is too general", null);
				}
			}
		}
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		return false;
	}
		
}
