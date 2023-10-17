package io.onedev.server.ee;

import com.google.common.collect.Sets;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.ee.clustering.ClusterManagementPage;
import io.onedev.server.ee.clustering.DefaultClusterManager;
import io.onedev.server.ee.dashboard.DashboardPage;
import io.onedev.server.ee.dashboard.widgets.WidgetGroup;
import io.onedev.server.ee.sendgrid.*;
import io.onedev.server.ee.storage.DefaultStorageManager;
import io.onedev.server.ee.storage.StorageSetting;
import io.onedev.server.ee.subscription.DefaultSubscriptionManager;
import io.onedev.server.ee.subscription.SubscriptionManagementPage;
import io.onedev.server.ee.terminal.BuildTerminalPage;
import io.onedev.server.ee.terminal.DefaultTerminalManager;
import io.onedev.server.ee.timetracking.DefaultTimeTrackingManager;
import io.onedev.server.ee.timetracking.TimesheetsPage;
import io.onedev.server.ee.xsearch.*;
import io.onedev.server.jetty.ServletConfigurator;
import io.onedev.server.StorageManager;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.model.support.Widget;
import io.onedev.server.model.support.administration.mailservice.MailService;
import io.onedev.server.terminal.TerminalManager;
import io.onedev.server.timetracking.TimeTrackingManager;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.BasePageMapper;
import io.onedev.server.web.mapper.ProjectPageMapper;
import io.onedev.server.web.page.layout.*;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class EEModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(TerminalManager.class).to(DefaultTerminalManager.class);
		bind(ClusterManager.class).to(DefaultClusterManager.class);
		bind(StorageManager.class).to(DefaultStorageManager.class);
		bind(CodeSearchManager.class).to(DefaultCodeSearchManager.class);
		bind(CodeIndexManager.class).to(DefaultCodeIndexManager.class);
		bind(SubscriptionManager.class).to(DefaultSubscriptionManager.class);
		bind(TimeTrackingManager.class).to(DefaultTimeTrackingManager.class);
		bind(CodeIndexStatusChangedBroadcaster.class);
		bind(MainMenuCustomization.class).to(DefaultMainMenuCustomization.class);
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Collection<Class<?>> getImplementations() {
				Collection<Class<?>> implementations = new HashSet<>();
				for (Class<?> implementation: ClassUtils.findImplementations(Widget.class, WidgetGroup.class))
					implementations.add(implementation);
				return implementations;
			}

			@Override
			public Class<?> getAbstractClass() {
				return Widget.class;
			}

		});

		contribute(WebApplicationConfigurator.class, application -> {
			application.mount(new BasePageMapper("~dashboards", DashboardPage.class));
			application.mount(new BasePageMapper("~dashboards/${dashboard}", DashboardPage.class));
			application.mount(new BasePageMapper("~projects/${project}/builds/${build}/terminal", BuildTerminalPage.class));
			application.mount(new BasePageMapper("~code-search/text", TextSearchPage.class));
			application.mount(new BasePageMapper("~code-search/symbols", SymbolSearchPage.class));
			application.mount(new BasePageMapper("~code-search/files", FileSearchPage.class));
			application.mount(new BasePageMapper("~administration/cluster", ClusterManagementPage.class));
			application.mount(new BasePageMapper("~administration/subscription-management", SubscriptionManagementPage.class));
			application.mount(new ProjectPageMapper("${project}/~timesheets", TimesheetsPage.class));
			application.mount(new ProjectPageMapper("${project}/~timesheets/${timesheet}", TimesheetsPage.class));
		});
		
		contribute(AdministrationSettingContribution.class, () -> {
			var settings = new ArrayList<Class<? extends ContributedAdministrationSetting>>();
			if (OneDev.getInstance(SubscriptionManager.class).isSubscriptionActive())
				settings.add(StorageSetting.class);
			return settings;
		});
		contribute(AdministrationMenuContribution.class, () -> {
			var menuItems = new ArrayList<SidebarMenuItem>();
			if (OneDev.getInstance(SubscriptionManager.class).isSubscriptionActive())
				menuItems.add(new SidebarMenuItem.Page(null, "High Availability & Scalability", ClusterManagementPage.class, new PageParameters()));
			menuItems.add(new SidebarMenuItem.Page(null, "Subscription Management", SubscriptionManagementPage.class, new PageParameters()));
			return menuItems;
		});

		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return MailService.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(SendgridMailService.class);
			}

		});

		bind(MessageManager.class).to(DefaultMessageManager.class);
		bind(SendgridServlet.class);
		contribute(ServletConfigurator.class, SendgridServletConfigurator.class);
		
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return EEPlugin.class;
	}
	
}
