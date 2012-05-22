package com.pmease.commons.tapestry;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import org.apache.tapestry5.TapestryFilter;

import com.pmease.commons.loader.PluginManager;
import com.pmease.commons.tapestry.extensionpoints.TapestryConfiguratorProvider;
import com.pmease.commons.tapestry.services.AppModule;

@Singleton
public class CustomTapestryFilter extends TapestryFilter {

	@Inject
	private PluginManager pluginManager;

	@Override
	protected Class<?>[] provideExtraModuleClasses(ServletContext context) {
		List<Class<?>> moduleClasses = new ArrayList<Class<?>>();

		moduleClasses.add(AppModule.class);
		for (TapestryConfiguratorProvider each : pluginManager
				.getExtensions(TapestryConfiguratorProvider.class))
			moduleClasses.add(each.getTapestryConfigurator());
		return moduleClasses.toArray(new Class<?>[0]);
	}

}
