package io.onedev.server.web.page.project.issues.detail;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.FixedIssueCriteria;
import io.onedev.server.web.component.build.list.BuildListPanel;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.Cursor;

@SuppressWarnings("serial")
public class IssueBuildsPage extends IssueDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_PAGE = "page";

	private String query;
	
	public IssueBuildsPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(newBuildList());
	}
	
	private BuildListPanel newBuildList() {
		return new BuildListPanel("builds", query, 0) {

			@Override
			protected BuildQuery getBaseQuery() {
				return new BuildQuery(new FixedIssueCriteria(getIssue()), new ArrayList<>());
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {
					
					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(getIssue(), getCursor(), query);
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
				CharSequence url = RequestCycle.get().urlFor(IssueBuildsPage.class, paramsOf(getIssue(), getCursor(), query));
				IssueBuildsPage.this.query = query;
				pushState(target, url.toString(), query);
			}

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}

		};
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		BuildListPanel listPanel = newBuildList();
		replace(listPanel);
		target.add(listPanel);
	}
	
	public static PageParameters paramsOf(Issue issue, @Nullable Cursor cursor, @Nullable String query) {
		PageParameters params = paramsOf(issue, cursor);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
