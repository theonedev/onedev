package io.onedev.server.service;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;

import java.util.Collection;

public interface IssueLinkService extends EntityService<IssueLink> {

	void syncLinks(LinkSpec spec, Issue issue, Collection<Issue> linkedIssues, boolean opposite);

    void create(IssueLink link);

    void populateLinks(Collection<Issue> issues);
	
	void loadDeepLinks(Issue issue);
	
}
