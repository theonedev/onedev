package io.onedev.server.web.page.project.builds.detail.issues;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Build;
import io.onedev.server.search.entity.issue.FixedInCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.web.component.issue.list.IssueListPanel;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QueryPosition;

@SuppressWarnings("serial")
public class FixedIssuesPage extends BuildDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_CURRENT_PAGE = "currentPage";

	private String query;
	
	public FixedIssuesPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new IssueListPanel("issues", getProject(), query) {

			@Override
			protected IssueQuery getBaseQuery() {
				return new IssueQuery(new FixedInCriteria(getBuild()), new ArrayList<>());
			}
			
			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {
					
					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(getBuild(), getPosition(), query);
						params.add(PARAM_CURRENT_PAGE, currentPage+1);
						return params;
					}
					
					@Override
					public int getCurrentPage() {
						return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
					}
					
				};
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target, String query) {
				setResponsePage(FixedIssuesPage.class, FixedIssuesPage.paramsOf(getBuild(), getPosition(), query));
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
