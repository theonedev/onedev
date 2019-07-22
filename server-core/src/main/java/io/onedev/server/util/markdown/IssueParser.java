package io.onedev.server.util.markdown;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

public class IssueParser extends ReferenceParser<Issue> {

	@Override
	protected Issue findReferenceable(Project project, long number) {
		return OneDev.getInstance(IssueManager.class).find(project, number);
	}
	
}
