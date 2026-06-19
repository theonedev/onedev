package io.onedev.server.search.entity.build;

import java.util.ArrayList;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

public class FuzzyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		var project = projectScope != null? projectScope.getProject() : null;
		return parse(project, value).getPredicate(projectScope, query, from, builder);
	}

	@Override
	public boolean matches(Build build) {
		return parse(build.getProject(), value).matches(build);
	}
	
	private Criteria<Build> parse(@Nullable Project project, String value) {
		var criterias = new ArrayList<Criteria<Build>>();
		criterias.add(new VersionCriteria("*" + value + "*", BuildQueryLexer.Is));
		criterias.add(new JobCriteria("*" + value + "*", BuildQueryLexer.Is));
		if (value.startsWith("#")) 
			value = value.substring(1);
		if (NumberUtils.isDigits(value))
			criterias.add(new NumberCriteria(Long.parseLong(value), BuildQueryLexer.Is));
		return new OrCriteria<>(criterias);
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
