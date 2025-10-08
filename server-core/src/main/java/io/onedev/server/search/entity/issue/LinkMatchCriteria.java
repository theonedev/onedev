package io.onedev.server.search.entity.issue;

import static io.onedev.server.search.entity.issue.IssueQuery.getRuleName;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.All;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.Any;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.Hibernate;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueLinkService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.util.LinkDescriptor;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public class LinkMatchCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	private String linkName;

	private final Criteria<Issue> criteria;
	
	private final boolean allMatch;
	
	private transient LinkDescriptor linkDescriptor;
	
	public LinkMatchCriteria(String linkName, Criteria<Issue> criteria, boolean allMatch) {
		this.linkName = linkName;
		this.criteria = criteria;
		this.allMatch = allMatch;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueLink> linkQuery = query.subquery(IssueLink.class);
		Root<IssueLink> linkRoot = linkQuery.from(IssueLink.class);
		linkQuery.select(linkRoot);
		
		LinkSpec spec = getLinkDescriptor().getSpec();
		boolean opposite = getLinkDescriptor().isOpposite();
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_SPEC), spec));
		
		if (allMatch) {
			if (opposite) {
				predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from));
				Join<Issue, Issue> linkSourceJoin = linkRoot.join(IssueLink.PROP_SOURCE, JoinType.INNER);
				predicates.add(builder.not(criteria.getPredicate(projectScope, query, linkSourceJoin, builder)));
			} else if (spec.getOpposite() != null) {
				predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from));
				Join<Issue, Issue> linkTargetJoin = linkRoot.join(IssueLink.PROP_TARGET, JoinType.INNER);
				predicates.add(builder.not(criteria.getPredicate(projectScope, query, linkTargetJoin, builder)));
			} else {
				Join<Issue, Issue> linkSourceJoin = linkRoot.join(IssueLink.PROP_SOURCE, JoinType.INNER);
				Join<Issue, Issue> linkTargetJoin = linkRoot.join(IssueLink.PROP_TARGET, JoinType.INNER);
				predicates.add(builder.or(
						builder.and(
								builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from), 
								builder.not(criteria.getPredicate(projectScope, query, linkTargetJoin, builder))),
						builder.and(
								builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from),
								builder.not(criteria.getPredicate(projectScope, query, linkSourceJoin, builder)))
				));
			}

			return builder.and(
					builder.not(builder.exists(linkQuery.where(builder.and(predicates.toArray(new Predicate[0]))))),
					new HasLinkCriteria(getLinkDescriptor()).getPredicate(projectScope, query, from, builder));
		} else {
			if (opposite) {
				predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from));
				Join<Issue, Issue> linkSourceJoin = linkRoot.join(IssueLink.PROP_SOURCE, JoinType.INNER);
				predicates.add(criteria.getPredicate(projectScope, query, linkSourceJoin, builder));
			} else if (spec.getOpposite() != null) {
				predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from));
				Join<Issue, Issue> linkTargetJoin = linkRoot.join(IssueLink.PROP_TARGET, JoinType.INNER);
				predicates.add(criteria.getPredicate(projectScope, query, linkTargetJoin, builder));
			} else {
				Join<Issue, Issue> linkSourceJoin = linkRoot.join(IssueLink.PROP_SOURCE, JoinType.INNER);
				Join<Issue, Issue> linkTargetJoin = linkRoot.join(IssueLink.PROP_TARGET, JoinType.INNER);
				predicates.add(builder.or(
						builder.and(
								builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from), 
								criteria.getPredicate(projectScope, query, linkTargetJoin, builder)),
						builder.and(
								builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from),
								criteria.getPredicate(projectScope, query, linkSourceJoin, builder))
				));
			}

			return builder.exists(linkQuery.where(builder.and(predicates.toArray(new Predicate[0]))));
		}
	}
	
	private LinkDescriptor getLinkDescriptor() {
		if (linkDescriptor == null)
			linkDescriptor = new LinkDescriptor(linkName);
		return linkDescriptor;
	}

	@Override
	public boolean matches(Issue issue) {
		if (!Hibernate.isInitialized(issue.getSourceLinks()) || !Hibernate.isInitialized(issue.getTargetLinks()))
			OneDev.getInstance(IssueLinkService.class).loadDeepLinks(issue);
		LinkSpec spec = getLinkDescriptor().getSpec();
		boolean opposite = getLinkDescriptor().isOpposite();
		if (allMatch) {
			if (opposite) {
				boolean hasLink = false;
				for (IssueLink link: issue.getSourceLinks()) {
					if (link.getSpec().equals(spec)) {
						hasLink = true;
						if (!criteria.matches(link.getSource())) 
							return false;
					}
				}
				return hasLink;
			} else if (spec.getOpposite() != null) {
				boolean hasLink = false;
				for (IssueLink link: issue.getTargetLinks()) {
					if (link.getSpec().equals(spec)) {
						hasLink = true;
						if (!criteria.matches(link.getTarget()))
							return false;
					}
				}
				return hasLink;
			} else {
				boolean hasLink = false;
				for (IssueLink link: issue.getLinks()) {
					if (link.getSpec().equals(spec)) {
						hasLink = true;
						if (!criteria.matches(link.getLinked(issue)))
							return false;
					}
				}
				return hasLink;
			}
		} else {
			if (opposite) {
				for (IssueLink link: issue.getSourceLinks()) {
					if (link.getSpec().equals(spec) && criteria.matches(link.getSource()))
						return true;
				}
				return false;
			} else if (spec.getOpposite() != null) {
				for (IssueLink link: issue.getTargetLinks()) {
					if (link.getSpec().equals(spec) && criteria.matches(link.getTarget()))
						return true;
				}
				return false;
			} else {
				for (IssueLink link: issue.getLinks()) {
					if (link.getSpec().equals(spec) && criteria.matches(link.getLinked(issue)))
						return true;
				}
				return false;
			}
		}
	}
	
	@Override
	public void onRenameLink(String oldName, String newName) {
		if (linkName.equals(oldName)) {
			linkName = newName;
			linkDescriptor = null;
		}
		criteria.onRenameLink(oldName, newName);
	}

	@Override
	public boolean isUsingLink(String linkName) {
		if (this.linkName.equals(linkName))
			return true;
		else
			return criteria.isUsingLink(linkName);
	}
	
	@Override
	public Collection<String> getUndefinedStates() {
		return criteria.getUndefinedStates();
	}

	@Override
	public Collection<String> getUndefinedFields() {
		return criteria.getUndefinedFields();
	}

	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		return criteria.getUndefinedFieldValues();
	}

	@Override
	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		return criteria.fixUndefinedStates(resolutions);
	}

	@Override
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		return criteria.fixUndefinedFields(resolutions);
	}

	@Override
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		return criteria.fixUndefinedFieldValues(resolutions);
	}

	@Override
	public String toStringWithoutParens() {
		return (allMatch?getRuleName(All):getRuleName(Any)) 
				+ " " + quote(linkName) + " "
				+ getRuleName(IssueQueryLexer.Matching) 
				+ "(" + criteria.toString() + ")";
	}

}
