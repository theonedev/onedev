package io.onedev.server.entitymanager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueLinkManager extends BaseEntityManager<IssueLink> implements IssueLinkManager {

	@Inject
	public DefaultIssueLinkManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void syncLinks(LinkSpec spec, Issue issue, Collection<Issue> linkedIssues, boolean opposite) {
		if (spec.getOpposite() != null) {
			Collection<IssueLink> links = opposite?issue.getSourceLinks():issue.getTargetLinks();
			for (IssueLink link: links) {
				if (link.getSpec().equals(spec) && !linkedIssues.contains(link.getLinked(issue)))
					delete(link);
			}
			for (Issue linkedIssue: linkedIssues) {
				boolean found = false;
				for (IssueLink link: links) {
					if (link.getSpec().equals(spec) && link.getLinked(issue).equals(linkedIssue)) {
						found = true;
						break;
					}
				}
				if (!found) {
					IssueLink link = new IssueLink();
					link.setSpec(spec);
					if (opposite) {
						link.setSource(linkedIssue);
						link.setTarget(issue);
					} else {
						link.setSource(issue);
						link.setTarget(linkedIssue);
					}
					save(link);
				}
			}
		} else {			
			for (IssueLink link: issue.getLinks()) {
				if (link.getSpec().equals(spec) && !linkedIssues.contains(link.getLinked(issue)))
					delete(link);
			}
			for (Issue linkedIssue: linkedIssues) {
				boolean found = false;
				for (IssueLink link: issue.getLinks()) {
					if (link.getSpec().equals(spec) && link.getLinked(issue).equals(linkedIssue)) {
						found = true;
						break;
					}
				}
				if (!found) {
					IssueLink link = new IssueLink();
					link.setSpec(spec);
					link.setSource(issue);
					link.setTarget(linkedIssue);
					save(link);
				}
			}
		}
	}

}
