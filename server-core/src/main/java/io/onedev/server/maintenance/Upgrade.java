package io.onedev.server.maintenance;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SystemUtils;
import org.hibernate.Interceptor;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.migration.DataMigrator;
import io.onedev.server.migration.MigrationHelper;
import io.onedev.server.persistence.DefaultPersistManager;
import io.onedev.server.persistence.HibernateProperties;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.validation.EntityValidator;

@Singleton
public class Upgrade extends DefaultPersistManager {

	public static final String COMMAND = "upgrade";
	
	private static final Logger logger = LoggerFactory.getLogger(Upgrade.class);
	
	public static final String BACKUP_DATETIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
	
	public static final String DB_BACKUP_DIR = "db-backup";
	
	@Inject
	public Upgrade(PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator, 
			TransactionManager transactionManager) {
		super(physicalNamingStrategy, properties, interceptor, idManager, dao, validator, transactionManager);
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
		} else if (version.startsWith("2.0.")) {
			bootstrapClass = "io.onedev.launcher.bootstrap.Bootstrap";
		} else {
			bootstrapClass = "io.onedev.commons.launcher.bootstrap.Bootstrap";
		}
		
		if (version.startsWith("1.") ||  version.startsWith("2.")) {
			switch (command) {
			case "check-data-version":
				command = "check_data_version";
				break;
			case "backup-db":
				command = "backup";
				break;
			case "clean-db":
				command = "clean";
				break;
			}
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
			logger.error("Missing upgrade target parameter. Usage: {} <OneDev installation directory>", 
					Bootstrap.command.getScript());
			System.exit(1);
		}
		File upgradeDir = new File(Bootstrap.command.getArgs()[0]);
		if (!upgradeDir.isAbsolute())
			upgradeDir = new File(Bootstrap.getBinDir(), upgradeDir.getPath());
		
		if (!upgradeDir.exists()) {
			logger.error("Unable to find directory: {}", upgradeDir.getAbsolutePath());
			System.exit(1);
		}
		
		boolean isEmpty = true;
		for (File file: upgradeDir.listFiles()) {
			if (!file.getName().equals("lost+found")) {
				isEmpty = false;
				break;
			}
		}
		if (!isEmpty) {
			if (!new File(upgradeDir, "boot/bootstrap.keys").exists()) {
				logger.error("Invalid OneDev installation directory: {}, make sure you are specifying the top level "
						+ "installation directory (it contains sub directories such as \"bin\", \"boot\", \"conf\", etc)", 
						upgradeDir.getAbsolutePath());
				System.exit(1);
			}

			logger.info("Upgrading {}...", upgradeDir.getAbsolutePath());
			
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
						File destDir = new File(upgradeDir, "site/lib");
						FileUtils.createDir(destDir);
						FileUtils.copyFileToDirectory(file, destDir);
						FileUtils.deleteFile(file);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			
			AtomicInteger oldDataVersion = new AtomicInteger(0);
			
			int ret = buildCommandline(upgradeDir, "check-data-version").execute(new LineConsumer() {
				
				@Override
				public void consume(String line) {
					String prefix = "Data version: ";
					if (line.startsWith(prefix)) {
						oldDataVersion.set(Integer.parseInt(line.substring(prefix.length())));
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
			
			int newDataVersion = Integer.parseInt(MigrationHelper.getVersion(DataMigrator.class));
			
			if (oldDataVersion.get() > newDataVersion) {
				logger.error("Unable to upgrade as specified installation is more recent");
				System.exit(1);
			}
			
			String timestamp = DateTimeFormat.forPattern(BACKUP_DATETIME_FORMAT).print(new DateTime());
			File programBackup = new File(upgradeDir, "site/program-backup/" + timestamp);
			FileUtils.createDir(programBackup);
			
			logger.info("Backing up old program files as {}...", programBackup.getAbsolutePath());
			try {
				for (File each: upgradeDir.listFiles()) {
					if (each.isFile()) 
						FileUtils.copyFileToDirectory(each, programBackup);
					else if (!each.getName().equals("temp") && !each.getName().equals("site"))
						FileUtils.copyDirectoryToDirectory(each, programBackup);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			restoreExecutables(programBackup);
			
			File dbBackupFile = null;
			boolean failed = false;
			try {
				if (oldDataVersion.get() != newDataVersion) {
					dbBackupFile = new File(upgradeDir, "site/" + DB_BACKUP_DIR + "/" + timestamp + ".zip");
					logger.info("Backing up database as {}...", dbBackupFile.getAbsolutePath());
					
					FileUtils.createDir(dbBackupFile.getParentFile());
					
					ret = buildCommandline(upgradeDir, "backup-db", dbBackupFile.getAbsolutePath()).execute(new LineConsumer() {

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
					
					if (ret == 0) {
						logger.info("Cleaning database with old program...");
						ret = buildCommandline(upgradeDir, "clean-db").execute(new LineConsumer() {
	
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
					}
					
					if (ret == 0) {
						logger.info("Updating program files...");
						updateProgramFiles(upgradeDir);

						logger.info("Restoring database with new program...");
						ret = buildCommandline(upgradeDir, "restore-db", dbBackupFile.getAbsolutePath()).execute(new LineConsumer() {

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
					}
					
					if (ret != 0) {
						logger.error("Failed to upgrade {}", upgradeDir.getAbsolutePath());
						failed = true;
					} 
					
				} else {
					logger.info("Copying new program files into {}...", upgradeDir.getAbsolutePath());
					updateProgramFiles(upgradeDir);
				}
			} catch (Exception e) {
				logger.error("Error upgrading " + upgradeDir.getAbsolutePath(), e);
				failed = true;
			}
			
			if (failed) {
				logger.info("Restoring old program files due to upgrade failure...");
				try {
					for (File child: programBackup.listFiles()) {
						if (!child.getName().equals("site")) {
							File target = new File(upgradeDir, child.getName());
							if (target.isDirectory()) {
								FileUtils.cleanDir(target);
								FileUtils.copyDirectory(child, target);
							} else {
								FileUtils.copyFile(child, target);
							}
						}
					}
					
					restoreExecutables(upgradeDir);
					logger.info("Old program files restored");
					FileUtils.deleteDir(programBackup);
				} catch (Exception e) {
					logger.error("Error restoring old program files", e);
					logger.warn("Please restore manually from \"{}\"", programBackup.getAbsolutePath());
				}
				if (dbBackupFile != null) {
					logger.warn("The database might be in an inconsistent state due to upgrade failure. In that case, "
							+ "you need to restore the database by first cleaning it, and then running below command:");
					if (SystemUtils.IS_OS_WINDOWS) {
						logger.info(upgradeDir.getAbsolutePath() + File.separator + "bin" + File.separator + "restore-db.bat " + dbBackupFile.getAbsolutePath());
					} else {
						logger.info(upgradeDir.getAbsolutePath() + File.separator + "bin" + File.separator + "restore-db.sh " + dbBackupFile.getAbsolutePath());
					}
				}
				System.exit(1);
			} else {
				logger.info("Successfully upgraded {}", upgradeDir.getAbsolutePath());
				
				if (oldDataVersion.get() <= 5) {
					logger.warn("\n"
							+ "************************* IMPORTANT NOTICE *************************\n"
							+ "* OneDev password hash algorithm has been changed for security    *\n"
							+ "* reason. Please reset administrator password with the             *\n"
							+ "* reset_admin_password command. Other users' password will be      *\n"
							+ "* reset and sent to their mail box automatically when they logs    *\n"
							+ "* into OneDev.                                                    *\n"
							+ "********************************************************************");
				}
				FileUtils.deleteDir(programBackup);
				System.exit(0);
			}			
		} else {
			logger.info("Populating {}...", upgradeDir.getAbsolutePath());
			
			if (Bootstrap.isServerRunning(Bootstrap.installDir)) {
				logger.error("Please stop server running at \"{}\" before populating", Bootstrap.installDir.getAbsolutePath());
				System.exit(1);
			}

			try {
				FileUtils.copyDirectory(Bootstrap.installDir, upgradeDir);
				restoreExecutables(upgradeDir);
				logger.info("Successfully populated {}", upgradeDir.getAbsolutePath());
				System.exit(0);
			} catch (Exception e) {
				logger.error("Error populating " + upgradeDir.getAbsolutePath(), e);
				FileUtils.cleanDir(upgradeDir);
				System.exit(1);
			}
		}
	}
	
	protected void updateProgramFiles(File upgradeDir) {
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

		if (new File(upgradeDir, "bin/apply_db_constraints.bat").exists())
			FileUtils.deleteFile(new File(upgradeDir, "bin/apply_db_constraints.bat"));
		if (new File(upgradeDir, "bin/apply_db_constraints.sh").exists())
			FileUtils.deleteFile(new File(upgradeDir, "bin/apply_db_constraints.sh"));
		if (new File(upgradeDir, "bin/backup.bat").exists())
			FileUtils.deleteFile(new File(upgradeDir, "bin/backup.bat"));
		if (new File(upgradeDir, "bin/backup.sh").exists())
			FileUtils.deleteFile(new File(upgradeDir, "bin/backup.sh"));
		if (new File(upgradeDir, "bin/restore.bat").exists())
			FileUtils.deleteFile(new File(upgradeDir, "bin/restore.bat"));
		if (new File(upgradeDir, "bin/restore.sh").exists())
			FileUtils.deleteFile(new File(upgradeDir, "bin/restore.sh"));
		if (new File(upgradeDir, "bin/reset_admin_password.bat").exists())
			FileUtils.deleteFile(new File(upgradeDir, "bin/reset_admin_password.bat"));
		if (new File(upgradeDir, "bin/reset_admin_password.sh").exists())
			FileUtils.deleteFile(new File(upgradeDir, "bin/reset_admin_password.sh"));
		
		cleanAndCopy(Bootstrap.getBootDir(), new File(upgradeDir, "boot"));
		if (new File(upgradeDir, "boot/system.classpath").exists())
			FileUtils.deleteFile(new File(upgradeDir, "boot/system.classpath"));
		
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
		try {
			File wrapperConfFile = new File(upgradeDir, "conf/wrapper.conf");
			String wrapperConf = FileUtils.readFileToString(wrapperConfFile, StandardCharsets.UTF_8);
			wrapperConf = StringUtils.replace(wrapperConf, "com.gitplex.commons.bootstrap.Bootstrap", 
					"io.onedev.commons.launcher.bootstrap.Bootstrap");
			wrapperConf = StringUtils.replace(wrapperConf, "com.gitplex.launcher.bootstrap.Bootstrap", 
					"io.onedev.commons.launcher.bootstrap.Bootstrap");
			wrapperConf = StringUtils.replace(wrapperConf, "com.turbodev.launcher.bootstrap.Bootstrap", 
					"io.onedev.commons.launcher.bootstrap.Bootstrap");
			wrapperConf = StringUtils.replace(wrapperConf, "io.onedev.launcher.bootstrap.Bootstrap", 
					"io.onedev.commons.launcher.bootstrap.Bootstrap");
			FileUtils.writeStringToFile(wrapperConfFile, wrapperConf, StandardCharsets.UTF_8);
			
			File hibernatePropsFile = new File(upgradeDir, "conf/hibernate.properties");
			String hibernateProps = FileUtils.readFileToString(hibernatePropsFile, StandardCharsets.UTF_8);
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
			
			hibernateProps = StringUtils.replace(hibernateProps, "hibernate.connection.autocommit=true", "");
			hibernateProps = StringUtils.replace(hibernateProps, "hibernate.hikari.leakDetectionThreshold=1800000", "");
			hibernateProps = StringUtils.replace(hibernateProps, "org.hibernate.cache.ehcache.EhCacheRegionFactory", 
					"org.hibernate.cache.jcache.JCacheRegionFactory");
			if (!hibernateProps.contains("hibernate.cache.auto_evict_collection_cache=true")) {
				hibernateProps += "hibernate.cache.auto_evict_collection_cache=true\r\n";
				hibernateProps += "hibernate.javax.cache.provider=org.ehcache.jsr107.EhcacheCachingProvider\r\n"; 
				hibernateProps += "hibernate.javax.cache.missing_cache_strategy=create\r\n";
			}
			
			FileUtils.writeStringToFile(hibernatePropsFile, hibernateProps, StandardCharsets.UTF_8);
			
			File serverPropsFile = new File(upgradeDir, "conf/server.properties");
			String serverProps = FileUtils.readFileToString(serverPropsFile, StandardCharsets.UTF_8);
			if (serverProps.contains("sessionTimeout")) 
				FileUtils.copyFile(new File(Bootstrap.getConfDir(), "server.properties"), serverPropsFile);
			serverProps = FileUtils.readFileToString(serverPropsFile, StandardCharsets.UTF_8);
			if (!serverProps.contains("ssh_port")) {
				serverProps += String.format("%n%nssh_port: 6611%n");
				FileUtils.writeStringToFile(serverPropsFile, serverProps, StandardCharsets.UTF_8);
			}

			File logbackPropsFile = new File(upgradeDir, "conf/logback.xml");
			String logbackProps = FileUtils.readFileToString(logbackPropsFile, StandardCharsets.UTF_8);
			if (!logbackProps.contains("AggregatedServiceLoader")) 
				FileUtils.copyFile(new File(Bootstrap.getConfDir(), "logback.xml"), logbackPropsFile);
			
			File sampleKeystoreFile = new File(upgradeDir, "conf/sample.keystore");
			if (sampleKeystoreFile.exists())
				FileUtils.deleteFile(sampleKeystoreFile);
			
			File logbackConfigFile = new File(upgradeDir, "conf/logback.xml");
			String logbackConfig = FileUtils.readFileToString(logbackConfigFile, StandardCharsets.UTF_8);
			logbackConfig = StringUtils.replace(logbackConfig, "<triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\"/>", 
					"<triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\"><maxFileSize>1MB</maxFileSize></triggeringPolicy>");
			logbackConfig = StringUtils.replace(logbackConfig, "gitplex", "turbodev");
			logbackConfig = StringUtils.replace(logbackConfig, "com.turbodev", "io.onedev");
			logbackConfig = StringUtils.replace(logbackConfig, "turbodev", "onedev");
			FileUtils.writeStringToFile(logbackConfigFile, logbackConfig, StandardCharsets.UTF_8);
			
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
		for (File file: new File(installDir, "boot").listFiles()) {
			if (file.getName().startsWith("wrapper-"))
				file.setExecutable(true);
		}
	}
	
}
