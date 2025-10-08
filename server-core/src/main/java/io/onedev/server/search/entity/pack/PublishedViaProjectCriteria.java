package io.onedev.server.search.entity.pack;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class PublishedViaProjectCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final String projectPath;

	public PublishedViaProjectCriteria(String projectPath) {
		this.projectPath = projectPath;
	}

	public String getProjectPath() {
		return projectPath;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		return OneDev.getInstance(ProjectService.class).getPathMatchPredicate(
				builder, from.join(Pack.PROP_BUILD, JoinType.INNER).join(Build.PROP_PROJECT, JoinType.INNER), projectPath);
	}

	@Override
	public boolean matches(Pack pack) {
		if (pack.getBuild() != null) 
			return WildcardUtils.matchPath(projectPath, pack.getBuild().getProject().getPath());
		else 
			return false;
	}

	@Override
	public String toStringWithoutParens() {
		return PackQuery.getRuleName(PackQueryLexer.PublishedByProject) + " " + quote(projectPath);
	}
}
