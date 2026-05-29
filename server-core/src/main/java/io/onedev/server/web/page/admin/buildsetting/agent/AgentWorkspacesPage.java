package io.onedev.server.web.page.admin.buildsetting.agent;

import java.io.Serializable;
import java.util.ArrayList;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Agent;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.workspace.RanOnCriteria;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.web.component.workspace.list.WorkspaceListPanel;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;

public class AgentWorkspacesPage extends AgentDetailPage {

	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_PAGE = "page";

	private String query;
	
	private WorkspaceListPanel workspaceList;
	
	public AgentWorkspacesPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(workspaceList = new WorkspaceListPanel("workspaces", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(AgentWorkspacesPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {

			@Override
			protected WorkspaceQuery getBaseQuery() {
				return new WorkspaceQuery(new RanOnCriteria(getAgent().getName()), new ArrayList<>());
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new ParamPagingHistorySupport() {
					
					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(getAgent(), query);
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
				return null;
			}

		});
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(workspaceList);
	}
	
	public static PageParameters paramsOf(Agent agent, @Nullable String query) {
		PageParameters params = paramsOf(agent);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
		
}
