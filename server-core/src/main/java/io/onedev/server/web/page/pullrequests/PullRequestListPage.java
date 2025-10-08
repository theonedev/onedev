package io.onedev.server.web.page.pullrequests;

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
import io.onedev.server.model.support.administration.GlobalPullRequestSetting;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.pullrequest.list.PullRequestListPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.PersonalQuerySupport;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.util.NamedPullRequestQueriesBean;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;

public class PullRequestListPage extends LayoutPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private SavedQueriesPanel<NamedPullRequestQuery> savedQueries;
	
	private PullRequestListPanel requestList;
	
	public PullRequestListPage(PageParameters params) {
		super(params);
		query = getPageParameters().get(PARAM_QUERY).toOptionalString();
	}
	
	private static GlobalPullRequestSetting getPullRequestSetting() {
		return OneDev.getInstance(SettingService.class).getPullRequestSetting();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(savedQueries = new SavedQueriesPanel<NamedPullRequestQuery>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedPullRequestQuery> newNamedQueriesBean() {
				return new NamedPullRequestQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedPullRequestQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, PullRequestListPage.class, 
						PullRequestListPage.paramsOf(namedQuery.getQuery(), 0));
			}

			@Override
			protected QueryPersonalization<NamedPullRequestQuery> getQueryPersonalization() {
				return getLoginUser().getPullRequestQueryPersonalization();
			}

			@Override
			protected void onSaveCommonQueries(ArrayList<NamedPullRequestQuery> queries) {
				var oldAuditContent = VersionedXmlDoc.fromBean(getPullRequestSetting().getNamedQueries()).toXML();
				getPullRequestSetting().setNamedQueries(queries);
				var newAuditContent = VersionedXmlDoc.fromBean(getPullRequestSetting().getNamedQueries()).toXML();
				OneDev.getInstance(SettingService.class).savePullRequestSetting(getPullRequestSetting());
				auditService.audit(null, "changed pull request queries", oldAuditContent, newAuditContent);
			}

			@Override
			protected ArrayList<NamedPullRequestQuery> getCommonQueries() {
				return (ArrayList<NamedPullRequestQuery>) getPullRequestSetting().getNamedQueries();
			}

		});

		add(requestList = new PullRequestListPanel("pullRequests", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(PullRequestListPage.class, params);
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
								return new SaveQueryPanel(id, new PersonalQuerySupport() {

									@Override
									public void onSave(AjaxRequestTarget target, String name) {
										QueryPersonalization<NamedPullRequestQuery> queryPersonalization = getLoginUser().getPullRequestQueryPersonalization();
										NamedPullRequestQuery namedQuery = NamedQuery.find(queryPersonalization.getQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedPullRequestQuery(name, query);
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
										GlobalPullRequestSetting pullRequestSetting = getPullRequestSetting();
										NamedPullRequestQuery namedQuery = pullRequestSetting.getNamedQuery(name);
										String oldAuditContent = null;
										String verb;
										if (namedQuery == null) {
											namedQuery = new NamedPullRequestQuery(name, query);
											pullRequestSetting.getNamedQueries().add(namedQuery);
											verb = "created";
										} else {
											oldAuditContent = VersionedXmlDoc.fromBean(namedQuery).toXML();
											namedQuery.setQuery(query);
											verb = "changed";
										}
										var newAuditContent = VersionedXmlDoc.fromBean(namedQuery).toXML();
										OneDev.getInstance(SettingService.class).savePullRequestSetting(pullRequestSetting);
										auditService.audit(null, verb + " pull request query \"" + name + "\"", oldAuditContent, newAuditContent);
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
		target.add(requestList);
	}
	
	public static PageParameters paramsOf(@Nullable String query, int page) {
		PageParameters params = new PageParameters();
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_PAGE, page);
		return params;
	}
	
	@Override
	protected String getPageTitle() {
		return _T("Pull Requests") + " - " + OneDev.getInstance(SettingService.class).getBrandingSetting().getName();
	}
	
	public static PageParameters paramsOf(int page) {
		String query = null;
		User user = SecurityUtils.getAuthUser();
		if (user != null && !user.getPullRequestQueryPersonalization().getQueries().isEmpty())
			query = user.getPullRequestQueryPersonalization().getQueries().iterator().next().getQuery();
		else if (!getPullRequestSetting().getNamedQueries().isEmpty())
			query = getPullRequestSetting().getNamedQueries().iterator().next().getQuery();
		return paramsOf(query, page);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Pull Requests"));
	}
	
}
