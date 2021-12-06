package io.onedev.server.entitymanager;

import java.util.Collection;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueLinkManager extends EntityManager<IssueLink> {

	void syncLinks(LinkSpec spec, Issue issue, Collection<Issue> linkedIssues, boolean opposite);
	
	void populateLinks(Collection<Issue> issues);
	
}
