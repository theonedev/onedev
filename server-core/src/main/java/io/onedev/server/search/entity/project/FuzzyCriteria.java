package io.onedev.server.search.entity.project;

import com.google.common.base.Splitter;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;

public class FuzzyCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(query, from, builder);
	}

	@Override
	public boolean matches(Project project) {
		return parse(value).matches(project);
	}
	
	private Criteria<Project> parse(String value) {
		var criterias = new ArrayList<Criteria<Project>>();
		for (var part: Splitter.on(' ').omitEmptyStrings().trimResults().split(value)) {
			if (Project.get() != null)
				criterias.add(new NameCriteria("*" + part + "*"));
			else
				criterias.add(new PathCriteria("**/*" + part + "*/**"));
		}
		return new AndCriteria<>(criterias);
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
