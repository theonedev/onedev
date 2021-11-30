package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;

public class LinkCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final LinkSpec linkSpec;
	
	private final boolean opposite;
	
	private final Issue issue;
	
	private final String value;
	
	public LinkCriteria(LinkSpec linkSpec, boolean opposite, @Nullable Project project, String value) {
		this.linkSpec = linkSpec;
		this.opposite = opposite;
		issue = EntityQuery.getIssue(project, value);
		this.value = value;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, Root<Issue> root, CriteriaBuilder builder) {
		Subquery<IssueLink> linkQuery = query.subquery(IssueLink.class);
		Root<IssueLink> link = linkQuery.from(IssueLink.class);
		linkQuery.select(link);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(link.get(IssueLink.PROP_SPEC), linkSpec));
		
		if (opposite) {
			predicates.add(builder.equal(link.get(IssueLink.PROP_TARGET), root));
			predicates.add(builder.equal(link.get(IssueLink.PROP_SOURCE), issue));
		} else if (linkSpec.getOpposite() != null) {
			predicates.add(builder.equal(link.get(IssueLink.PROP_SOURCE), root));
			predicates.add(builder.equal(link.get(IssueLink.PROP_TARGET), issue));
		} else {
			predicates.add(builder.or(
					builder.and(
							builder.equal(link.get(IssueLink.PROP_SOURCE), root), 
							builder.equal(link.get(IssueLink.PROP_TARGET), issue)),
					builder.and(
							builder.equal(link.get(IssueLink.PROP_SOURCE), issue), 
							builder.equal(link.get(IssueLink.PROP_TARGET), root))
			));
		}

		return builder.exists(linkQuery.where(builder.and(predicates.toArray(new Predicate[0]))));
	}

	@Override
	public boolean matches(Issue issue) {
		if (opposite) {
			for (IssueLink link: issue.getSourceLinks()) {
				if (link.getSpec().equals(linkSpec) && link.getSource().equals(LinkCriteria.this.issue))
					return true;
			}
			return false;
		} else if (linkSpec.getOpposite() != null) {
			for (IssueLink link: issue.getTargetLinks()) {
				if (link.getSpec().equals(linkSpec) && link.getTarget().equals(LinkCriteria.this.issue))
					return true;
			}
			return false;
		} else {
			for (IssueLink link: issue.getLinks()) {
				if (link.getSpec().equals(linkSpec) && link.getLinked(issue).equals(LinkCriteria.this.issue))
					return true;
			}
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return quote(linkSpec.getName(opposite)) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(value);
	}

	@Override
	public void fill(Issue issue) {
		IssueLink link = new IssueLink();
		link.setSpec(linkSpec);
		if (opposite) {
			link.setTarget(issue);
			link.setSource(LinkCriteria.this.issue);
			issue.getSourceLinks().add(link);
		} else {
			link.setSource(issue);
			link.setTarget(LinkCriteria.this.issue);
			issue.getTargetLinks().add(link);
		}
	}

}
