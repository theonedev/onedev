package io.onedev.server.web.page.project.builds.detail.changes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.commit.CommitQuery;
import io.onedev.server.search.commit.Revision;
import io.onedev.server.search.commit.RevisionCriteria;
import io.onedev.server.web.component.commit.list.CommitListPanel;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.util.Cursor;

@SuppressWarnings("serial")
public class BuildChangesPage extends BuildDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private final String baseCommitHash;
	
	public BuildChangesPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();

		BuildManager buildManager = OneDev.getInstance(BuildManager.class);
		Build baseBuild = buildManager.findStreamPrevious(getBuild(), null);
		if (baseBuild != null)
			baseCommitHash = baseBuild.getCommitHash();
		else
			baseCommitHash = null;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(newCommitList());
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		CommitListPanel listPanel = newCommitList();
		replace(listPanel);
		target.add(listPanel);
	}
	
	private CommitListPanel newCommitList() {
		return new CommitListPanel("commits", query) {

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target, String query) {
				CharSequence url = RequestCycle.get().urlFor(BuildChangesPage.class, paramsOf(getBuild(), getCursor(), query));
				BuildChangesPage.this.query = query;
				pushState(target, url.toString(), query);
			}
			
			@Override
			protected CommitQuery getBaseQuery() {
				List<Revision> revisions = new ArrayList<>();

				if (baseCommitHash != null)
					revisions.add(new Revision(baseCommitHash, Revision.Scope.SINCE));
				revisions.add(new Revision(getBuild().getCommitHash(), Revision.Scope.UNTIL));
				return new CommitQuery(Lists.newArrayList(new RevisionCriteria(revisions)));
			}

			@Override
			protected Project getProject() {
				return BuildChangesPage.this.getProject();
			}
			
		};
	}

	public static PageParameters paramsOf(Build build, @Nullable Cursor cursor, @Nullable String query) {
		PageParameters params = paramsOf(build, cursor);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
