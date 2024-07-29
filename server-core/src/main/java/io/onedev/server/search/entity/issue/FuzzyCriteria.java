package io.onedev.server.search.entity.issue;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Issue;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;

public class FuzzyCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(query, from, builder);
	}

	@Override
	public boolean matches(Issue issue) {
		return parse(value).matches(issue);
	}
	
	private Criteria<Issue> parse(String value) {
		var terms = normalizeFuzzyQuery(value);
		var titleCriterias = new ArrayList<Criteria<Issue>>(); 
		var descriptionCriterias = new ArrayList<Criteria<Issue>>();
		var commentCriterias = new ArrayList<Criteria<Issue>>();
		for (var term: terms) {
			titleCriterias.add(new TitleCriteria(term));
			descriptionCriterias.add(new DescriptionCriteria(term));
			commentCriterias.add(new CommentCriteria(term));
		}
		return new OrCriteria<>(new AndCriteria<>(titleCriterias), new AndCriteria<>(descriptionCriterias), new AndCriteria<>(commentCriterias));
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
