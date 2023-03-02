package io.onedev.server.entitymanager;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;

public interface IssueLinkManager extends EntityManager<IssueLink> {

	void syncLinks(LinkSpec spec, Issue issue, Collection<Issue> linkedIssues, boolean opposite);

    void create(IssueLink link);

    void populateLinks(Collection<Issue> issues);
	
	void loadDeepLinks(Issue issue);
	
}
