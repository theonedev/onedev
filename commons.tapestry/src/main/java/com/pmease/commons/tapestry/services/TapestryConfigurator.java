package com.pmease.commons.tapestry.services;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.ApplicationStateContribution;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.apache.tapestry5.services.transform.InjectionProvider2;
import org.eclipse.jetty.servlet.ServletMapping;
import org.hibernate.Session;
import org.hibernate.mapping.PersistentClass;

import com.google.inject.Injector;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.SessionProvider;
import com.pmease.commons.jetty.JettyPlugin;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.PluginManager;
import com.pmease.commons.tapestry.DisabledInjectionProvider;
import com.pmease.commons.tapestry.GuiceObjectProvider;
import com.pmease.commons.tapestry.TapestryModule;
import com.pmease.commons.tapestry.persistence.EntityApplicationStatePersistenceStrategy;
import com.pmease.commons.tapestry.persistence.EntityPersistentFieldStrategy;
import com.pmease.commons.tapestry.persistence.EntityValueEncoder;
import com.pmease.commons.tapestry.persistence.PersistenceConstants;

public class TapestryConfigurator {
	
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
    
    @SuppressWarnings("rawtypes")
	public static void contributeValueEncoderSource(MappedConfiguration<Class<?>, ValueEncoderFactory> configuration,
            final org.hibernate.cfg.Configuration hibernateCfg, final SessionProvider sessionProvider,
            final TypeCoercer typeCoercer, final PropertyAccess propertyAccess, final LoggerSource loggerSource) {
    	
        Iterator<PersistentClass> mappings = hibernateCfg.getClassMappings();
        while (mappings.hasNext()) {
            final PersistentClass persistentClass = mappings.next();
            final Class<?> entityClass = persistentClass.getMappedClass();

            if (entityClass != null) {
                ValueEncoderFactory<?> factory = new ValueEncoderFactory() {
                    @SuppressWarnings("unchecked")
					public ValueEncoder create(Class type) {
                        return new EntityValueEncoder(entityClass, persistentClass, sessionProvider, propertyAccess,
                                typeCoercer, loggerSource.getLogger(entityClass));
                    }
                };

                configuration.add(entityClass, factory);
            }
        }
    }
    
    public static void contributePersistentFieldManager(MappedConfiguration<String, 
    		PersistentFieldStrategy> configuration) {
        configuration.addInstance(PersistenceConstants.ENTITY, EntityPersistentFieldStrategy.class);
    }

    /**
     * Contributes the following strategy:
     * <dl>
     * <dt>entity</dt>
     * <dd>Stores the id of the entity and reloads from the {@link Session}</dd>
     * </dl>
     */
    public void contributeApplicationStatePersistenceStrategySource(
            MappedConfiguration<String, ApplicationStatePersistenceStrategy> configuration) {
        configuration.addInstance(PersistenceConstants.ENTITY, EntityApplicationStatePersistenceStrategy.class);
    }

    @SuppressWarnings("rawtypes")
	public static void contributeApplicationStateManager(
            MappedConfiguration<Class, ApplicationStateContribution> configuration,
            org.hibernate.cfg.Configuration hibernateCfg) {

        Iterator<PersistentClass> mappings = hibernateCfg.getClassMappings();
        while (mappings.hasNext()) {
            final PersistentClass persistentClass = mappings.next();
            final Class entityClass = persistentClass.getMappedClass();

            configuration.add(entityClass, new ApplicationStateContribution(PersistenceConstants.ENTITY));
        }
    }
    
}
