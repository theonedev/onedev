package io.onedev.server.web.page.project.builds.detail.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.cache.BuildInfoManager;
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

		BuildInfoManager buildInfoManager = OneDev.getInstance(BuildInfoManager.class);
		Collection<ObjectId> prevCommitIds = buildInfoManager.getPrevCommits(getProject(), getBuild().getId());
		if (prevCommitIds != null) {
			Fragment fragment = new Fragment("content", "availableFrag", this);
			fragment.add(new Label("jobName", getBuild().getJobName()));
			fragment.add(new CommitListPanel("commits", getProject(), query) {

				@Override
				protected void onQueryUpdated(AjaxRequestTarget target, String query) {
					setResponsePage(BuildChangesPage.class, BuildChangesPage.paramsOf(getBuild(), getPosition(), query));
				}
				
				@Override
				protected CommitQuery getBaseQuery() {
					List<Revision> revisions = new ArrayList<>();

					for (ObjectId prevCommitId: prevCommitIds)
						revisions.add(new Revision(prevCommitId.name(), Revision.Scope.SINCE));
					revisions.add(new Revision(getBuild().getCommitHash(), Revision.Scope.UNTIL));
					return new CommitQuery(Lists.newArrayList(new RevisionCriteria(revisions)));
				}
				
			});
			add(fragment);
		} else {
			add(new Fragment("content", "notAvailableFrag", this));
		}
	}

	public static PageParameters paramsOf(Build build, @Nullable QueryPosition position, @Nullable String query) {
		PageParameters params = paramsOf(build, position);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
