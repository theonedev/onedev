package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;

public class HasLinkCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final LinkSpec linkSpec;
	
	private final boolean opposite;
	
	public HasLinkCriteria(LinkSpec linkSpec, boolean opposite) {
		this.linkSpec = linkSpec;
		this.opposite = opposite;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueLink> linkQuery = query.subquery(IssueLink.class);
		Root<IssueLink> linkRoot = linkQuery.from(IssueLink.class);
		linkQuery.select(linkRoot);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_SPEC), linkSpec));
		
		if (opposite) {
			predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from));
		} else if (linkSpec.getOpposite() != null) {
			predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from));
		} else {
			predicates.add(builder.or(
					builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from), 
					builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from))
			);
		}

		return builder.exists(linkQuery.where(builder.and(predicates.toArray(new Predicate[0]))));
	}

	@Override
	public boolean matches(Issue issue) {
		if (opposite) 
			return issue.getSourceLinks().stream().anyMatch(it->it.getSpec().equals(linkSpec));
		else if (linkSpec.getOpposite() != null) 
			return issue.getTargetLinks().stream().anyMatch(it->it.getSpec().equals(linkSpec));
		else 
			return issue.getLinks().stream().anyMatch(it->it.getSpec().equals(linkSpec));
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.HasAny) + " " + quote(linkSpec.getName(opposite));
	}

}
