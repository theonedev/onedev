package io.onedev.server.ee.dashboard;

import java.util.Collection;
import java.util.HashSet;

import org.apache.wicket.protocol.http.WebApplication;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.loader.ImplementationProvider;
import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.ee.dashboard.widgets.WidgetGroup;
import io.onedev.server.model.support.Widget;
import io.onedev.server.web.WebApplicationConfigurator;
import io.onedev.server.web.mapper.BasePageMapper;
import io.onedev.server.web.page.layout.MainMenuCustomization;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class DashboardModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(MainMenuCustomization.class).toInstance(new EEMainMenuCustomization());
		
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
		
		contribute(WebApplicationConfigurator.class, new WebApplicationConfigurator() {
			
			@Override
			public void configure(WebApplication application) {
				application.mount(new BasePageMapper("dashboards", DashboardPage.class));
				application.mount(new BasePageMapper("dashboards/${dashboard}", DashboardPage.class));
			}
			
		});		
		
	}

}
