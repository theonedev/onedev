package com.gitplex.commons.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.logging.Handler;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class Bootstrap {

	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	public static final int SOCKET_CONNECT_TIMEOUT = 60000;

	public static final String APP_LOADER_PROPERTY_NAME = "appLoader";

	public static final String LOGBACK_CONFIG_FILE_PROPERTY_NAME = "logback.configurationFile";

	public static final String DEFAULT_APP_LOADER = "com.gitplex.commons.loader.AppLoader";

	public static File installDir;

	public static boolean sandboxMode;

	public static boolean prodMode;
	
	@Nullable
	public static Command command;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			Locale.setDefault(Locale.US);

			File sandboxDir = new File("target/sandbox");
			if (sandboxDir.exists()) {
				Map<String, File> systemClasspath = (Map<String, File>) BootstrapUtils
						.readObject(new File(sandboxDir, "boot/system.classpath"));
				Set<String> bootstrapKeys = (Set<String>) BootstrapUtils
						.readObject(new File(sandboxDir, "boot/bootstrap.keys"));

				List<URL> urls = new ArrayList<URL>();
				for (String key : bootstrapKeys) {
					File file = systemClasspath.get(key);
					if (file == null)
						throw new RuntimeException("Unable to find bootstrap file for '" + key + "'.");
					try {
						urls.add(file.toURI().toURL());
					} catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}

				URLClassLoader bootClassLoader = new URLClassLoader(urls.toArray(new URL[0]),
						Bootstrap.class.getClassLoader().getParent());
				Thread.currentThread().setContextClassLoader(bootClassLoader);

				try {
					Class<?> bootstrapClass = bootClassLoader.loadClass(Bootstrap.class.getName());
					bootstrapClass.getMethod("boot", String[].class).invoke(null, new Object[] {args});
				} catch (Exception e) {
					throw BootstrapUtils.unchecked(e);
				}

			} else {
				boot(args);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@SuppressWarnings("unchecked")
	public static void boot(String[] args) {
		/*
		 * Sandbox mode might be checked frequently so we cache the result here
		 * to avoid calling File.exists() frequently.
		 */
		sandboxMode = new File("target/sandbox").exists();
		prodMode = (System.getProperty("prod") != null);

		File loadedFrom = new File(Bootstrap.class.getProtectionDomain().getCodeSource().getLocation().getFile());

		if (new File(loadedFrom.getParentFile(), "bootstrap.keys").exists())
			installDir = loadedFrom.getParentFile().getParentFile();
		else if (new File("target/sandbox").exists())
			installDir = new File("target/sandbox");
		else
			throw new RuntimeException("Unable to find product directory.");

		try {
			installDir = installDir.getCanonicalFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (args.length != 0) {
			String[] commandArgs = new String[args.length-1];
			System.arraycopy(args, 1, commandArgs, 0, commandArgs.length);
			command = new Command(args[0], commandArgs);
		} else {
			command = null;
		}

		configureLogging();

		try {
	        File tempDir = Bootstrap.getTempDir();
			if (tempDir.exists()) {
				logger.info("Cleaning temp directory...");
				BootstrapUtils.cleanDir(tempDir);
			} else if (!tempDir.mkdirs()) {
				throw new RuntimeException("Can not create directory '" + tempDir.getAbsolutePath() + "'.");
			}
			System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());

			logger.info("Launching application from '" + installDir.getAbsolutePath() + "'...");

			List<File> libFiles = new ArrayList<File>();
			libFiles.addAll(getLibFiles(getSiteLibDir()));
			
			File classpathFile = new File(installDir, "boot/system.classpath");
			if (classpathFile.exists()) {
				Map<String, File> systemClasspath = (Map<String, File>) BootstrapUtils.readObject(classpathFile);
				Set<String> bootstrapKeys = (Set<String>) BootstrapUtils
						.readObject(new File(installDir, "boot/bootstrap.keys"));
				for (Map.Entry<String, File> entry : systemClasspath.entrySet()) {
					if (!bootstrapKeys.contains(entry.getKey())) {
						libFiles.add(entry.getValue());
					}
				}
				if (new File("system/lib").exists()) {
					libFiles.addAll(getLibFiles(new File("system/lib")));
				}
			} else {
				libFiles.addAll(getLibFiles(getLibDir()));
			}

			List<URL> urls = new ArrayList<URL>();

			// load our jars first so that we can override classes in third party
			// jars if necessary.
			for (File file : libFiles) {
				if (isPriorityLib(file)) {
					try {
						urls.add(file.toURI().toURL());
					} catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}
			}
			for (File file : libFiles) {
				if (!isPriorityLib(file)) {
					try {
						urls.add(file.toURI().toURL());
					} catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}
			}

			ClassLoader appClassLoader = new URLClassLoader(urls.toArray(new URL[0]), Bootstrap.class.getClassLoader()) {

				private Map<String, CachedUrl> resourceCache = new ConcurrentHashMap<String, CachedUrl>();

				@Override
				protected Class<?> findClass(String name) throws ClassNotFoundException {
					return super.findClass(name);
				}

				@Override
				public Class<?> loadClass(String name) throws ClassNotFoundException {
					return super.loadClass(name);
				}

				@Override
				public URL findResource(String name) {
					URL url;
					CachedUrl cachedUrl = resourceCache.get(name);
					if (cachedUrl == null) {
						url = super.findResource(name);
						resourceCache.put(name, new CachedUrl(url));
					} else {
						url = cachedUrl.getUrl();
					}
					return url;
				}

			};
			ClassLoader originClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(appClassLoader);

			try {
				String appLoaderClassName = System.getProperty(APP_LOADER_PROPERTY_NAME);
				if (appLoaderClassName == null)
					appLoaderClassName = DEFAULT_APP_LOADER;

				final Startable appLoader;
				try {
					Class<?> appLoaderClass = appClassLoader.loadClass(appLoaderClassName);
					appLoader = (Startable) appLoaderClass.newInstance();
					appLoader.start();
				} catch (Exception e) {
					throw BootstrapUtils.unchecked(e);
				}

				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						try {
							appLoader.stop();
						} catch (Exception e) {
							throw BootstrapUtils.unchecked(e);
						}
					}
				});
			} finally {
				Thread.currentThread().setContextClassLoader(originClassLoader);
			}
		} catch (Exception e) {
			logger.error("Error booting application", e);
			System.exit(1);
		}
	}

	private static boolean isPriorityLib(File lib) {
		String entryName = "META-INF/gitplex-artifact.properties";
		if (lib.isDirectory()) {
			return new File(lib, entryName).exists();
		} else {
			try (JarFile jarFile = new JarFile(lib)) {
				return jarFile.getJarEntry(entryName) != null;
			} catch (IOException e) {
				throw new RuntimeException(lib.getAbsolutePath() + e);
			} 
		}
	}
	
	private static List<File> getLibFiles(File libDir) {
		List<File> libFiles = new ArrayList<>();
		File libCacheDir = BootstrapUtils.createTempDir("libcache");
		for (File file : libDir.listFiles()) {
			if (file.getName().endsWith(".jar"))
				libFiles.add(file);
			else if (file.getName().endsWith(".zip"))
				BootstrapUtils.unzip(file, libCacheDir);
		}
		for (File file : libCacheDir.listFiles()) {
			if (file.getName().endsWith(".jar"))
				libFiles.add(file);
		}
		return libFiles;
	}
	
	private static void configureLogging() {
		// Set system properties so that they can be used in logback
		// configuration file.
		if (command != null) {
			System.setProperty("logback.logFile", installDir.getAbsolutePath() + "/logs/" + command.getName() + ".log");
			System.setProperty("logback.fileLogPattern", "%-5level - %msg%n");			
			System.setProperty("logback.consoleLogPattern", "%-5level - %msg%n");
		} else {
			System.setProperty("logback.logFile", installDir.getAbsolutePath() + "/logs/server.log");
			System.setProperty("logback.consoleLogPattern", "%d{HH:mm:ss} %-5level %logger{36} - %msg%n");			
			System.setProperty("logback.fileLogPattern", "%date %-5level [%thread] %logger{36} %msg%n");
		}

		File configFile = new File(installDir, "conf/logback.xml");
		System.setProperty(LOGBACK_CONFIG_FILE_PROPERTY_NAME, configFile.getAbsolutePath());

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(lc);
			lc.reset();
			configurator.doConfigure(configFile);
		} catch (JoranException je) {
			je.printStackTrace();
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

		// Redirect JDK logging to slf4j
		java.util.logging.Logger jdkLogger = java.util.logging.Logger.getLogger("");
		for (Handler handler : jdkLogger.getHandlers())
			jdkLogger.removeHandler(handler);
		SLF4JBridgeHandler.install();
	}

	public static File getBinDir() {
		return new File(installDir, "bin");
	}
	
	public static File getBootDir() {
		return new File(installDir, "boot");
	}
	
	public static File getStatusDir() {
		return new File(installDir, "status");
	}
	
	public static boolean isServerRunning(File installDir) {
		// status directory may contain multiple pid files, for instance, 
		// appname.pid, appname_backup.pid, etc. We only check for appname.pid
		// here and assumes that appname does not contain underscore
		for (File file: new File(installDir, "status").listFiles()) {
			if (file.getName().endsWith(".pid") && !file.getName().contains("_"))
				return true;
		}
		return false;
	}

	public static File getLibDir() {
		return new File(installDir, "lib");
	}
	
	public static File getSiteLibDir() {
		return new File(getSiteDir(), "lib");
	}

	public static File getTempDir() {
		if (command != null) 
			return new File(installDir, "temp/" + command.getName());
		else
			return new File(installDir, "temp/server");
	}

	public static File getConfDir() {
		return new File(installDir, "conf");
	}

	public static File getSiteDir() {
		return new File(installDir, "site");
	}
	
	public static File getPluginsDir() {
		return new File(installDir, "plugins");
	}
	
	private static class CachedUrl {
		private final URL url;

		public CachedUrl(URL url) {
			this.url = url;
		}

		public URL getUrl() {
			return url;
		}
	}
}
