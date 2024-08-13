package io.onedev.server.web.page.packs;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.administration.GlobalPackSetting;
import io.onedev.server.model.support.pack.NamedPackQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Translation;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.pack.list.PackListPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.PersonalQuerySupport;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.util.NamedPackQueriesBean;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class PackListPage extends LayoutPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private SavedQueriesPanel<NamedPackQuery> savedQueries;
	
	private PackListPanel packList;
	
	public PackListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
	}

	private static GlobalPackSetting getPackSetting() {
		return OneDev.getInstance(SettingManager.class).getPackSetting();		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(savedQueries = new SavedQueriesPanel<>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedPackQuery> newNamedQueriesBean() {
				return new NamedPackQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedPackQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, PackListPage.class,
						PackListPage.paramsOf(namedQuery.getQuery(), 0));
			}

			@Override
			protected QueryPersonalization<NamedPackQuery> getQueryPersonalization() {
				return getLoginUser().getPackQueryPersonalization();
			}

			@Override
			protected ArrayList<NamedPackQuery> getCommonQueries() {
				return (ArrayList<NamedPackQuery>) getPackSetting().getNamedQueries();
			}

			@Override
			protected void onSaveCommonQueries(ArrayList<NamedPackQuery> namedQueries) {
				getPackSetting().setNamedQueries(namedQueries);
				OneDev.getInstance(SettingManager.class).savePackSetting(getPackSetting());
			}

		});
		
		add(packList = new PackListPanel("packs", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(PackListPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}, true) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {

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
										QueryPersonalization<NamedPackQuery> queryPersonalization = getLoginUser().getPackQueryPersonalization();
										NamedPackQuery namedQuery = NamedQuery.find(queryPersonalization.getQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedPackQuery(name, query);
											queryPersonalization.getQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(UserManager.class).update(getLoginUser(), null);
										target.add(savedQueries);
										close();
									}
									
								}) {

									@Override
									protected void onSave(AjaxRequestTarget target, String name) {
										GlobalPackSetting packSetting = getPackSetting();
										NamedPackQuery namedQuery = packSetting.getNamedQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedPackQuery(name, query);
											packSetting.getNamedQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(SettingManager.class).savePackSetting(packSetting);
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
		target.add(packList);
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
		if (user != null && !user.getPackQueryPersonalization().getQueries().isEmpty()) 
			query = user.getPackQueryPersonalization().getQueries().iterator().next().getQuery();
		else if (!getPackSetting().getNamedQueries().isEmpty())
			query = getPackSetting().getNamedQueries().iterator().next().getQuery();
		return paramsOf(query, page);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, Translation.get("Packages"));
	}
	
	@Override
	protected String getPageTitle() {
		return Translation.get("Packages") + " - " + OneDev.getInstance(SettingManager.class).getBrandingSetting().getName();
	}
	
}
