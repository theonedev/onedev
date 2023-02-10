package io.onedev.server.commandhandler;

import com.google.common.base.Joiner;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.OneDev;
import io.onedev.server.migration.DataMigrator;
import io.onedev.server.migration.MigrationHelper;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.security.SecurityUtils;
import org.apache.commons.lang3.SystemUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hibernate.cfg.AvailableSettings.DIALECT;

@Singleton
public class Upgrade extends AbstractPlugin {

	public static final String COMMAND = "upgrade";
	
	private static final Logger logger = LoggerFactory.getLogger(Upgrade.class);
	
	public static final String BACKUP_DATETIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
	
	public static final String DB_BACKUP_DIR = "db-backup";
	
	private static final String RELEASE_PROPS_FILE = "release.properties";
	
	public static final String INCOMPATIBILITIES = "incompatibilities/incompatibilities.md";
	
	public static final String INCOMPATIBILITIES_SINCE_UPGRADED_VERSION = "incompatibilities/since-upgraded-version.md"; 
	
	public static final String CHECKED_INCOMPATIBILITIES_SINCE_UPGRADED_VERSION = "incompatibilities/checked-since-upgraded-version.md"; 
	
	protected Commandline buildCommandline(File upgradeDir, String command, String...commandArgs) {
		String version = loadReleaseProps(new File(upgradeDir, RELEASE_PROPS_FILE)).getProperty("version");
		
		if (version == null) {
			File versionFile = new File(upgradeDir, "version.txt");
			if (versionFile.exists()) {
				try {
					version = FileUtils.readFileToString(versionFile, Charset.defaultCharset()).trim();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		if (version == null)
			version = "1.0-EAP-build26";
		
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
		} else if (version.startsWith("3.") || version.startsWith("4.0.") || version.startsWith("4.1.")
				|| version.startsWith("4.2.") || version.startsWith("4.3.") || version.startsWith("4.4.")
				|| version.startsWith("4.5.") || version.startsWith("4.6.") || version.startsWith("4.7.")
				|| version.startsWith("4.8.") || version.startsWith("4.9.")) {
			bootstrapClass = "io.onedev.commons.launcher.bootstrap.Bootstrap";
		} else {
			bootstrapClass = "io.onedev.commons.bootstrap.Bootstrap";
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
		cmdline.addArgs(
				"-Xmx" + Runtime.getRuntime().maxMemory()/1024/1024 + "m",
				"--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
				"--add-opens=java.base/java.lang=ALL-UNNAMED",
				"--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
				"--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
				"--add-opens=java.base/java.util=ALL-UNNAMED",
				"--add-opens=java.base/java.text=ALL-UNNAMED",
				"--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
				"--add-modules=java.se",
				"--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED",
				"--add-opens=java.management/sun.management=ALL-UNNAMED",
				"--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED",
				"-classpath", "*", bootstrapClass, 
				command);
		cmdline.addArgs(commandArgs);
		cmdline.workingDir(new File(upgradeDir, "boot"));
		return cmdline;
	}
	
	private String prefixUpgradeTargetLog(String line) {
		return ">>> " + line;
	}
	
	private String getDialect(File installDir) {
		String dialect = System.getenv(DIALECT.replace('.', '_'));
		if (dialect == null) {
			File file = new File(installDir, "conf/hibernate.properties"); 
			dialect = FileUtils.loadProperties(file).getProperty(DIALECT);
		}
		return dialect;
	}
	
	private Properties loadReleaseProps(File releasePropsFile) {
		if (releasePropsFile.exists())
			return FileUtils.loadProperties(releasePropsFile);
		else
			return new Properties();
	}
	
	private int getDataVersion(File upgradeDir) {
		AtomicInteger oldDataVersion = new AtomicInteger(0);

		int ret = buildCommandline(upgradeDir, "check-data-version").execute(new LineConsumer() {

			 @Override
			 public void consume(String line) {
				 String prefix = "Data version: ";
				 if (line.startsWith(prefix)) {
					 oldDataVersion.set(Integer.parseInt(line.substring(prefix.length())));
					 logger.info("Data version: " + oldDataVersion.get());
				 } else {
					 logger.info(prefixUpgradeTargetLog(line));
				 }
			 }

		 }, new LineConsumer() {

			 @Override
			 public void consume(String line) {
				 logger.error(prefixUpgradeTargetLog(line));
			 }
			 
		}).getReturnCode();
		
		if (ret != 0)
			return -1;
		else 
			return oldDataVersion.get();
	}
	
	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		if (Bootstrap.command.getArgs().length == 0) {
			logger.error("Missing upgrade target parameter. Usage: {} <OneDev installation directory>", 
					Bootstrap.command.getScript());
			System.exit(1);
		}
		
		File upgradeDir = new File(Bootstrap.command.getArgs()[0]);
		if (!upgradeDir.isAbsolute() && System.getenv("WRAPPER_INIT_DIR") != null)
			upgradeDir = new File(System.getenv("WRAPPER_INIT_DIR"), upgradeDir.getPath());
		
		if (!upgradeDir.exists()) {
			logger.error("Unable to find directory: {}", upgradeDir.getAbsolutePath());
			System.exit(1);
		} else if (!upgradeDir.isDirectory()) {
			logger.error("A directory is expected: {}", upgradeDir.getAbsolutePath());
			System.exit(1);
		}
		
		boolean isEmpty = true;
		for (File file: upgradeDir.listFiles()) {
			if (!file.getName().equals("lost+found") 
					&& !file.getName().equals("site") 
					&& !file.getName().equals("conf") // conf/trust-certs might be mounted
					&& !file.getName().startsWith(".")) {
				isEmpty = false;
				break;
			}
		}
		if (!isEmpty) {
			if (!new File(upgradeDir, "boot").exists()) {
				logger.error("Invalid OneDev installation directory: {}, make sure you are specifying the top level "
						+ "installation directory (it contains sub directories such as \"bin\", \"boot\", \"conf\", etc)", 
						upgradeDir.getAbsolutePath());
				System.exit(1);
			}
			
			Properties releaseProps = loadReleaseProps(new File(Bootstrap.installDir, RELEASE_PROPS_FILE));
			Properties oldReleaseProps = loadReleaseProps(new File(upgradeDir, RELEASE_PROPS_FILE));
			
			if (!releaseProps.equals(oldReleaseProps)) {
				logger.info("Upgrading {}...", upgradeDir.getAbsolutePath());
				
				if (OneDev.isServerRunning(upgradeDir) || OneDev.isServerRunning(Bootstrap.installDir)) {
					logger.error("Please stop server before upgrading");
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
				
				File statusDir = new File(upgradeDir, "status");
				if (statusDir.exists())
					FileUtils.cleanDir(statusDir);

				try {
					if (new File(upgradeDir, "sampledb").exists() 
							&& !new File(upgradeDir, "internaldb").exists()) { 
						FileUtils.moveDirectory(
								new File(upgradeDir, "sampledb"), 
								new File(upgradeDir, "internaldb"));
					}

					File hibernatePropsFile = new File(upgradeDir, "conf/hibernate.properties");
					String hibernateProps = FileUtils.readFileToString(hibernatePropsFile, StandardCharsets.UTF_8);
					if (hibernateProps.contains("sampledb")) {
						hibernateProps = StringUtils.replace(hibernateProps, "sampledb", "internaldb");
						FileUtils.writeStringToFile(hibernatePropsFile, hibernateProps, StandardCharsets.UTF_8);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				int oldDataVersion = getDataVersion(upgradeDir);
				if (oldDataVersion == -1) {
					logger.error("Unable to upgrade specified installation due to above error");
					System.exit(1);
				}
				
				int newDataVersion = Integer.parseInt(MigrationHelper.getVersion(DataMigrator.class));
				
				if (oldDataVersion > newDataVersion) {
					logger.error("OneDev program is too old, please use a newer version");
					System.exit(1);
				}
				
				String timestamp = DateTimeFormat.forPattern(BACKUP_DATETIME_FORMAT).print(new DateTime());
				File programBackup = new File(upgradeDir, "site/program-backup/" + timestamp);
				FileUtils.createDir(programBackup);
				
				logger.info("Backing up old program files as {}...", programBackup.getAbsolutePath());
				try {
					for (File each: upgradeDir.listFiles()) {
						if (each.isFile()) {
							FileUtils.copyFileToDirectory(each, programBackup);
						} else if (!each.getName().equals("temp") 
								&& !each.getName().equals("site") 
								&& !each.getName().equals("internaldb")) {
							FileUtils.copyDirectoryToDirectory(each, programBackup);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				restoreExecutables(programBackup);
				
				File dbBackupFile = new File(upgradeDir, "site/" + DB_BACKUP_DIR + "/" + timestamp + ".zip");
				boolean isHSQL = HibernateConfig.isHSQLDialect(getDialect(upgradeDir));
				boolean failed = false;
				boolean dbChanged = false;
				boolean dbCleaned = false;
				try {
					if (oldDataVersion != newDataVersion) {
						logger.info("Backing up database as {}...", dbBackupFile.getAbsolutePath());
						
						FileUtils.createDir(dbBackupFile.getParentFile());

						int ret = buildCommandline(upgradeDir, "backup-db", dbBackupFile.getAbsolutePath()).execute(new LineConsumer() {

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
							dbChanged = true;
							if (isHSQL) { 
								FileUtils.deleteDir(new File(upgradeDir, "internaldb"), 3);
							} else {
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
							if (ret == 0)
								dbCleaned = true;
						}
						
						if (ret == 0) {
							logger.info("Updating program files...");
							updateProgramFiles(upgradeDir, oldDataVersion);

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
							dbCleaned = getDataVersion(upgradeDir) == -1;
							failed = true;
						} else {
							dbCleaned = false;
						}
					} else {
						logger.info("Copying new program files into {}...", upgradeDir.getAbsolutePath());
						updateProgramFiles(upgradeDir, oldDataVersion);
					}
				} catch (Exception e) {
					logger.error("Error upgrading " + upgradeDir.getAbsolutePath(), e);
					failed = true;
				}
				
				if (failed) {
					if (dbChanged && !dbCleaned) {
						logger.info("Cleaning database with new program...");
						try {
							buildCommandline(upgradeDir, "clean-db").execute(new LineConsumer() {
								
								@Override
								public void consume(String line) {
									logger.info(prefixUpgradeTargetLog(line));
								}
								
							}, new LineConsumer() {
		
								@Override
								public void consume(String line) {
									logger.error(prefixUpgradeTargetLog(line));
								}
								
							}).checkReturnCode();
							
							dbCleaned = true;
						} catch (Exception e) {
							logger.error("Error cleaning database", e);
						}
					}
					
					boolean programRestored = false;
					logger.info("Restoring old program files...");
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
						
						if (oldDataVersion <= 102) 
							FileUtils.createDir(new File(upgradeDir, "site/assets/root"));
						
						restoreExecutables(upgradeDir);
						FileUtils.deleteDir(programBackup);
						
						logger.info("Old program files restored");
						programRestored = true;
					} catch (Exception e) {
						logger.error("Error restoring old program files", e);
						logger.warn("Please restore manually from \"{}\"", programBackup.getAbsolutePath());
					}
					
					if (programRestored && dbChanged && dbCleaned) {
						logger.info("Restoring old database...");
						try {
							buildCommandline(upgradeDir, "restore-db", dbBackupFile.getAbsolutePath(), "false").execute(new LineConsumer() {
	
								@Override
								public void consume(String line) {
									logger.info(prefixUpgradeTargetLog(line));
								}
								
							}, new LineConsumer() {
	
								@Override
								public void consume(String line) {
									logger.error(prefixUpgradeTargetLog(line));
								}
								
							}).checkReturnCode();
							
							logger.info("Old database restored");
							dbChanged = false;
						} catch (Exception e) {
							logger.error("Error restoring old database", e);
						}
					}
					
					logger.error("️!! Failed to upgrade {}", upgradeDir.getAbsolutePath());
					if (dbChanged) {
						logger.warn("OneDev is unable to restore old database, please do it manually by first resetting it (delete and create), and then running below command:");
						if (SystemUtils.IS_OS_WINDOWS) {
							logger.info(upgradeDir.getAbsolutePath() + File.separator + "bin" + File.separator + "restore-db.bat " + dbBackupFile.getAbsolutePath());
						} else {
							logger.info(upgradeDir.getAbsolutePath() + File.separator + "bin" + File.separator + "restore-db.sh " + dbBackupFile.getAbsolutePath());
						}
					}
					System.exit(1);
				} else {
					logger.info("Successfully upgraded {}", upgradeDir.getAbsolutePath());
					
					if (oldDataVersion <= 5) {
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
				logger.info("Successfully checked {}", upgradeDir.getAbsolutePath());
				System.exit(0);
			}
		} else {
			logger.info("Populating {}...", upgradeDir.getAbsolutePath());
			
			if (OneDev.isServerRunning(Bootstrap.installDir)) {
				logger.error("Please stop server before populating");
				System.exit(1);
			}

			try {
				FileUtils.copyDirectory(Bootstrap.installDir, upgradeDir);
				FileUtils.cleanDir(new File(upgradeDir, "logs"));
				restoreExecutables(upgradeDir);
				logger.info("Successfully populated {}", upgradeDir.getAbsolutePath());
				System.exit(0);
			} catch (Exception e) {
				logger.error("!! Error populating " + upgradeDir.getAbsolutePath(), e);
				FileUtils.cleanDir(upgradeDir);
				System.exit(1);
			}
		}
	}
	
	protected void updateProgramFiles(File upgradeDir, int oldDataVersion) {
		cleanAndCopy(new File(Bootstrap.installDir, "3rdparty-licenses"), new File(upgradeDir, "3rdparty-licenses"));

		File siteServerScriptFile = new File(upgradeDir, "bin/server.sh");
		for (File file: Bootstrap.getBinDir().listFiles()) {
			if (!file.getName().endsWith(".pid") && !file.getName().endsWith(".status")) {
				try {
					String serverScript = null;
					if (file.getName().equals("server.sh")) {
						serverScript = FileUtils.readFileToString(file, Charset.defaultCharset());
						
						String siteRunAsUserLine = null;
						for (String line: (List<String>)FileUtils.readLines(siteServerScriptFile, Charset.defaultCharset())) {
							if (line.contains("RUN_AS_USER")) {
								siteRunAsUserLine = line;
								break;
							}
						}
						if (siteRunAsUserLine != null) {
							String runAsUserLine = null;
							for (String line: (List<String>)FileUtils.readLines(file, Charset.defaultCharset())) {
								if (line.contains("RUN_AS_USER")) {
									runAsUserLine = line;
									break;
								}
							}
							if (runAsUserLine != null) 
								serverScript = StringUtils.replace(serverScript, runAsUserLine, siteRunAsUserLine);
						}
						
						String siteFilesToSourceLine = null;
						for (String line: (List<String>)FileUtils.readLines(siteServerScriptFile, Charset.defaultCharset())) {
							if (line.contains("FILES_TO_SOURCE") && !line.startsWith("#")) {
								siteFilesToSourceLine = line;
								break;
							}
						}
						if (siteFilesToSourceLine != null) {
							String filesToSourceLine = null;
							for (String line: (List<String>)FileUtils.readLines(file, Charset.defaultCharset())) {
								if (line.contains("FILES_TO_SOURCE") && !line.startsWith("#")) {
									filesToSourceLine = line;
									break;
								}
							}
							if (filesToSourceLine != null) 
								serverScript = StringUtils.replace(serverScript, filesToSourceLine, siteFilesToSourceLine);
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
		
		FileUtils.deleteDir(new File(upgradeDir, "status"));
		
		cleanAndCopy(Bootstrap.getBootDir(), new File(upgradeDir, "boot"));
		cleanAndCopy(new File(Bootstrap.installDir, "agent"), new File(upgradeDir, "agent"));
		
		cleanAndCopy(Bootstrap.getLibDir(), new File(upgradeDir, "lib"));

		FileUtils.createDir(new File(upgradeDir, "site/assets"));
		if (new File(upgradeDir, "site/robots.txt").exists()) {
			try {
				FileUtils.copyFile(
						new File(upgradeDir, "site/robots.txt"), 
						new File(upgradeDir, "site/assets/robots.txt"));
				FileUtils.deleteFile(new File(upgradeDir, "site/robots.txt"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (new File(upgradeDir, "site/logo.png").exists()) {
			try {
				FileUtils.copyFile(
						new File(upgradeDir, "site/logo.png"), 
						new File(upgradeDir, "site/assets/logo.png"));
				FileUtils.deleteFile(new File(upgradeDir, "site/logo.png"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (new File(upgradeDir, "site/avatars").exists()) {
			try {
				FileUtils.copyDirectory(
						new File(upgradeDir, "site/avatars"), 
						new File(upgradeDir, "site/assets/avatars"));
				FileUtils.deleteDir(new File(upgradeDir, "site/avatars"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		for (File file: new File(Bootstrap.getSiteDir(), "assets/avatars").listFiles()) {
			if (file.isFile()) {
				try {
					FileUtils.copyFileToDirectory(file, new File(upgradeDir, "site/assets/avatars"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		File uploadedDir = new File(upgradeDir, "site/assets/avatars/uploaded");
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
		
		if (oldDataVersion <= 102) {
			File assetsRootDir = new File(upgradeDir, "site/assets/root");
			FileUtils.createDir(assetsRootDir);
			if (new File(upgradeDir, "site/assets/robots.txt").exists()) {
				try {
					FileUtils.copyFile(
							new File(upgradeDir, "site/assets/robots.txt"), 
							new File(assetsRootDir, "robots.txt"));
					FileUtils.deleteFile(new File(upgradeDir, "site/assets/robots.txt"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
	
			for (File file: assetsRootDir.listFiles()) {
				try {
					FileUtils.moveToDirectory(file, new File(upgradeDir, "site/assets"), true);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
			FileUtils.deleteDir(assetsRootDir);
			
			File usersDir = new File(upgradeDir, "site/users");
			if (usersDir.exists()) {
				for (File file: usersDir.listFiles()) {
					File oldVisitInfoDir = new File(file, "info");
					File projectDir = new File(upgradeDir, "site/projects/" + file.getName());
					if (oldVisitInfoDir.exists() && projectDir.exists()) {
						File newVisitInfoDir = new File(projectDir, "info/visit");
						FileUtils.createDir(newVisitInfoDir.getParentFile());
						FileUtils.deleteDir(newVisitInfoDir);
						try {
							FileUtils.moveDirectory(oldVisitInfoDir, newVisitInfoDir);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
				FileUtils.deleteDir(usersDir);
			}
			FileUtils.deleteDir(new File(upgradeDir, "site/info"));
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
			wrapperConf = StringUtils.replace(wrapperConf, "io.onedev.commons.launcher.bootstrap.Bootstrap", 
					"io.onedev.commons.bootstrap.Bootstrap");
			wrapperConf = StringUtils.replace(wrapperConf, "wrapper.pidfile=../status/onedev.pid", "");
			wrapperConf = StringUtils.replace(wrapperConf, "-XX:+IgnoreUnrecognizedVMOptions", 
					"--add-opens=java.base/sun.nio.ch=ALL-UNNAMED");
			if (!wrapperConf.contains("java.base/jdk.internal.ref=ALL-UNNAMED")) {
				wrapperConf += ""
						+ "\r\nwrapperConfwrapper.java.additional.30=--add-modules=java.se" 
						+ "\r\nwrapper.java.additional.31=--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED" 
						+ "\r\nwrapper.java.additional.32=--add-opens=java.management/sun.management=ALL-UNNAMED"
						+ "\r\nwrapper.java.additional.33=--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED";
			}
			if (!wrapperConf.contains("java.base/sun.nio.fs=ALL-UNNAMED")) {
				wrapperConf += "\r\nwrapper.java.additional.50=--add-opens=java.base/sun.nio.fs=ALL-UNNAMED";
			}
			
			if (!wrapperConf.contains("wrapper.disable_console_input")) 
				wrapperConf += "\r\nwrapper.disable_console_input=TRUE";

			wrapperConf = wrapperConf.replaceAll("\r\n(\r\n)+\r\n", "\r\n\r\n");
			
			FileUtils.writeStringToFile(wrapperConfFile, wrapperConf, StandardCharsets.UTF_8);
			
			File hibernatePropsFile = new File(upgradeDir, "conf/hibernate.properties");
			String hibernateProps = FileUtils.readFileToString(hibernatePropsFile, StandardCharsets.UTF_8);
			hibernateProps = StringUtils.replace(hibernateProps, "hibernate.hikari.autoCommit=false", 
					"hibernate.hikari.autoCommit=true");
			hibernateProps = StringUtils.replace(hibernateProps, "GitPlex", "OneDev");
			hibernateProps = StringUtils.replace(hibernateProps, "TurboDev", "OneDev");

			if (!hibernateProps.contains("hsqldb.lob_file_scale")) {
				hibernateProps = StringUtils.replace(hibernateProps, "internaldb/onedev;", 
						"internaldb/onedev;hsqldb.lob_file_scale=4;");
			}
			if (!hibernateProps.contains("hsqldb.lob_compressed")) {
				hibernateProps = StringUtils.replace(hibernateProps, "internaldb/onedev;",
						"internaldb/onedev;hsqldb.lob_compressed=true;");
			}
			if (!hibernateProps.contains("hsqldb.tx")) {
				hibernateProps = StringUtils.replace(hibernateProps, "internaldb/onedev;",
						"internaldb/onedev;hsqldb.tx=mvcc;");
			}
			
			if (!hibernateProps.contains("hibernate.connection.autocommit=true")) {
				hibernateProps = StringUtils.replace(hibernateProps, 
						"hibernate.connection.provider_class=org.hibernate.hikaricp.internal.HikariCPConnectionProvider", 
						"hibernate.connection.provider_class=org.hibernate.hikaricp.internal.HikariCPConnectionProvider\r\n"
						+ "hibernate.connection.autocommit=true");
			}
			
			hibernateProps = StringUtils.replace(hibernateProps, "hibernate.connection.autocommit=true", "");
			hibernateProps = StringUtils.replace(hibernateProps, "hibernate.hikari.leakDetectionThreshold=1800000", "");
			hibernateProps = StringUtils.replace(hibernateProps, "org.hibernate.cache.ehcache.EhCacheRegionFactory", 
					"com.hazelcast.hibernate.HazelcastLocalCacheRegionFactory");
			hibernateProps = StringUtils.replace(hibernateProps, "org.hibernate.cache.jcache.JCacheRegionFactory", 
					"com.hazelcast.hibernate.HazelcastLocalCacheRegionFactory");
			if (!hibernateProps.contains("hibernate.cache.auto_evict_collection_cache=true")) {
				hibernateProps += "hibernate.cache.auto_evict_collection_cache=true\r\n";
				hibernateProps += "hibernate.javax.cache.provider=org.ehcache.jsr107.EhcacheCachingProvider\r\n"; 
				hibernateProps += "hibernate.javax.cache.missing_cache_strategy=create\r\n";
			}
			hibernateProps = StringUtils.replace(hibernateProps, 
					"hibernate.javax.cache.provider=org.ehcache.jsr107.EhcacheCachingProvider", 
					"");
			hibernateProps = StringUtils.replace(hibernateProps, 
					"hibernate.javax.cache.missing_cache_strategy=create", 
					"");
			hibernateProps = StringUtils.replace(hibernateProps, 
					"org.hibernate.dialect.Oracle12cDialect", 
					"io.onedev.server.persistence.OracleDialect");
			
			hibernateProps = hibernateProps.replaceAll("\r\n(\r\n)+\r\n", "\r\n\r\n");
			
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

			File logbackConfigFile = new File(upgradeDir, "conf/logback.xml");
			String logbackConfig = FileUtils.readFileToString(logbackConfigFile, StandardCharsets.UTF_8);
			if (!logbackConfig.contains("AggregatedServiceLoader") 
					|| !logbackConfig.contains("com.hazelcast.cp.CPSubsystem")) { 
				FileUtils.copyFile(new File(Bootstrap.getConfDir(), "logback.xml"), logbackConfigFile);
			}
			
			File sampleKeystoreFile = new File(upgradeDir, "conf/sample.keystore");
			if (sampleKeystoreFile.exists())
				FileUtils.deleteFile(sampleKeystoreFile);
			
			logbackConfigFile = new File(upgradeDir, "conf/logback.xml");
			logbackConfig = FileUtils.readFileToString(logbackConfigFile, StandardCharsets.UTF_8);
			logbackConfig = StringUtils.replace(logbackConfig, 
					"<triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\"/>", 
					"<triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\"><maxFileSize>1MB</maxFileSize></triggeringPolicy>");
			logbackConfig = StringUtils.replace(logbackConfig, "gitplex", "turbodev");
			logbackConfig = StringUtils.replace(logbackConfig, "com.turbodev", "io.onedev");
			logbackConfig = StringUtils.replace(logbackConfig, "turbodev", "onedev");
			
			if (!logbackConfig.contains("MaskingPatternLayout")) {
				logbackConfig = StringUtils.replace(logbackConfig, 
						"ch.qos.logback.classic.encoder.PatternLayoutEncoder",
						"ch.qos.logback.core.encoder.LayoutWrappingEncoder");
				logbackConfig = StringUtils.replace(logbackConfig, 
						"<pattern>", 
						"<layout class=\"io.onedev.commons.bootstrap.MaskingPatternLayout\">\n				<pattern>");
				logbackConfig = StringUtils.replace(logbackConfig, 
						"</pattern>", 
						"</pattern>\n			</layout>");
			}
			
			FileUtils.writeStringToFile(logbackConfigFile, logbackConfig, StandardCharsets.UTF_8);
			
			FileUtils.copyFile(new File(Bootstrap.installDir, "conf/wrapper-license.conf"), 
					new File(upgradeDir, "conf/wrapper-license.conf"));
			FileUtils.copyFile(new File(Bootstrap.installDir, "readme.txt"), new File(upgradeDir, "readme.txt"));
			FileUtils.copyFile(new File(Bootstrap.installDir, "license.txt"), new File(upgradeDir, "license.txt"));
			FileUtils.copyFile(new File(Bootstrap.installDir, RELEASE_PROPS_FILE), new File(upgradeDir, RELEASE_PROPS_FILE));

			FileUtils.createDir(new File(Bootstrap.installDir, INCOMPATIBILITIES).getParentFile());
			if (new File(upgradeDir, INCOMPATIBILITIES_SINCE_UPGRADED_VERSION).exists())
				FileUtils.deleteFile(new File(upgradeDir, INCOMPATIBILITIES_SINCE_UPGRADED_VERSION));
			if (new File(upgradeDir, CHECKED_INCOMPATIBILITIES_SINCE_UPGRADED_VERSION).exists())
				FileUtils.deleteFile(new File(upgradeDir, CHECKED_INCOMPATIBILITIES_SINCE_UPGRADED_VERSION));
			List<String> incompatibilities = FileUtils.readLines(
					new File(Bootstrap.installDir, INCOMPATIBILITIES), StandardCharsets.UTF_8);
			if (new File(upgradeDir, INCOMPATIBILITIES).exists()) {
				String lastIncompatibilityVersion = null;
				for (var line: FileUtils.readLines(new File(upgradeDir, INCOMPATIBILITIES), StandardCharsets.UTF_8)) {
					if (line.trim().startsWith("# ")) {
						lastIncompatibilityVersion = line.trim().substring(1).trim();
						break;
					}
				}
				
				if (lastIncompatibilityVersion != null) {
					var incompatibilitiesSinceUpgradedVersion = new ArrayList<>();
					for (var line: incompatibilities) {
						if (line.trim().startsWith("# ") 
								&& line.trim().substring(1).trim().equals(lastIncompatibilityVersion)) {
							break;
						} else {
							incompatibilitiesSinceUpgradedVersion.add(line);
						}
					}
					if (!incompatibilitiesSinceUpgradedVersion.isEmpty()) {
						FileUtils.writeFile(
								new File(upgradeDir, INCOMPATIBILITIES_SINCE_UPGRADED_VERSION), 
								Joiner.on("\n").join(incompatibilitiesSinceUpgradedVersion));
					}
				}
			}
			FileUtils.copyFile(new File(Bootstrap.installDir, INCOMPATIBILITIES), 
					new File(upgradeDir, INCOMPATIBILITIES));
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

	@Override
	public void stop() {
	}

}
