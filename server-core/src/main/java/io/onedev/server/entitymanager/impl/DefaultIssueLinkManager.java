package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.annotation.Sessional;
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

	@Sessional
	@Override
	public void populateLinks(Collection<Issue> issues) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueLink> query = builder.createQuery(IssueLink.class);
		
		Root<IssueLink> root = query.from(IssueLink.class);
		query.select(root);
		
		query.where(builder.or(
				root.get(IssueLink.PROP_SOURCE).in(issues)), 
				root.get(IssueLink.PROP_TARGET).in(issues));
		
		for (Issue issue: issues) {
			issue.setSourceLinks(new ArrayList<>());
			issue.setTargetLinks(new ArrayList<>());
		}
		
		for (IssueLink link: getSession().createQuery(query).getResultList()) {
			Long sourceId = Issue.idOf(link.getSource());
			Long targetId = Issue.idOf(link.getTarget());
			issues.stream().filter(it->it.getId().equals(sourceId)).forEach(it->it.getTargetLinks().add(link));
			issues.stream().filter(it->it.getId().equals(targetId)).forEach(it->it.getSourceLinks().add(link));
		}
	}
	
}
