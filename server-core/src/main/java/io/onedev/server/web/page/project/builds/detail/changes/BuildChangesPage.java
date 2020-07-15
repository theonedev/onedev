package io.onedev.server.web.page.project.builds.detail.changes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
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
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.commit.list.CommitListPanel;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;

@SuppressWarnings("serial")
public class BuildChangesPage extends BuildDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private final String baseCommitHash;
	
	private CommitListPanel commitList;
	
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
		
		add(commitList = new CommitListPanel("commits", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return query;
			}

			@Override
			public void setObject(String object) {
				query = object;
				PageParameters params = getPageParameters();
				params.set(PARAM_QUERY, query);
				CharSequence url = RequestCycle.get().urlFor(BuildChangesPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {

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
			
		});
		
		add(new WebMarkupContainer("buildStreamHelpUrl") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("href", OneDev.getInstance().getDocRoot() + "/pages/concepts.md#build-stream");
			}
			
		});
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(commitList);
	}
	
	public static PageParameters paramsOf(Build build, @Nullable String query) {
		PageParameters params = paramsOf(build);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
