package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;

public class LinkMatchCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final LinkSpec linkSpec;
	
	private final boolean opposite;

	private final IssueCriteria criteria;
	
	private final boolean allMatch;
	
	public LinkMatchCriteria(LinkSpec linkSpec, boolean opposite, IssueCriteria criteria, boolean allMatch) {
		this.linkSpec = linkSpec;
		this.opposite = opposite;
		this.criteria = criteria;
		this.allMatch = allMatch;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueLink> linkQuery = query.subquery(IssueLink.class);
		Root<IssueLink> linkRoot = linkQuery.from(IssueLink.class);
		linkQuery.select(linkRoot);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_SPEC), linkSpec));
		
		if (allMatch) {
			if (opposite) {
				predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from));
				Join<Issue, Issue> linkSourceJoin = linkRoot.join(IssueLink.PROP_SOURCE, JoinType.INNER);
				predicates.add(builder.not(criteria.getPredicate(query, linkSourceJoin, builder)));
			} else if (linkSpec.getOpposite() != null) {
				predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from));
				Join<Issue, Issue> linkTargetJoin = linkRoot.join(IssueLink.PROP_TARGET, JoinType.INNER);
				predicates.add(builder.not(criteria.getPredicate(query, linkTargetJoin, builder)));
			} else {
				Join<Issue, Issue> linkSourceJoin = linkRoot.join(IssueLink.PROP_SOURCE, JoinType.INNER);
				Join<Issue, Issue> linkTargetJoin = linkRoot.join(IssueLink.PROP_TARGET, JoinType.INNER);
				predicates.add(builder.or(
						builder.and(
								builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from), 
								builder.not(criteria.getPredicate(query, linkTargetJoin, builder))),
						builder.and(
								builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from),
								builder.not(criteria.getPredicate(query, linkSourceJoin, builder)))
				));
			}

			return builder.and(
					builder.not(builder.exists(linkQuery.where(builder.and(predicates.toArray(new Predicate[0]))))),
					new HasLinkCriteria(linkSpec, opposite).getPredicate(query, from, builder));
		} else {
			if (opposite) {
				predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from));
				Join<Issue, Issue> linkSourceJoin = linkRoot.join(IssueLink.PROP_SOURCE, JoinType.INNER);
				predicates.add(criteria.getPredicate(query, linkSourceJoin, builder));
			} else if (linkSpec.getOpposite() != null) {
				predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from));
				Join<Issue, Issue> linkTargetJoin = linkRoot.join(IssueLink.PROP_TARGET, JoinType.INNER);
				predicates.add(criteria.getPredicate(query, linkTargetJoin, builder));
			} else {
				Join<Issue, Issue> linkSourceJoin = linkRoot.join(IssueLink.PROP_SOURCE, JoinType.INNER);
				Join<Issue, Issue> linkTargetJoin = linkRoot.join(IssueLink.PROP_TARGET, JoinType.INNER);
				predicates.add(builder.or(
						builder.and(
								builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from), 
								criteria.getPredicate(query, linkTargetJoin, builder)),
						builder.and(
								builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from),
								criteria.getPredicate(query, linkSourceJoin, builder))
				));
			}

			return builder.exists(linkQuery.where(builder.and(predicates.toArray(new Predicate[0]))));
		}
	}

	@Override
	public boolean matches(Issue issue) {
		if (allMatch) {
			if (opposite) {
				boolean hasLink = false;
				for (IssueLink link: issue.getSourceLinks()) {
					if (link.getSpec().equals(linkSpec)) {
						hasLink = true;
						if (!criteria.matches(link.getSource())) 
							return false;
					}
				}
				return hasLink;
			} else if (linkSpec.getOpposite() != null) {
				boolean hasLink = false;
				for (IssueLink link: issue.getTargetLinks()) {
					if (link.getSpec().equals(linkSpec)) {
						hasLink = true;
						if (!criteria.matches(link.getTarget()))
							return false;
					}
				}
				return hasLink;
			} else {
				boolean hasLink = false;
				for (IssueLink link: issue.getLinks()) {
					if (link.getSpec().equals(linkSpec)) {
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
					if (link.getSpec().equals(linkSpec) && criteria.matches(link.getSource()))
						return true;
				}
				return false;
			} else if (linkSpec.getOpposite() != null) {
				for (IssueLink link: issue.getTargetLinks()) {
					if (link.getSpec().equals(linkSpec) && criteria.matches(link.getTarget()))
						return true;
				}
				return false;
			} else {
				for (IssueLink link: issue.getLinks()) {
					if (link.getSpec().equals(linkSpec) && criteria.matches(link.getLinked(issue)))
						return true;
				}
				return false;
			}
		}
	}

	@Override
	public String toStringWithoutParens() {
		return allMatch?IssueQuery.getRuleName(IssueQueryLexer.All):IssueQuery.getRuleName(IssueQueryLexer.Any) 
				+ quote(linkSpec.getName(opposite)) + " "
				+ IssueQuery.getRuleName(IssueQueryLexer.Matching) 
				+ "(" + criteria.toString() + ")";
	}

}
