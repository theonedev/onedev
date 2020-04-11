package io.onedev.server.web.page.project.builds.detail.issues;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.FixedInBuildCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.web.component.issue.list.IssueListPanel;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.Cursor;

@SuppressWarnings("serial")
public class FixedIssuesPage extends BuildDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_PAGE = "page";

	private String query;
	
	public FixedIssuesPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(newIssueList());
	}
	
	private IssueListPanel newIssueList() {
		return new IssueListPanel("issues", query) {

			@Override
			protected IssueQuery getBaseQuery() {
				return new IssueQuery(new FixedInBuildCriteria(getBuild()), new ArrayList<>());
			}
			
			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {
					
					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(getBuild(), getCursor(), query);
						params.add(PARAM_PAGE, currentPage+1);
						return params;
					}
					
					@Override
					public int getCurrentPage() {
						return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
					}
					
				};
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target, String query) {
				CharSequence url = RequestCycle.get().urlFor(ProjectIssueListPage.class, paramsOf(getBuild(), getCursor(), query));
				FixedIssuesPage.this.query = query;
				pushState(target, url.toString(), query);
			}

			@Override
			protected Project getProject() {
				return FixedIssuesPage.this.getProject();
			}
			
		};
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		IssueListPanel listPanel = newIssueList();
		replace(listPanel);
		target.add(listPanel);
	}

	public static PageParameters paramsOf(Build build, @Nullable Cursor cursor, @Nullable String query) {
		PageParameters params = paramsOf(build, cursor);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
