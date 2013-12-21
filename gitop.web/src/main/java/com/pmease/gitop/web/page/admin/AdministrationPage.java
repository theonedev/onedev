package com.pmease.gitop.web.page.admin;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.gitop.web.page.AbstractLayoutPage;
import com.pmease.gitop.web.page.admin.api.IAdministrationTab;
import com.pmease.gitop.web.page.admin.api.IAdministrationTab.Category;

@SuppressWarnings("serial")
public class AdministrationPage extends AbstractLayoutPage {

	private static final String OVERVIEW = "OVERVIEW";
	
	final IAdministrationTab activeTab;
	
	public AdministrationPage(PageParameters params) {
		String tabId = params.get("tabId").toString();
		if (tabId == null) {
			tabId = OVERVIEW;
		}
		
		activeTab = findTab(tabId);
	}
	
	@Override
	protected boolean isPermitted() {
		return currentUser().isPresent() && currentUser().get().isAdmin();
	}
	
	private IAdministrationTab findTab(String tabId) {
		if (Strings.isNullOrEmpty(tabId)) {
			return null;
		}
		
		List<IAdministrationTab> tabs = getAllTabs();
		for (IAdministrationTab each : tabs) {
			if (Objects.equal(each.getTabId(), tabId.toLowerCase())) {
				return each;
			}
		}
		
		return null;
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new BookmarkablePageLink<Void>("overviewlink", AdministrationPage.class));
		
		add(createSidebarNavs());
		add(createContentPanel());
	}
	
	private Component createContentPanel() {
		if (activeTab == null) {
			return new OverviewPanel("panel");
		} else {
			return activeTab.getPanel("panel");
		}
	}
	
	private Component createSidebarNavs() {
		Loop loop = new Loop("categories", Category.values().length) {

			@Override
			protected void populateItem(LoopItem item) {
				final Category category = Category.values()[item.getIndex()];
				item.add(new Label("name", category.name()));
				IModel<List<IAdministrationTab>> model = new LoadableDetachableModel<List<IAdministrationTab>>() {

					@Override
					protected List<IAdministrationTab> load() {
						return getTabs(category);
					}
					
				};
				
				ListView<IAdministrationTab> navs = new ListView<IAdministrationTab>("nav", model) {

					@Override
					protected void populateItem(ListItem<IAdministrationTab> item) {
						final IAdministrationTab tab = item.getModelObject();
						item.add(tab.newTabLink("link"));
						item.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								if (activeTab == null) {
									return "";
								}
								
								return Objects.equal(activeTab.getTitle().getObject(), tab.getTitle().getObject())
										? "active" : "";
							}
							
						}));
					}
				};
				
				item.add(navs);
			}
		};
		
		return loop;
	}
	
	private List<IAdministrationTab> getTabs(Category category) {
		if (category == null) {
			return Collections.emptyList();
		}

		List<IAdministrationTab> tabs = getAllTabs();
		List<IAdministrationTab> result = Lists.newArrayList();

		for (IAdministrationTab each : tabs) {
			if (Objects.equal(each.getCategory(), category) && each.isVisible()) {
				result.add(each);
			}
		}
		
		return result;
	}
	
	private List<IAdministrationTab> getAllTabs() {
		List<IAdministrationTab> tabs = Lists.newArrayList();
		
		// ACCOUNTS CATEGORY
		//
		tabs.add(new AbstractAdministrationTab(Model.of("Users"), Category.ACCOUNTS) {

			@Override
			public WebMarkupContainer getPanel(String panelId) {
				return new UserAdministrationPanel(panelId);
			}
		});
		
		// SETTINGS CATEGORY
		//
		tabs.add(new AbstractAdministrationTab(Model.of("System Settings"), Category.SETTINGS) {

			@Override
			public WebMarkupContainer getPanel(String panelId) {
				return new SystemSettingEdit(panelId);
			}
			
		});
		
		tabs.add(new AbstractAdministrationTab(Model.of("Mail Server"), Category.SETTINGS) {

			@Override
			public WebMarkupContainer getPanel(String panelId) {
				return new MailSettingEdit(panelId);
			}
		});
		
		// SUPPORT CATEGORY
		//
		tabs.add(new AbstractAdministrationTab(Model.of("Support Request"), Category.SUPPORT) {

			@Override
			public WebMarkupContainer getPanel(String panelId) {
				return new SupportPanel(panelId);
			}
		});
		
		tabs.add(new AbstractAdministrationTab(Model.of("Licensing"), Category.SUPPORT) {

			@Override
			public WebMarkupContainer getPanel(String panelId) {
				return new LicensingPanel(panelId);
			}
		});
		
		// Add more tabs from IAdministrationTab extension
		//
		
		return tabs;
	}
	
	@Override
	protected String getPageTitle() {
		return "Administration - " + (activeTab == null ? "Overview" : activeTab.getTitle().getObject());
	}
}
