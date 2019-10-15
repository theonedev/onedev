package io.onedev.server.web.page.project.builds.detail.changes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.search.commit.CommitQuery;
import io.onedev.server.search.commit.Revision;
import io.onedev.server.search.commit.RevisionCriteria;
import io.onedev.server.web.component.commit.list.CommitListPanel;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.util.QueryPosition;

@SuppressWarnings("serial")
public class BuildChangesPage extends BuildDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	public BuildChangesPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		BuildManager buildManager = OneDev.getInstance(BuildManager.class);
		Build prevBuild = buildManager.findStreamPrevious(getBuild(), null);
		add(new CommitListPanel("commits", getProject(), query) {

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target, String query) {
				setResponsePage(BuildChangesPage.class, BuildChangesPage.paramsOf(getBuild(), getPosition(), query));
			}
			
			@Override
			protected CommitQuery getBaseQuery() {
				List<Revision> revisions = new ArrayList<>();

				if (prevBuild != null)
					revisions.add(new Revision(prevBuild.getCommitHash(), Revision.Scope.SINCE));
				revisions.add(new Revision(getBuild().getCommitHash(), Revision.Scope.UNTIL));
				return new CommitQuery(Lists.newArrayList(new RevisionCriteria(revisions)));
			}
			
		});
	}

	public static PageParameters paramsOf(Build build, @Nullable QueryPosition position, @Nullable String query) {
		PageParameters params = paramsOf(build, position);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
