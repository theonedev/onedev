package com.pmease.commons.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.bytecode.SignatureAttribute.ClassSignature;
import javassist.bytecode.SignatureAttribute.ClassType;
import javassist.bytecode.SignatureAttribute.TypeArgument;
import javassist.bytecode.SignatureAttribute.TypeParameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;
import com.google.inject.util.Modules.OverriddenModuleBuilder;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.bootstrap.Lifecycle;
import com.pmease.commons.util.ExceptionUtils;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.dependency.DependencyHelper;

public class AppLoader implements Lifecycle {

	private static final Logger logger = LoggerFactory.getLogger(AppLoader.class);

	public static Injector injector;
	
	@SuppressWarnings("rawtypes")
	private static Map<String, TypeLiteral> typeLiterals = new HashMap<String, TypeLiteral>();
	
	@Override
	public void start() {
        File tempDir = Bootstrap.getTempDir();
		if (tempDir.exists()) {
			logger.info("Cleaning temp directory...");
			try {
				FileUtils.cleanDirectory(tempDir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else if (!tempDir.mkdir()) {
			throw new RuntimeException("Can not create directory '" + 
					tempDir.getAbsolutePath() + "'.");
		}
		System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
		
		logger.info("Initializing dependency injection container...");
		
		OverriddenModuleBuilder builder = Modules.override(new AppLoaderModule());
		
		for (Module module: DependencyHelper.sortDependencies(loadPluginModules()))
			builder = Modules.override(builder.with(module));
		
		injector = Guice.createInjector(builder.with(new AbstractModule() {

			@Override
			protected void configure() {
			}
			
		}));
		
		logger.info("Starting plugin manager...");
		injector.getInstance(PluginManager.class).start();
	}

	@Override
	public void stop() {
		logger.info("Stopping plugin manager...");
		injector.getInstance(PluginManager.class).stop();
	}

	private Map<String, AbstractPluginModule> loadPluginModules() {
		URLClassLoader classLoader = (URLClassLoader) AppLoader.class.getClassLoader();

		Map<String, AbstractPluginModule> pluginModules = new HashMap<String, AbstractPluginModule>();
		
		for (URL url: classLoader.getURLs()) {
			Properties pluginProps = FileUtils.loadProperties(
					new File(url.getFile()), "pmease-plugin.properties");
			if (pluginProps != null) {
				String pluginId = pluginProps.getProperty("id");
				if (pluginModules.containsKey(pluginId))
					throw new RuntimeException("More than one version of plugin '" + pluginId + "' is found.");
				
				String moduleClassName = pluginProps.getProperty("module");
				try {
					Class<?> moduleClass = Class.forName(moduleClassName);
					
					if (AbstractPluginModule.class.isAssignableFrom(moduleClass)) {
						AbstractPluginModule pluginModule = (AbstractPluginModule) moduleClass.newInstance();
						
						pluginModule.setPluginId(pluginId);
						pluginModule.setPluginName(pluginProps.getProperty("name"));
						pluginModule.setPluginDescription(pluginProps.getProperty("description"));
						pluginModule.setPluginVendor(pluginProps.getProperty("vendor"));
						pluginModule.setPluginVersion(pluginProps.getProperty("version"));
						String dependenciesStr = pluginProps.getProperty("dependencies");
						if (dependenciesStr != null)
							pluginModule.setPluginDependencies(new HashSet<String>(StringUtils.splitAndTrim(dependenciesStr, ";")));
						pluginModules.put(pluginId, pluginModule);
					} else {
						throw new RuntimeException("Plugin module class should extend from '" 
								+ AbstractPluginModule.class.getName() + "'.");
					}
				} catch (Exception e) {
					throw new RuntimeException("Error loading plugin '" + pluginId + "'.", e);
				}
			}
		}
		
		return pluginModules;
	}
	
	public static <T> T getInstance(Class<T> type) {
		return injector.getInstance(type);
	}
	
	@SuppressWarnings({ "unchecked"})
	public static <T> Set<T> getExtensions(Class<T> extensionPoint) {
		synchronized (typeLiterals) {
			TypeLiteral<Set<T>> typeLiteral = typeLiterals.get(extensionPoint.getName());
			if (typeLiteral == null) {
				try {
					String packageName = extensionPoint.getPackage().getName();
					String generatedTypeLiteralClassName = 
							"generated." + packageName + "." + extensionPoint.getSimpleName() + "TypeLiteral";
					ClassPool classPool = ClassPool.getDefault();
					classPool.insertClassPath(new ClassClassPath(TypeLiteral.class));
					CtClass ctGeneratedTypeLiteral = classPool.makeClass(generatedTypeLiteralClassName);
					CtClass ctTypeLiteral = classPool.get(TypeLiteral.class.getName());
					ctGeneratedTypeLiteral.setSuperclass(ctTypeLiteral);
					CtConstructor constructor = new CtConstructor(new CtClass[0], ctGeneratedTypeLiteral);
					constructor.setBody(null);
					ctGeneratedTypeLiteral.addConstructor(constructor);
					
					TypeArgument setTypeArgument = new TypeArgument(new ClassType(extensionPoint.getName()));
					TypeArgument superClassTypeArgument = new TypeArgument(
							new ClassType(Set.class.getName(), new TypeArgument[]{setTypeArgument}));
					
					ClassType superClass = new ClassType(
							TypeLiteral.class.getName(), 
							new TypeArgument[]{superClassTypeArgument});
					
					ClassSignature signature = new ClassSignature(new TypeParameter[0], superClass, new ClassType[0]);
					ctGeneratedTypeLiteral.setGenericSignature(signature.encode());
					
					Class<?> typeLiteralClassOfExtensionPoint = ctGeneratedTypeLiteral.toClass();
					typeLiteral = (TypeLiteral<Set<T>>) typeLiteralClassOfExtensionPoint.newInstance();
					
					typeLiterals.put(extensionPoint.getName(), typeLiteral);
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				}
			}
			
			return injector.getInstance(Key.get(typeLiteral));
		}
	}
	
}
