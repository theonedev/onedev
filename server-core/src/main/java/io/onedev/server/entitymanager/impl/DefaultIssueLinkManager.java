package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import org.hibernate.Hibernate;
import org.hibernate.query.Query;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;

@Singleton
public class DefaultIssueLinkManager extends BaseEntityManager<IssueLink> implements IssueLinkManager {

	private final IssueFieldManager fieldManager;
	
	@Inject
	public DefaultIssueLinkManager(Dao dao, IssueFieldManager fieldManager) {
		super(dao);
		this.fieldManager = fieldManager;
	}

	@Transactional
	@Override
	public void syncLinks(LinkSpec spec, Issue issue, Collection<Issue> linkedIssues, boolean opposite) {
		if (spec.getOpposite() != null) {
			Collection<IssueLink> links = opposite?issue.getSourceLinks():issue.getTargetLinks();
			for (var it = links.iterator(); it.hasNext();) {
				var link = it.next();
				if (link.getSpec().equals(spec) && !linkedIssues.contains(link.getLinked(issue))) {
					delete(link);
					it.remove();
				}
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
					create(link);
					links.add(link);
				}
			}
		} else {			
			for (var link: issue.getLinks()) {
				if (link.getSpec().equals(spec) && !linkedIssues.contains(link.getLinked(issue))) {
					delete(link);
					issue.getSourceLinks().remove(link);
					issue.getTargetLinks().remove(link);
				}
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
					create(link);
					issue.getTargetLinks().add(link);
				}
			}
		}
	}

	@Transactional
	@Override
	public void create(IssueLink link) {
		Preconditions.checkState(link.isNew());
		dao.persist(link);
	}

	@Sessional
	@Override
	public void populateLinks(Collection<Issue> issues) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueLink> linkQuery = builder.createQuery(IssueLink.class);
		
		Root<IssueLink> linkRoot = linkQuery.from(IssueLink.class);
		linkQuery.select(linkRoot);
		
		linkQuery.where(builder.or(
				linkRoot.get(IssueLink.PROP_SOURCE).in(issues), 
				linkRoot.get(IssueLink.PROP_TARGET).in(issues)));
		
		for (Issue issue: issues) {
			issue.setSourceLinks(new ArrayList<>());
			issue.setTargetLinks(new ArrayList<>());
		}

		for (IssueLink link: getSession().createQuery(linkQuery).getResultList()) {
			issues.stream().filter(it->it.equals(link.getSource())).forEach(it->it.getTargetLinks().add(link));
			issues.stream().filter(it->it.equals(link.getTarget())).forEach(it->it.getSourceLinks().add(link));
		}
	}

	@Sessional
	@Override
	public void loadDeepLinks(Issue issue) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<IssueLink> root = criteriaQuery.from(IssueLink.class);
		criteriaQuery.multiselect(root, root.get(IssueLink.PROP_SOURCE), root.get(IssueLink.PROP_TARGET));
		
		criteriaQuery.where(builder.or(
				builder.equal(root.get(IssueLink.PROP_SOURCE), issue), 
				builder.equal(root.get(IssueLink.PROP_TARGET), issue)));
		
		Query<Object[]> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(Integer.MAX_VALUE);
		
		List<IssueLink> sourceLinks = new ArrayList<>();
		List<IssueLink> targetLinks = new ArrayList<>();
		
		List<Object[]> results = query.getResultList();
		if (!results.isEmpty()) {
			Map<Long, Issue> issues = new HashMap<>();
			for (Object[] row: results) {
				IssueLink link = (IssueLink) row[0];
				Issue source = (Issue) row[1];
				Issue target = (Issue) row[2];
				if (!source.equals(issue)) 
					sourceLinks.add(link);
				issues.put(source.getId(), source);
				if (!target.equals(issue)) 
					targetLinks.add(link);
				issues.put(target.getId(), target);
			}

			if (Hibernate.isInitialized(issue.getFields())) {
				issues.remove(issue.getId());
				fieldManager.populateFields(issues.values());
				populateLinks(issues.values());
			} else {
				fieldManager.populateFields(issues.values());
				issues.remove(issue.getId());
				populateLinks(issues.values());
			}
		}
		
		issue.setSourceLinks(sourceLinks);
		issue.setTargetLinks(targetLinks);
	}
	
}
