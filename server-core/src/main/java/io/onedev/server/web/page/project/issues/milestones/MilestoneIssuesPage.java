package io.onedev.server.web.page.project.issues.milestones;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.MilestoneCriteria;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.component.issue.list.IssueListPanel;
import io.onedev.server.web.component.issue.statestats.StateStatsBar;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class MilestoneIssuesPage extends MilestoneDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_PAGE = "page";
	
	private String query;
	
	private IssueListPanel issueList;
	
	public MilestoneIssuesPage(PageParameters params) {
		super(params);
		
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new StateStatsBar("issueStats", new LoadableDetachableModel<Map<String, Integer>>() {

			@Override
			protected Map<String, Integer> load() {
				return getMilestone().getStateStats(getProject());
			}
			
		}) {

			@Override
			protected Link<Void> newStateLink(String componentId, String state) {
				String query = new IssueQuery(new StateCriteria(state, IssueQueryLexer.Is)).toString();
				PageParameters params = paramsOf(getProject(), getMilestone(), query);
				return new ViewStateAwarePageLink<Void>(componentId, MilestoneIssuesPage.class, params);
			}
			
		});
		
		add(issueList = new IssueListPanel("issues", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(MilestoneIssuesPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {
			
			@Override
			protected IssueQuery getBaseQuery() {
				return new IssueQuery(new MilestoneCriteria(getMilestone().getName()), new ArrayList<>());
			}
	
			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {
					
					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(getProject(), getMilestone(), query);
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
			protected ProjectScope getProjectScope() {
				return new ProjectScope(getProject(), true, null);
			}
			
		});
		
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		add(issueList);
	}
	
	public static PageParameters paramsOf(Project project, Milestone milestone, @Nullable String query) {
		PageParameters params = paramsOf(project, milestone);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
