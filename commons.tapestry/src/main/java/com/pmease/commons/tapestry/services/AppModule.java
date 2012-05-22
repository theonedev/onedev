package com.pmease.commons.tapestry.services;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.services.transform.InjectionProvider2;
import org.eclipse.jetty.servlet.ServletMapping;

import com.google.inject.Injector;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.jetty.JettyPlugin;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.PluginManager;
import com.pmease.commons.tapestry.DisabledInjectionProvider;
import com.pmease.commons.tapestry.GuiceObjectProvider;
import com.pmease.commons.tapestry.TapestryModule;

public class AppModule {
	
	public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration) {
		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en");

		configuration.add(SymbolConstants.PRODUCTION_MODE, !Bootstrap.isSandboxMode() || Bootstrap.isProdMode());
	}

	public static Injector buildGuiceInjector() {
		return AppLoader.injector;
	}

	public static void contributeMasterObjectProvider(@Local Injector injector,
			OrderedConfiguration<ObjectProvider> configuration) {
		configuration.add("guiceProvider", new GuiceObjectProvider(injector), "after:*");
	}

    public static void contributeInjectionProvider(OrderedConfiguration<InjectionProvider2> configuration, 
    		SymbolSource symbolSource, AssetSource assetSource) {
    	configuration.overrideInstance("Named", DisabledInjectionProvider.class);
    }

    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration, 
    		PluginManager pluginManager) {
    	configuration.add(new LibraryMapping("commons", TapestryModule.class.getPackage().getName()));
    	for (LibraryMapping mapping: pluginManager.getExtensions(LibraryMapping.class)) 
    		configuration.add(mapping);
    }

    public static void contributeIgnoredPathsFilter(Configuration<String> configuration, PluginManager pluginManager) {
    	JettyPlugin jettyPlugin = pluginManager.getPlugin(JettyPlugin.class);
    	for (ServletMapping mapping: jettyPlugin.getContext().getServletHandler().getServletMappings()) {
    		for (String pathSpec: mapping.getPathSpecs()) {
    			if (!pathSpec.equals("/") && !pathSpec.equals("/*")) {
        			pathSpec = pathSpec.replace(".", "\\.");
        			if (pathSpec.endsWith("/*")) {
        				pathSpec = StringUtils.stripEnd(pathSpec, "/*");
        				configuration.add(pathSpec);
        				configuration.add(pathSpec + "/.*");
        			} else {
    	    			pathSpec = pathSpec.replace("*", ".*");
    	        		configuration.add(pathSpec);
        			}
    			}
    		}
    	}
    }
    
}
