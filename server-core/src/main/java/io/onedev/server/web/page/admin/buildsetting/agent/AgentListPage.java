package io.onedev.server.web.page.admin.buildsetting.agent;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.NamedAgentQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.administration.AgentSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.NamedAgentQueriesBean;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.jspecify.annotations.Nullable;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;

public class AgentListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private SavedQueriesPanel<NamedAgentQuery> savedQueries;
	
	private Component agentList;
	
	public AgentListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
	}

	private static AgentSetting getAgentSetting() {
		return OneDev.getInstance(SettingService.class).getAgentSetting();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(savedQueries = new SavedQueriesPanel<NamedAgentQuery>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedAgentQuery> newNamedQueriesBean() {
				return new NamedAgentQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedAgentQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, AgentListPage.class, 
						AgentListPage.paramsOf(namedQuery.getQuery(), 0));
			}

			@Override
			protected QueryPersonalization<NamedAgentQuery> getQueryPersonalization() {
				return null;
			}

			@Override
			protected ArrayList<NamedAgentQuery> getCommonQueries() {
				return (ArrayList<NamedAgentQuery>) getAgentSetting().getNamedQueries();
			}

			@Override
			protected void onSaveCommonQueries(ArrayList<NamedAgentQuery> namedQueries) {
				getAgentSetting().setNamedQueries(namedQueries);
				OneDev.getInstance(SettingService.class).saveAgentSetting(getAgentSetting());
			}

		});
		
		add(agentList = new AgentListPanel("agents", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(AgentListPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new ParamPagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(query, 0);
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
			protected QuerySaveSupport getQuerySaveSupport() {
				return new QuerySaveSupport() {

					@Override
					public void onSaveQuery(AjaxRequestTarget target, String query) {
						new ModalPanel(target)  {

							@Override
							protected Component newContent(String id) {
								return new SaveQueryPanel(id, null) {

									@Override
									protected void onSave(AjaxRequestTarget target, String name) {
										AgentSetting agentSetting = getAgentSetting();
										NamedAgentQuery namedQuery = agentSetting.getNamedQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedAgentQuery(name, query);
											agentSetting.getNamedQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(SettingService.class).saveAgentSetting(agentSetting);
										target.add(savedQueries);
										close();
									}

									@Override
									protected void onCancel(AjaxRequestTarget target) {
										close();
									}
									
								};
							}
							
						};
					}

					@Override
					public boolean isSavedQueriesVisible() {
						savedQueries.configure();
						return savedQueries.isVisible();
					}

				};
			}

		});
		
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(agentList);
	}

	public static PageParameters paramsOf(@Nullable String query, int page) {
		PageParameters params = new PageParameters();
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_PAGE, page);
		return params;
	}
	
	public static PageParameters paramsOf(int page) {
		String query = null;
		
		if (!getAgentSetting().getNamedQueries().isEmpty())
			query = getAgentSetting().getNamedQueries().iterator().next().getQuery();
		
		return paramsOf(query, page);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AgentCssResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Agents"));
	}

}
