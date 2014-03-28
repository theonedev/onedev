package com.pmease.gitop.web.page.admin;

import java.util.Collections;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
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
import com.google.common.collect.Lists;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.page.BasePage;
import com.pmease.gitop.web.page.admin.api.AdministrationTab;
import com.pmease.gitop.web.page.admin.api.AdministrationTab.Category;

@SuppressWarnings("serial")
public abstract class AdministrationLayoutPage extends BasePage {
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofSystemAdmin());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("overviewlink", AdministrationOverviewPage.class));
		add(createSidebarNavs());
	}
	
	private Component createSidebarNavs() {
		Loop loop = new Loop("categories", Category.values().length) {

			@Override
			protected void populateItem(LoopItem item) {
				final Category category = Category.values()[item.getIndex()];
				item.add(new Label("name", category.name()));
				IModel<List<AdministrationTab>> model = new LoadableDetachableModel<List<AdministrationTab>>() {

					@Override
					protected List<AdministrationTab> load() {
						return getTabs(category);
					}
					
				};
				
				ListView<AdministrationTab> navs = new ListView<AdministrationTab>("nav", model) {

					@Override
					protected void populateItem(ListItem<AdministrationTab> item) {
						final AdministrationTab tab = item.getModelObject();
						item.add(tab.newTabLink("link", new PageParameters()));
						item.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return tab.isSelected(getPage()) ? "active" : "";
							}
							
						}));
					}
				};
				
				item.add(navs);
			}
		};
		
		return loop;
	}

	private List<AdministrationTab> getTabs(Category category) {
		if (category == null) {
			return Collections.emptyList();
		}

		List<AdministrationTab> tabs = getAllTabs();
		List<AdministrationTab> result = Lists.newArrayList();

		for (AdministrationTab each : tabs) {
			if (Objects.equal(each.getGroupName(), category.name()) && each.isVisible()) {
				result.add(each);
			}
		}
		
		return result;
	}
	
	private List<AdministrationTab> getAllTabs() {
		List<AdministrationTab> tabs = Lists.newArrayList();
		
		// ACCOUNTS CATEGORY
		//
		tabs.add(new AdministrationTab(Model.of("Users"), Category.ACCOUNTS, UserAdministrationPage.class));
		
		// SETTINGS CATEGORY
		//
		tabs.add(new AdministrationTab(Model.of("System Settings"), Category.SETTINGS, SystemSettingEdit.class));		
		tabs.add(new AdministrationTab(Model.of("Mail Server"), Category.SETTINGS, MailSettingEdit.class));		
		// SUPPORT CATEGORY
		//
		tabs.add(new AdministrationTab(Model.of("Support Request"), Category.SUPPORT, SupportPage.class));		
		tabs.add(new AdministrationTab(Model.of("Licensing"), Category.SUPPORT, LicensingPage.class));
		
		// Add more tabs from IAdministrationTab extension
		//
		
		return tabs;
	}
	
}
