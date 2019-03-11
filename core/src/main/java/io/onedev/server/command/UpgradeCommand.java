package io.onedev.server.command;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SystemUtils;
import org.hibernate.Interceptor;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.launcher.loader.PluginManager;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.migration.DatabaseMigrator;
import io.onedev.server.migration.MigrationHelper;
import io.onedev.server.persistence.DefaultPersistManager;
import io.onedev.server.persistence.HibernateProperties;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.validation.EntityValidator;

@Singleton
public class UpgradeCommand extends DefaultPersistManager {

	private static final Logger logger = LoggerFactory.getLogger(UpgradeCommand.class);
	
	private static final String BACKUP_DATETIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
	
	private final String appName;
	
	@Inject
	public UpgradeCommand(PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator, PluginManager pluginManager) {
		super(physicalNamingStrategy, properties, interceptor, idManager, dao, validator);
		appName = pluginManager.getProduct().getName();
	}

	protected Commandline buildCommandline(File upgradeDir, String command, String...commandArgs) {
		String version;
		File versionFile = new File(upgradeDir, "version.txt");
		if (versionFile.exists()) {
			try {
				version = FileUtils.readFileToString(versionFile, Charset.defaultCharset()).trim();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			version = "1.0-EAP-build26";
		}
		
		String bootstrapClass;
		if (version.startsWith("1.0-EAP")) {
			int build = Integer.parseInt(StringUtils.substringAfterLast(version, "build"));
			if (build <= 26)
				bootstrapClass = "com.gitplex.commons.bootstrap.Bootstrap";
			else
				bootstrapClass = "com.gitplex.launcher.bootstrap.Bootstrap";
		} else if (version.startsWith("1.0.0")) {
			bootstrapClass = "com.gitplex.launcher.bootstrap.Bootstrap";
		} else if (version.startsWith("1.0.1")) {
			bootstrapClass = "com.turbodev.launcher.bootstrap.Bootstrap";
		} else if (version.startsWith("2.0.")){
			bootstrapClass = "io.onedev.launcher.bootstrap.Bootstrap";
		} else {
			bootstrapClass = "io.onedev.commons.launcher.bootstrap.Bootstrap";
		}
		Commandline cmdline= new Commandline(System.getProperty("java.home") + "/bin/java");
		cmdline.addArgs("-Xmx" + Runtime.getRuntime().maxMemory()/1024/1024 + "m",  "-classpath", "*", bootstrapClass, 
				command);
		cmdline.addArgs(commandArgs);
		cmdline.workingDir(new File(upgradeDir, "boot"));
		return cmdline;
	}
	
	private String prefixUpgradeTargetLog(String line) {
		return ">>> " + line;
	}
	
	@Override
	public void start() {
		if (Bootstrap.command.getArgs().length == 0) {
			logger.error("Missing upgrade target parameter. Usage: {} <installation directory of old {} version>", 
					Bootstrap.command.getScript(), appName);
			System.exit(1);
		}
		File upgradeDir = new File(Bootstrap.command.getArgs()[0]);
		if (!upgradeDir.isAbsolute())
			upgradeDir = new File(Bootstrap.getBinDir(), upgradeDir.getPath());
		
		if (!upgradeDir.exists()) {
			logger.error("Unable to find directory: {}", upgradeDir.getAbsolutePath());
			System.exit(1);
		}
		
		logger.info("Upgrading {}...", upgradeDir.getAbsolutePath());
		
		if (!new File(upgradeDir, "boot/bootstrap.keys").exists()) {
			logger.error("Invalid {} installation directory: {}, make sure you are specifying the top level "
					+ "installation directory (it contains sub directories such as \"bin\", \"boot\", \"conf\", etc)", 
					appName, upgradeDir.getAbsolutePath());
			System.exit(1);
		}
		
		if (Bootstrap.isServerRunning(upgradeDir)) {
			logger.error("Please stop server running at \"{}\" before upgrading", upgradeDir.getAbsolutePath());
			System.exit(1);
		}
		if (Bootstrap.isServerRunning(Bootstrap.installDir)) {
			logger.error("Please stop server running at \"{}\" before upgrading", Bootstrap.installDir.getAbsolutePath());
			System.exit(1);
		}

		for (File file: new File(upgradeDir, "lib").listFiles()) {
			if (file.getName().contains("mariadb") || file.getName().contains("mysql") 
					|| file.getName().contains("ojdbc") || file.getName().contains("postgresql") 
					|| file.getName().contains("sqljdbc")) {
				try {
					FileUtils.moveFileToDirectory(file, new File(upgradeDir, "site/lib"), true);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		cleanAndCopy(new File(upgradeDir, "site/lib"), new File(Bootstrap.installDir, "site/lib"));
		
		AtomicReference<String> oldDataVersion = new AtomicReference<>(null);
		int ret = buildCommandline(upgradeDir, "check_data_version").execute(new LineConsumer() {
			
			@Override
			public void consume(String line) {
				String prefix = "Data version: ";
				if (line.startsWith(prefix)) {
					oldDataVersion.set(line.substring(prefix.length()));
					logger.info("Old data version: " + oldDataVersion.get());
				} else {
					logger.info(prefixUpgradeTargetLog(line));
				}
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(prefixUpgradeTargetLog(line));
			}}
		
		).getReturnCode();
		
		if (ret != 0) {
			logger.error("Unable to upgrade specified installation due to above error");
			System.exit(1);
		}

		AtomicBoolean usingEmbeddedDB = new AtomicBoolean(false);
		ret = buildCommandline(upgradeDir, "db_dialect").execute(new LineConsumer() {
			
			@Override
			public void consume(String line) {
				String prefix = "Database dialect: ";
				String dialect;
				if (line.startsWith(prefix)) {
					dialect = line.substring(prefix.length());
					logger.info("Database dialect: " + dialect);
					if (dialect.toLowerCase().contains("hsql"))
						usingEmbeddedDB.set(true);
				} else {
					logger.info(prefixUpgradeTargetLog(line));
				}
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(prefixUpgradeTargetLog(line));
			}}
		
		).getReturnCode();
		
		if (ret != 0) {
			logger.error("Unable to upgrade specified installation due to above error");
			System.exit(1);
		}
		
		File upgradeBackup = new File(Bootstrap.installDir, getBackupName(upgradeDir.getName()));
		FileUtils.createDir(upgradeBackup);
		File dbBackup = null;
		
		FileUtils.cleanDir(new File(upgradeDir, "temp"));
		
		if (usingEmbeddedDB.get()) {
			logger.info("Backing up old installation directory as {}...", upgradeBackup.getAbsolutePath());
			try {
				FileUtils.copyDirectory(upgradeDir, upgradeBackup);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			restoreExecutables(upgradeBackup);

			if (!MigrationHelper.getVersion(DatabaseMigrator.class).equals(oldDataVersion.get())) {
				dbBackup = new File(upgradeDir, getBackupName("dbbackup") + ".zip");
				logger.info("Backing up database as {}...", dbBackup.getAbsolutePath());
				backupDB(upgradeDir, dbBackup);
			}
		} else {
			// for non-embedded database, we should include database backup in the old installation backup directory
			// in case we need that for database restore after a failed upgrade
			if (!MigrationHelper.getVersion(DatabaseMigrator.class).equals(oldDataVersion.get())) {
				dbBackup = new File(upgradeDir, getBackupName("dbbackup") + ".zip");
				logger.info("Backing up database as {}...", dbBackup.getAbsolutePath());
				backupDB(upgradeDir, dbBackup);
			}
			
			logger.info("Backing up old installation directory as {}...", upgradeBackup.getAbsolutePath());
			try {
				FileUtils.copyDirectory(upgradeDir, upgradeBackup);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			restoreExecutables(upgradeBackup);
		}
		
		boolean failed;
		try {
			if (dbBackup != null) {
				logger.info("Cleaning database with old program version...");
				ret = buildCommandline(upgradeDir, "clean").execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.info(prefixUpgradeTargetLog(line));
					}
					
				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.error(prefixUpgradeTargetLog(line));
					}
					
				}).getReturnCode();
				
				if (ret != 0) {
					logger.error("Failed to upgrade {}", upgradeDir.getAbsolutePath());
					failed = true;
				} else {
					failed = false;
				}

				if (!failed) {
					logger.info("Copying new program files into {}...", upgradeDir.getAbsolutePath());
					copyProgramFilesTo(upgradeDir);

					logger.info("Restoring database with new program version...");
					ret = buildCommandline(upgradeDir, "restore", dbBackup.getAbsolutePath()).execute(new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.info(prefixUpgradeTargetLog(line));
						}
						
					}, new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.error(prefixUpgradeTargetLog(line));
						}
						
					}).getReturnCode();
					
					if (ret != 0) {
						logger.error("Failed to upgrade {}", upgradeDir.getAbsolutePath());
						failed = true;
					} else {
						failed = false;
					}
				}
			} else {
				logger.info("Copying new program files into {}...", upgradeDir.getAbsolutePath());
				copyProgramFilesTo(upgradeDir);
				failed = false;
			}
		} catch (Exception e) {
			logger.error("Error upgrading " + upgradeDir.getAbsolutePath(), e);
			failed = true;
		}
		
		if (failed) {
			logger.info("Now restoring old installation directory due to upgrade failure...");
			try {
				/* we do not clean upgradeDir here directly as user may open 
				 * a command prompt inside the various sub directories to 
				 * cause it unable to be deleted
				 */
				for (File child: upgradeDir.listFiles()) {
					if (child.isDirectory())
						FileUtils.cleanDir(child);
					else
						FileUtils.deleteFile(child);
				}
				FileUtils.copyDirectory(upgradeBackup, upgradeDir);
				restoreExecutables(upgradeDir);
				logger.info("Old installation directory is now restored.");
			} catch (Exception e2) {
				logger.error("Error restoring old installation directory", e2);
				logger.warn("Please restore it manually by deleting all contents under directory \"{}\", and then copying over all contents under directory \"{}\"", 
						upgradeDir.getAbsolutePath(), upgradeBackup.getAbsolutePath());
			}
			if (!usingEmbeddedDB.get() && dbBackup != null) {
				logger.warn("The database might be in an inconsistent state due to upgrade failure. In that case, "
						+ "you need to restore the database by first cleaning it, and then running below command:");
				if (SystemUtils.IS_OS_WINDOWS) {
					logger.info(upgradeDir.getAbsolutePath() + File.separator + "bin" + File.separator + "restore.bat " + dbBackup.getAbsolutePath());
				} else {
					logger.info(upgradeDir.getAbsolutePath() + File.separator + "bin" + File.separator + "restore.sh " + dbBackup.getAbsolutePath());
				}
			}
			System.exit(1);
		} else {
			logger.info("Successfully upgraded {}", upgradeDir.getAbsolutePath());
			
			if (Integer.parseInt(oldDataVersion.get()) <= 5) {
				logger.warn("\n"
						+ "************************* IMPORTANT NOTICE *************************\n"
						+ "* OneDev password hash algorithm has been changed for security    *\n"
						+ "* reason. Please reset administrator password with the             *\n"
						+ "* reset_admin_password command. Other users' password will be      *\n"
						+ "* reset and sent to their mail box automatically when they logs    *\n"
						+ "* into OneDev.                                                    *\n"
						+ "********************************************************************");
			}
			System.exit(0);
		}
	}
	
	private void backupDB(File upgradeDir, File dbBackup) {
		int ret = buildCommandline(upgradeDir, "backup", dbBackup.getAbsolutePath()).execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.info(prefixUpgradeTargetLog(line));
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(prefixUpgradeTargetLog(line));
			}
			
		}).getReturnCode();
		
		if (ret != 0) {
			logger.error("Unable to upgrade specified installation due to above error");
			System.exit(1);
		}
	}

	private String getBackupName(String name) {
		return name + "~" + DateTimeFormat.forPattern(BACKUP_DATETIME_FORMAT).print(new DateTime());		
	}
	
	protected void copyProgramFilesTo(File upgradeDir) {
		cleanAndCopy(new File(Bootstrap.installDir, "3rdparty-licenses"), new File(upgradeDir, "3rdparty-licenses"));
		
		File siteServerScriptFile = new File(upgradeDir, "bin/server.sh");
		for (File file: Bootstrap.getBinDir().listFiles()) {
			try {
				String serverScript = null;
				if (file.getName().equals("server.sh")) {
					String siteRunAsUserLine = null;
					for (String line: (List<String>)FileUtils.readLines(siteServerScriptFile, Charset.defaultCharset())) {
						if (line.contains("RUN_AS_USER")) {
							siteRunAsUserLine = line;
							break;
						}
					}
					if (siteRunAsUserLine != null) {
						String newRunAsUserLine = null;
						for (String line: (List<String>)FileUtils.readLines(file, Charset.defaultCharset())) {
							if (line.contains("RUN_AS_USER")) {
								newRunAsUserLine = line;
								break;
							}
						}
						if (newRunAsUserLine != null) {
							serverScript = FileUtils.readFileToString(file, Charset.defaultCharset());
							serverScript = StringUtils.replace(serverScript, newRunAsUserLine, siteRunAsUserLine);
						}
					}
				}
				if (serverScript != null)
					FileUtils.writeStringToFile(siteServerScriptFile, serverScript, Charset.defaultCharset());
				else 
					FileUtils.copyFile(file, new File(upgradeDir, "bin/" + file.getName()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		cleanAndCopy(Bootstrap.getBootDir(), new File(upgradeDir, "boot"));
		cleanAndCopy(Bootstrap.getLibDir(), new File(upgradeDir, "lib"));
		for (File file: new File(Bootstrap.getSiteDir(), "avatars").listFiles()) {
			try {
				FileUtils.copyFileToDirectory(file, new File(upgradeDir, "site/avatars"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		File uploadedDir = new File(upgradeDir, "site/avatars/uploaded");
		File userUploadsDir = new File(uploadedDir, "users"); 
		if (uploadedDir.exists() && !userUploadsDir.exists()) {
			FileUtils.createDir(userUploadsDir);
			for (File file: uploadedDir.listFiles()) {
				if (file.isFile()) {
					try {
						FileUtils.moveFileToDirectory(file, userUploadsDir, false);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		for (File file: Bootstrap.getSiteLibDir().listFiles()) {
			// end user may put some custom program files in site lib before upgrade, 
			// to override site lib of old version
			if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
				cleanAndCopy(Bootstrap.getSiteLibDir(), new File(upgradeDir, "site/lib"));
				break;
			}
		}
		try {
			File wrapperConfFile = new File(upgradeDir, "conf/wrapper.conf");
			String wrapperConf = FileUtils.readFileToString(wrapperConfFile, Charsets.UTF_8);
			wrapperConf = StringUtils.replace(wrapperConf, "com.gitplex.commons.bootstrap.Bootstrap", 
					"io.onedev.commons.launcher.bootstrap.Bootstrap");
			wrapperConf = StringUtils.replace(wrapperConf, "com.gitplex.launcher.bootstrap.Bootstrap", 
					"io.onedev.commons.launcher.bootstrap.Bootstrap");
			wrapperConf = StringUtils.replace(wrapperConf, "com.turbodev.launcher.bootstrap.Bootstrap", 
					"io.onedev.commons.launcher.bootstrap.Bootstrap");
			wrapperConf = StringUtils.replace(wrapperConf, "io.onedev.launcher.bootstrap.Bootstrap", 
					"io.onedev.commons.launcher.bootstrap.Bootstrap");
			FileUtils.writeStringToFile(wrapperConfFile, wrapperConf, Charsets.UTF_8);
			
			File hibernatePropsFile = new File(upgradeDir, "conf/hibernate.properties");
			String hibernateProps = FileUtils.readFileToString(hibernatePropsFile, Charsets.UTF_8);
			hibernateProps = StringUtils.replace(hibernateProps, "hibernate.hikari.autoCommit=false", 
					"hibernate.hikari.autoCommit=true");
			hibernateProps = StringUtils.replace(hibernateProps, "GitPlex", "OneDev");
			hibernateProps = StringUtils.replace(hibernateProps, "TurboDev", "OneDev");
			
			if (!hibernateProps.contains("hibernate.connection.autocommit=true")) {
				hibernateProps = StringUtils.replace(hibernateProps, 
						"hibernate.connection.provider_class=org.hibernate.hikaricp.internal.HikariCPConnectionProvider", 
						"hibernate.connection.provider_class=org.hibernate.hikaricp.internal.HikariCPConnectionProvider\r\n"
						+ "hibernate.connection.autocommit=true");
			}
			FileUtils.writeStringToFile(hibernatePropsFile, hibernateProps, Charsets.UTF_8);

			File logbackConfigFile = new File(upgradeDir, "conf/logback.xml");
			String logbackConfig = FileUtils.readFileToString(logbackConfigFile, Charsets.UTF_8);
			logbackConfig = StringUtils.replace(logbackConfig, "<triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\"/>", 
					"<triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\"><maxFileSize>1MB</maxFileSize></triggeringPolicy>");
			logbackConfig = StringUtils.replace(logbackConfig, "gitplex", "turbodev");
			logbackConfig = StringUtils.replace(logbackConfig, "com.turbodev", "io.onedev");
			logbackConfig = StringUtils.replace(logbackConfig, "turbodev", "onedev");
			FileUtils.writeStringToFile(logbackConfigFile, logbackConfig, Charsets.UTF_8);
			
			FileUtils.copyFile(new File(Bootstrap.installDir, "conf/wrapper-license.conf"), 
					new File(upgradeDir, "conf/wrapper-license.conf"));
			FileUtils.copyFile(new File(Bootstrap.installDir, "readme.txt"), new File(upgradeDir, "readme.txt"));
			FileUtils.copyFile(new File(Bootstrap.installDir, "license.txt"), new File(upgradeDir, "license.txt"));
			FileUtils.copyFile(new File(Bootstrap.installDir, "version.txt"), new File(upgradeDir, "version.txt"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		restoreExecutables(upgradeDir);
	}
	
	protected void cleanAndCopy(File srcDir, File destDir) {
		try {
			FileUtils.cleanDir(destDir);
			FileUtils.copyDirectory(srcDir, destDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void restoreExecutables(File installDir) {
		for (File file: new File(installDir, "bin").listFiles()) {
			if (file.getName().endsWith(".sh"))
				file.setExecutable(true);
		}
	}
	
}
