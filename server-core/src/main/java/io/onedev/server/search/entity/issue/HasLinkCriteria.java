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
import io.onedev.server.util.LinkSide;
import io.onedev.server.util.criteria.Criteria;

public class HasLinkCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	private String linkName;
	
	private transient LinkSide linkSide;
	
	public HasLinkCriteria(String linkName) {
		this.linkName = linkName;
	}
	
	public HasLinkCriteria(LinkSide linkSide) {
		linkName = linkSide.getName();
		this.linkSide = linkSide;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueLink> linkQuery = query.subquery(IssueLink.class);
		Root<IssueLink> linkRoot = linkQuery.from(IssueLink.class);
		linkQuery.select(linkRoot);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_SPEC), getLinkSide().getSpec()));
		
		if (getLinkSide().isOpposite()) {
			predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from));
		} else if (getLinkSide().getSpec().getOpposite() != null) {
			predicates.add(builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from));
		} else {
			predicates.add(builder.or(
					builder.equal(linkRoot.get(IssueLink.PROP_SOURCE), from), 
					builder.equal(linkRoot.get(IssueLink.PROP_TARGET), from))
			);
		}

		return builder.exists(linkQuery.where(builder.and(predicates.toArray(new Predicate[0]))));
	}

	private LinkSide getLinkSide() {
		if (linkSide == null) 
			linkSide = new LinkSide(linkName);
		return linkSide;
	}
	
	@Override
	public boolean matches(Issue issue) {
		LinkSpec spec = getLinkSide().getSpec();
		if (getLinkSide().isOpposite()) 
			return issue.getSourceLinks().stream().anyMatch(it->it.getSpec().equals(spec));
		else if (spec.getOpposite() != null) 
			return issue.getTargetLinks().stream().anyMatch(it->it.getSpec().equals(spec));
		else 
			return issue.getLinks().stream().anyMatch(it->it.getSpec().equals(spec));
	}
	
	@Override
	public void onRenameLink(String oldName, String newName) {
		if (linkName.equals(oldName)) {
			linkName = newName;
			linkSide = null;
		}
	}

	@Override
	public boolean isUsingLink(String linkName) {
		return this.linkName.equals(linkName);
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.HasAny) + " " + quote(linkName);
	}

}
