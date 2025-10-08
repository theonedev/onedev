package io.onedev.server.web.page.builds;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.build.list.BuildListPanel;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.PersonalQuerySupport;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.util.NamedBuildQueriesBean;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;

public class BuildListPage extends LayoutPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
		
	private String query;
		
	private SavedQueriesPanel<NamedBuildQuery> savedQueries;
	
	private BuildListPanel buildList;
	
	public BuildListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
	}

	private static GlobalBuildSetting getBuildSetting() {
		return OneDev.getInstance(SettingService.class).getBuildSetting();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(savedQueries = new SavedQueriesPanel<NamedBuildQuery>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedBuildQuery> newNamedQueriesBean() {
				return new NamedBuildQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedBuildQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, BuildListPage.class, 
						BuildListPage.paramsOf(namedQuery.getQuery(), 0));
			}

			@Override
			protected QueryPersonalization<NamedBuildQuery> getQueryPersonalization() {
				return getLoginUser().getBuildQueryPersonalization();
			}

			@Override
			protected ArrayList<NamedBuildQuery> getCommonQueries() {
				return (ArrayList<NamedBuildQuery>) getBuildSetting().getNamedQueries();
			}

			@Override
			protected void onSaveCommonQueries(ArrayList<NamedBuildQuery> namedQueries) {
				var oldAuditContent = VersionedXmlDoc.fromBean(getBuildSetting().getNamedQueries()).toXML();
				getBuildSetting().setNamedQueries(namedQueries);
				var newAuditContent = VersionedXmlDoc.fromBean(getBuildSetting().getNamedQueries()).toXML();
				OneDev.getInstance(SettingService.class).saveBuildSetting(getBuildSetting());
				auditService.audit(null, "changed build queries", oldAuditContent, newAuditContent);
			}

		});
		
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
				CharSequence url = RequestCycle.get().urlFor(BuildListPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}, true, true) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new ParamPagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int currentPage) {
						return paramsOf(query, currentPage+1);
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
								return new SaveQueryPanel(id, new PersonalQuerySupport() {

									@Override
									public void onSave(AjaxRequestTarget target, String name) {
										QueryPersonalization<NamedBuildQuery> queryPersonalization = getLoginUser().getBuildQueryPersonalization();
										NamedBuildQuery namedQuery = NamedQuery.find(queryPersonalization.getQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedBuildQuery(name, query);
											queryPersonalization.getQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(UserService.class).update(getLoginUser(), null);
										target.add(savedQueries);
										close();
									}
									
								}) {

									@Override
									protected void onSave(AjaxRequestTarget target, String name) {
										GlobalBuildSetting buildSetting = getBuildSetting();
										NamedBuildQuery namedQuery = buildSetting.getNamedQuery(name);
										String oldAuditContent = null;
										String verb;
										if (namedQuery == null) {
											namedQuery = new NamedBuildQuery(name, query);
											buildSetting.getNamedQueries().add(namedQuery);
											verb = "created";
										} else {
											oldAuditContent = VersionedXmlDoc.fromBean(namedQuery).toXML();
											namedQuery.setQuery(query);
											verb = "changed";
										}
										var newAuditContent = VersionedXmlDoc.fromBean(namedQuery).toXML();
										OneDev.getInstance(SettingService.class).saveBuildSetting(buildSetting);
										auditService.audit(null, verb + " build query \"" + name + "\"", oldAuditContent, newAuditContent);
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
		target.add(buildList);
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
		User user = SecurityUtils.getAuthUser();
		if (user != null && !user.getBuildQueryPersonalization().getQueries().isEmpty()) 
			query = user.getBuildQueryPersonalization().getQueries().iterator().next().getQuery();
		else if (!getBuildSetting().getNamedQueries().isEmpty())
			query = getBuildSetting().getNamedQueries().iterator().next().getQuery();
			
		return paramsOf(query, page);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Builds"));
	}
	
	@Override
	protected String getPageTitle() {
		return _T("Builds") + " - " + OneDev.getInstance(SettingService.class).getBrandingSetting().getName();
	}
	
}
