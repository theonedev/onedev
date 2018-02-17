package com.turbodev.server.web.page.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.ComponentRenderer;
import com.turbodev.server.web.component.link.ViewStateAwarePageLink;
import com.turbodev.server.web.component.sidebar.SidebarPanel;
import com.turbodev.server.web.component.tabbable.PageTab;
import com.turbodev.server.web.component.tabbable.Tab;
import com.turbodev.server.web.page.admin.authenticator.AuthenticatorPage;
import com.turbodev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import com.turbodev.server.web.page.admin.licensemanagement.LicenseManagementPage;
import com.turbodev.server.web.page.admin.mailsetting.MailSettingPage;
import com.turbodev.server.web.page.admin.securitysetting.SecuritySettingPage;
import com.turbodev.server.web.page.admin.serverinformation.ServerInformationPage;
import com.turbodev.server.web.page.admin.serverlog.ServerLogPage;
import com.turbodev.server.web.page.admin.systemsetting.SystemSettingPage;
import com.turbodev.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public abstract class AdministrationPage extends LayoutPage {

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new SidebarPanel("sidebar", null) {

			@Override
			protected List<? extends Tab> newTabs() {
				List<PageTab> tabs = new ArrayList<>();
				tabs.add(new AdministrationTab("System Setting", "fa fa-fw fa-sliders", SystemSettingPage.class));
				tabs.add(new AdministrationTab("Security Setting", "fa fa-fw fa-lock", SecuritySettingPage.class));
				tabs.add(new AdministrationTab("External Authentication", "fa fa-fw fa-key", AuthenticatorPage.class));
				tabs.add(new AdministrationTab("Mail Setting", "fa fa-fw fa-envelope", MailSettingPage.class));
				tabs.add(new AdministrationTab("Database Backup", "fa fa-fw fa-database", DatabaseBackupPage.class));
				tabs.add(new AdministrationTab("Server Log", "fa fa-fw fa-file-text-o", ServerLogPage.class));
				tabs.add(new AdministrationTab("Server Information", "fa fa-fw fa-desktop", ServerInformationPage.class));
				tabs.add(new AdministrationTab("License Management", "fa fa-fw fa-vcard-o", LicenseManagementPage.class));
				return tabs;
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AdministrationResourceReference()));
	}

	@Override
	protected List<ComponentRenderer> getBreadcrumbs() {
		List<ComponentRenderer> breadcrumbs = super.getBreadcrumbs();
		
		breadcrumbs.add(new ComponentRenderer() {

			@Override
			public Component render(String componentId) {
				return new ViewStateAwarePageLink<Void>(componentId, SystemSettingPage.class) {

					@Override
					public IModel<?> getBody() {
						return Model.of("Administration");
					}
					
				};
			}
			
		});
		
		return breadcrumbs;
	}

}
