package io.onedev.server.web.page.project.issues.detail;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.FixedIssueCriteria;
import io.onedev.server.web.component.build.list.BuildListPanel;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class IssueBuildsPage extends IssueDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_PAGE = "page";

	private String query;
	
	private BuildListPanel buildList;
	
	public IssueBuildsPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(buildList = new BuildListPanel("builds", new IModel<String>() {

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
				params.remove(PARAM_PAGE);
				CharSequence url = RequestCycle.get().urlFor(IssueBuildsPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}, 0) {

			@Override
			protected BuildQuery getBaseQuery() {
				return new BuildQuery(new FixedIssueCriteria(getIssue()), new ArrayList<>());
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {
					
					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(getIssue(), query);
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
			protected Project getProject() {
				return getIssue().getProject();
			}

		});
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(buildList);
	}
	
	public static PageParameters paramsOf(Issue issue, @Nullable String query) {
		PageParameters params = paramsOf(issue);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
