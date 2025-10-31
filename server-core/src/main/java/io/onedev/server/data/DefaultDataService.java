package io.onedev.server.data;

import static com.google.common.base.Throwables.getStackTraceAsString;
import static io.onedev.server.model.User.PROP_NOTIFY_OWN_EVENTS;
import static io.onedev.server.model.User.PROP_SERVICE_ACCOUNT;
import static io.onedev.server.model.support.administration.SystemSetting.PROP_CURL_LOCATION;
import static io.onedev.server.model.support.administration.SystemSetting.PROP_DISABLE_AUTO_UPDATE_CHECK;
import static io.onedev.server.model.support.administration.SystemSetting.PROP_GIT_LOCATION;
import static io.onedev.server.model.support.administration.SystemSetting.PROP_SSH_ROOT_URL;
import static io.onedev.server.model.support.administration.SystemSetting.PROP_USE_AVATAR_SERVICE;
import static io.onedev.server.model.support.administration.SystemSetting.PROP_SESSION_TIMEOUT;
import static io.onedev.server.persistence.PersistenceUtils.tableExists;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.Validator;

import org.apache.shiro.authc.credential.PasswordService;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.query.Query;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.ZipUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.commandhandler.Upgrade;
import io.onedev.server.data.migration.DataMigrator;
import io.onedev.server.data.migration.MigrationHelper;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AlertService;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.RoleService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.ModelVersion;
import io.onedev.server.model.Role;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.AgentSetting;
import io.onedev.server.model.support.administration.AlertSetting;
import io.onedev.server.model.support.administration.AuditSetting;
import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.model.support.administration.BrandingSetting;
import io.onedev.server.model.support.administration.ClusterSetting;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.GlobalPackSetting;
import io.onedev.server.model.support.administration.GlobalProjectSetting;
import io.onedev.server.model.support.administration.GlobalPullRequestSetting;
import io.onedev.server.model.support.administration.GpgSetting;
import io.onedev.server.model.support.administration.PerformanceSetting;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.ServiceDeskSetting;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.model.support.issue.LinkSpecOpposite;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.persistence.PersistenceUtils;
import io.onedev.server.persistence.SessionFactoryService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.web.util.editbean.NewUserBean;

@Singleton
public class DefaultDataService implements DataService, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultDataService.class);
	
	private static final String ENV_INITIAL_USER = "initial_user";

	private static final String ENV_INITIAL_PASSWORD_FILE = "initial_password_file";
	
	private static final String ENV_INITIAL_PASSWORD = "initial_password";
	
	private static final String ENV_INITIAL_EMAIL = "initial_email";
	
	private static final String ENV_INITIAL_SERVER_URL = "initial_server_url";
	
	private static final String ENV_INITIAL_SSH_ROOT_URL = "initial_ssh_root_url";
	
	private static final int BACKUP_BATCH_SIZE = 1000;

	@Inject
	private PhysicalNamingStrategy physicalNamingStrategy;

	@Inject
	private HibernateConfig hibernateConfig;

	@Inject
	private Validator validator;

	@Inject
	private SessionFactoryService sessionFactoryService;

	@Inject
	private Dao dao;

	@Inject
	private UserService userService;

	@Inject
	private SettingService settingService;

	@Inject
	private PasswordService passwordService;

	@Inject
	private TaskScheduler taskScheduler;

	@Inject
	private RoleService roleService;

	@Inject
	private LinkSpecService linkSpecService;

	@Inject
	private EmailAddressService emailAddressService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private AlertService alertService;
	
	private String backupTaskId;

	private Metadata getMetadata() {
		return sessionFactoryService.getMetadata();
	}
	
	private void execute(Connection conn, List<String> sqls, boolean failOnError) {
		try (Statement stmt = conn.createStatement()) {
			for (String sql: sqls) {
				try {
					stmt.execute(sql);
				} catch (Exception e) {
					if (sql.contains(" drop ") && e.getMessage() != null)
						logger.warn(e.getMessage());
					else
						logger.error("Error executing sql: " + sql, e);
					if (failOnError) 
						throw e;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String checkDataVersion(Connection conn, boolean allowEmptyDB) {
		String dbDataVersion = readDbDataVersion(conn);
		
		if (!allowEmptyDB && dbDataVersion == null) 
			throw new ExplicitException("Database is not populated yet");
		String appDataVersion = MigrationHelper.getVersion(DataMigrator.class);
		if (dbDataVersion != null && !dbDataVersion.equals(appDataVersion)) {
			throw new ExplicitException(String.format("Data version mismatch (app data version: %s, db data version: %s)", 
					appDataVersion, dbDataVersion));
		}
		return dbDataVersion;
	}
	
	@Override
	public void populateDatabase(Connection conn) {
		if (hibernateConfig.isHSQLDialect()) 
			execute(conn, Lists.newArrayList("SET DATABASE TRANSACTION CONTROL MVCC"), true);
		
		String dbDataVersion = checkDataVersion(conn, true);
		
		if (dbDataVersion == null) {
			File tempFile = null;
        	try {
            	tempFile = FileUtils.createTempFile("schema", ".sql");
	        	new SchemaExport().setOutputFile(tempFile.getAbsolutePath())
	        			.setFormat(false).createOnly(EnumSet.of(TargetType.SCRIPT), getMetadata());
	        	List<String> sqls = new ArrayList<String>();
	        	for (String sql: FileUtils.readLines(tempFile, Charset.defaultCharset())) {
	        		if (shouldInclude(sql))
	        			sqls.add(sql);
	        	}
	        	execute(conn, sqls, true);
				
	        	try (var stmt = conn.createStatement()) {
	        		stmt.execute(String.format("insert into %s values(1, '%s')", 
	        				getTableName(ModelVersion.class), MigrationHelper.getVersion(DataMigrator.class)));
	        	}
        	} catch (Exception e) {
        		throw new RuntimeException(e);
			} finally {
        		if (tempFile != null)
        			tempFile.delete();
        	}
		} 
	}

	@Override
	public Connection openConnection() {
		return PersistenceUtils.openConnection(hibernateConfig, 
				Thread.currentThread().getContextClassLoader());
	}
	
	@Override
	public String getTableName(Class<? extends AbstractEntity> entityClass) {
		JdbcEnvironment environment = getMetadata().getDatabase()
				.getServiceRegistry().getService(JdbcEnvironment.class);
		Identifier identifier = Identifier.toIdentifier(entityClass.getSimpleName());
		return physicalNamingStrategy.toPhysicalTableName(identifier, environment).getText();
	}
	
	@Override
	public String getColumnName(String fieldName) {
		JdbcEnvironment environment = getMetadata().getDatabase()
				.getServiceRegistry().getService(JdbcEnvironment.class);
		Identifier identifier = Identifier.toIdentifier(fieldName);
		return physicalNamingStrategy.toPhysicalColumnName(identifier, environment).getText();
	}
	
	private String readDbDataVersion(Connection conn) {
		try {
			var versionTableName = getTableName(ModelVersion.class);
			if (tableExists(conn, versionTableName)) {
				try (	Statement stmt = conn.createStatement();
						 ResultSet resultset = stmt.executeQuery("select " + getColumnName(ModelVersion.PROP_VERSION_COLUMN) + " from " + versionTableName)) {
					if (!resultset.next()) {
						throw new RuntimeException("No data version found in database: this is normally caused "
								+ "by unsuccessful restore/upgrade, please clean the database and try again");
					}
					String dataVersion = resultset.getString(1);
					if (resultset.next())
						throw new RuntimeException("Illegal data version format in database");
					return dataVersion;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().contains("ORA-00942")) 
				return null;
			else
				throw ExceptionUtils.unchecked(e);
		}
	}

	private File getVersionFile(File dataDir) {
		File versionFile = new File(dataDir, ModelVersion.class.getSimpleName() + "s.xml");
		if (!versionFile.exists())
			versionFile = new File(dataDir, "VersionTables.xml");
		return versionFile;
	}
	
	@Override
	public void migrateData(File dataDir) {
		File versionFile = getVersionFile(dataDir);
		
		VersionedXmlDoc dom = VersionedXmlDoc.fromFile(versionFile);
		List<Element> elements = dom.getRootElement().elements();
		if (elements.size() != 1)
			throw new RuntimeException("Incorrect data format: illegal data version");
		Element versionElement = elements.iterator().next().element(ModelVersion.PROP_VERSION_COLUMN);		
		if (versionElement == null) {
			throw new RuntimeException("Incorrect data format: no data version");
		}
		
		if (MigrationHelper.migrate(versionElement.getText(), new DataMigrator(), dataDir)) {
			// load version file again in case we changed something of it while migrating
			versionFile = getVersionFile(dataDir);
			dom = VersionedXmlDoc.fromFile(versionFile);
			elements = dom.getRootElement().elements();
			Preconditions.checkState(elements.size() == 1);
			versionElement = Preconditions.checkNotNull(elements.iterator().next().element(ModelVersion.PROP_VERSION_COLUMN));		
			versionElement.setText(MigrationHelper.getVersion(DataMigrator.class));
			dom.writeToFile(versionFile, false);
		}		
	}

	/**
	 * Determines whether or not entityType1 has transitive foreign key
	 * dependency on entityType2.
	 * 
	 * @param entityType1
	 * @param entityType2
	 * @return
	 */
	private boolean hasForeignKeyDependency(Class<?> entityType1, Class<?> entityType2) {
		for (Field field: BeanUtils.findFields(entityType1)) {
			if (field.getAnnotation(ManyToOne.class) != null || field.getAnnotation(JoinColumn.class) != null) {
				if (field.getType() == entityType2)
					return true;
				if (field.getType() != entityType1 && 
						hasForeignKeyDependency(field.getType(), entityType2)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @return hibernate entity types ordered using foreign key dependency
	 *         information. For example, Build type comes before Project
	 *         type as Build has foreign key reference to Project
	 */
	private List<Class<?>> getEntityTypes() {
		var entityTypes = getMetadata().getEntityBindings().stream()
				.map(it->it.getMappedClass())
				.collect(Collectors.toList());
		
		/* Collections.sort does not work here */
		var sorted = new ArrayList<Class<?>>();
		while (!entityTypes.isEmpty()) {
			Class<?> dependencyLeaf = null;
			for (var entityType: entityTypes) {
				boolean hasDependents = false;
				for (var each: entityTypes) {
					if (each != entityType) {
						if (hasForeignKeyDependency(each, entityType)) {
							hasDependents = true;
							break;
						}
					}
				}
				if (!hasDependents) {
					dependencyLeaf = entityType;
					break;
				}
			}
			if (dependencyLeaf != null) {
				sorted.add(dependencyLeaf);
				entityTypes.remove(dependencyLeaf);
			} else {
				throw new RuntimeException("Looped foreigh key dependency found between model classes");
			}
		}
		return sorted;
	}
	
	@Override
	public void exportData(File exportDir) {
		exportData(exportDir, BACKUP_BATCH_SIZE);
	}

	@Sessional
	@Override
	public void exportData(File exportDir, int batchSize) {
		for (Class<?> entityType: getEntityTypes()) {
			logger.info("Exporting table '" + entityType.getSimpleName() + "'...");
			
			logger.info("Querying table ids...");
			
			Session session = dao.getSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Number> query = builder.createQuery(Number.class);
			Root<?> root = query.from(entityType);
			query.select(root.get("id")).orderBy(builder.asc(root.get("id")));
			
			List<Number> ids = session.createQuery(query).list();
			
			int count = ids.size();
			
			for (int i=0; i<count/batchSize; i++) {
				exportEntity(session, entityType, ids, i*batchSize, batchSize, batchSize, exportDir);
				// clear session to free memory
				session.clear();
			}
			
			if (count%batchSize != 0) {
				exportEntity(session, entityType, ids, count/batchSize*batchSize, count%batchSize, batchSize, exportDir);
			}
			logger.info("");
		}
	}

	private void exportEntity(Session session, Class<?> entityType, List<Number> ids, int start, int count, int batchSize, File exportDir) {
		logger.info("Loading table rows ({}->{}) from database...", String.valueOf(start+1), (start + count));
		
		Query<?> query = session.createQuery("from " + entityType.getSimpleName() + " where id>=:fromId and id<=:toId order by id");
		query.setParameter("fromId", ids.get(start));
		query.setParameter("toId", ids.get(start+count-1));
		
		logger.info("Converting table rows to XML...");
		VersionedXmlDoc dom = new VersionedXmlDoc();
		Element rootElement = dom.addElement("list");
		for (Object entity: query.list())
			rootElement.appendContent(VersionedXmlDoc.fromBean(entity));
		String fileName;

		if (start == 0)
			fileName = entityType.getSimpleName() + "s.xml";
		else
			fileName = entityType.getSimpleName() + "s.xml." + (start/batchSize + 1);
		
		logger.info("Writing resulting XML to file '" + fileName + "...");
		dom.writeToFile(new File(exportDir, fileName), true);
	}
	
	@Sessional
	@Override
	public void importData(File dataDir) {
		var entityTypes = getEntityTypes();
		Collections.reverse(entityTypes);
		entityTypes.remove(ModelVersion.class);
		entityTypes.add(0, ModelVersion.class);
		for (Class<?> entityType: entityTypes) {
			File[] dataFiles = dataDir.listFiles((dir, name) -> name.startsWith(entityType.getSimpleName() + "s.xml"));
			for (File file: dataFiles) {
				Session session = dao.getSession();
				Transaction transaction = session.beginTransaction();
				try {
					logger.info("Importing from data file '" + file.getName() + "'...");
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					
					for (Element element: dom.getRootElement().elements()) {
						element.detach();
						AbstractEntity entity = (AbstractEntity) new VersionedXmlDoc(DocumentHelper.createDocument(element)).toBean();
						session.replicate(entity, ReplicationMode.EXCEPTION);
					}
					session.flush();
					session.clear();
					transaction.commit();
				} catch (Exception e) {
					transaction.rollback();
					throw ExceptionUtils.unchecked(e);
				}
			}
		}
	}
		
	@Override
	public void applyConstraints(Connection conn) {
		File tempFile = null;
    	try {
        	tempFile = FileUtils.createTempFile("schema", ".sql");
        	new SchemaExport().setOutputFile(tempFile.getAbsolutePath())
        			.setFormat(false).createOnly(EnumSet.of(TargetType.SCRIPT), getMetadata());
        	List<String> sqls = new ArrayList<>();
        	for (String sql: FileUtils.readLines(tempFile, Charset.defaultCharset())) {
        		if (isApplyingConstraints(sql)) {
        			sqls.add(sql);
        		}
        	}
        	execute(conn, sqls, true);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	} finally {
    		if (tempFile != null)
    			tempFile.delete();
    	}
	}
	
	@Override
	public void createTables(Connection conn) {
		File tempFile = null;
    	try {
        	tempFile = FileUtils.createTempFile("schema", ".sql");
        	new SchemaExport().setOutputFile(tempFile.getAbsolutePath())
        			.setFormat(false).createOnly(EnumSet.of(TargetType.SCRIPT), getMetadata());
        	List<String> sqls = new ArrayList<>();
        	for (String sql: FileUtils.readLines(tempFile, Charset.defaultCharset())) {
        		if (shouldInclude(sql) && !isApplyingConstraints(sql))
        			sqls.add(sql);
        	}
        	execute(conn, sqls, true);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	} finally {
    		if (tempFile != null)
    			FileUtils.deleteFile(tempFile);
    	}
	}
	
	@Override
	public void dropConstraints(Connection conn) {
		File tempFile = null;
    	try {
        	tempFile = FileUtils.createTempFile("schema", ".sql");
        	new SchemaExport().setOutputFile(tempFile.getAbsolutePath())
        			.setFormat(false).drop(EnumSet.of(TargetType.SCRIPT), getMetadata());
        	List<String> sqls = new ArrayList<>();
        	for (String sql: FileUtils.readLines(tempFile, Charset.defaultCharset())) {
        		if (isDroppingConstraints(sql))
        			sqls.add(sql);
        	}
        	execute(conn, sqls, false);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	} finally {
    		if (tempFile != null)
    			tempFile.delete();
    	}
	}
	
	@Override
	public void cleanDatabase(Connection conn) {
		File tempFile = null;
    	try {
        	tempFile = FileUtils.createTempFile("schema", ".sql");
        	new SchemaExport().setOutputFile(tempFile.getAbsolutePath())
        			.setFormat(false).drop(EnumSet.of(TargetType.SCRIPT), getMetadata());
        	List<String> sqls = new ArrayList<>();
        	for (String sql: FileUtils.readLines(tempFile, Charset.defaultCharset())) {
        		sqls.add(sql);
        	}
        	execute(conn, sqls, false);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	} finally {
    		if (tempFile != null)
    			tempFile.delete();
    	}
	}
	
	private boolean isApplyingConstraints(String sql) {
		return sql.toLowerCase().contains(" foreign key ");
	}

	private boolean isDroppingConstraints(String sql) {
		return sql.toLowerCase().contains(" drop constraint ");
	}
	
	private boolean shouldInclude(String sql) {
		// some databases will create index on foreign keys automatically, so 
		// we skip creating indexes for those
		if (!sql.toLowerCase().startsWith("create index") || !sql.toLowerCase().endsWith("_id)")) 
			return true;
		else 
			return !hibernateConfig.isHSQLDialect() && !hibernateConfig.isMySQLDialect();
	}
	
	private void createRoot(NewUserBean bean) {
		User user = new User();
		user.setId(User.ROOT_ID);
		user.setName(bean.getName());
		user.setFullName(bean.getFullName());
		user.setPassword(passwordService.encryptPassword(bean.getPassword()));
		userService.replicate(user);

		EmailAddress primaryEmailAddress = null;
		for (EmailAddress emailAddress: emailAddressService.query()) { 
			if (emailAddress.getOwner().equals(user) && emailAddress.isPrimary()) {
				primaryEmailAddress = emailAddress;
				break;
			}
		}
		
		if (primaryEmailAddress == null) {
    		primaryEmailAddress = new EmailAddress();
    		primaryEmailAddress.setPrimary(true);
    		primaryEmailAddress.setGit(true);
    		primaryEmailAddress.setVerificationCode(null);
    		primaryEmailAddress.setOwner(user);
		}
		primaryEmailAddress.setValue(bean.getEmailAddress());
		if (primaryEmailAddress.isNew())
			emailAddressService.create(primaryEmailAddress);
		else
			emailAddressService.update(primaryEmailAddress);
	}	

	@Transactional
	@Override
	public List<ManualConfig> checkData() {
		List<ManualConfig> manualConfigs = new ArrayList<ManualConfig>();
		User system = userService.get(User.SYSTEM_ID);
		if (system == null) {
			system = new User();
			system.setId(User.SYSTEM_ID);
			system.setName(User.SYSTEM_NAME.toLowerCase());
			system.setFullName(User.SYSTEM_NAME);
			system.setPassword("no password");
    		userService.replicate(system);
		}
		User unknown = userService.get(User.UNKNOWN_ID);
		if (unknown == null) {
			unknown = new User();
			unknown.setId(User.UNKNOWN_ID);
			unknown.setName(User.UNKNOWN_NAME.toLowerCase());
			unknown.setFullName(User.UNKNOWN_NAME);
			unknown.setPassword("no password");
    		userService.replicate(unknown);
		}		
		
		if (userService.get(User.ROOT_ID) == null) {
			NewUserBean bean = new NewUserBean();
			bean.setName(System.getenv(ENV_INITIAL_USER));
			var passwordPath = System.getenv(ENV_INITIAL_PASSWORD_FILE);
			if (passwordPath != null) {
				try {
					bean.setPassword(FileUtils.readFileToString(new File(passwordPath), StandardCharsets.UTF_8).trim());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			var password = System.getenv(ENV_INITIAL_PASSWORD);
			if (password != null)
				bean.setPassword(password);
			bean.setEmailAddress(System.getenv(ENV_INITIAL_EMAIL));
			
			if (validator.validate(bean).isEmpty()) {
				createRoot(bean);
			} else {
				manualConfigs.add(new ManualConfig("Create Administrator Account", null, bean, Sets.newHashSet(PROP_SERVICE_ACCOUNT, PROP_NOTIFY_OWN_EVENTS)) {
	
					@Override
					public void complete() {
						createRoot((NewUserBean) getSetting());
					}
					
				});
			}
		}
		
		Setting setting = settingService.findSetting(Key.SYSTEM);
		SystemSetting systemSetting;

		String ingressUrl = OneDev.getInstance().getIngressUrl();
		
		if (setting == null || setting.getValue() == null) {
		    systemSetting = new SystemSetting();
	    	systemSetting.setSshRootUrl(System.getenv(ENV_INITIAL_SSH_ROOT_URL));
		    String serverUrl = System.getenv(ENV_INITIAL_SERVER_URL);
		    if (ingressUrl != null) {
				systemSetting.setServerUrl(ingressUrl);
				settingService.saveSystemSetting(systemSetting);
				systemSetting = null;
		    } else if (serverUrl != null) {
		    	systemSetting.setServerUrl(StringUtils.stripEnd(serverUrl, "/\\"));
				if (validator.validate(systemSetting).isEmpty()) {
					settingService.saveSystemSetting(systemSetting);
					systemSetting = null;
				}
			} else {
				systemSetting.setServerUrl(OneDev.getInstance().guessServerUrl());
			}
		} else {
			systemSetting = (SystemSetting) setting.getValue();
			if (ingressUrl != null)
				systemSetting.setServerUrl(ingressUrl);
			if (validator.validate(systemSetting).isEmpty())			
				systemSetting = null;
		}
		
		if (systemSetting != null) {
			Collection<String> excludedProps = Sets.newHashSet(PROP_SSH_ROOT_URL, PROP_DISABLE_AUTO_UPDATE_CHECK, PROP_USE_AVATAR_SERVICE, PROP_SESSION_TIMEOUT);
			if (Bootstrap.isInDocker()) {
				excludedProps.add(PROP_GIT_LOCATION);
				excludedProps.add(PROP_CURL_LOCATION);
			}
			if (ingressUrl != null)
				excludedProps.add("serverUrl");

			manualConfigs.add(new ManualConfig("Specify System Settings", null, 
					systemSetting, excludedProps) {
	
				@Override
				public void complete() {
					settingService.saveSystemSetting((SystemSetting) getSetting());
				}
				
			});
		}
		
		setting = settingService.findSetting(Key.SYSTEM_UUID);
		if (setting == null || setting.getValue() == null) 
			settingService.saveSystemUUID(UUID.randomUUID().toString());

		setting = settingService.findSetting(Key.SSH);
		if (setting == null || setting.getValue() == null) {
			SshSetting sshSetting = new SshSetting();
            sshSetting.setPemPrivateKey(SshKeyUtils.generatePEMPrivateKey());
            
            settingService.saveSshSetting(sshSetting);
        }
		
		setting = settingService.findSetting(Key.GPG);
		if (setting == null || setting.getValue() == null) {
			GpgSetting gpgSetting = new GpgSetting();
            settingService.saveGpgSetting(gpgSetting);
        }
		
		setting = settingService.findSetting(Key.SECURITY);
		if (setting == null) {
			settingService.saveSecuritySetting(new SecuritySetting());
		} 
		setting = settingService.findSetting(Key.ISSUE);
		if (setting == null) {
			LinkSpec link = new LinkSpec();
			link.setName("Sub Issues");
			link.setMultiple(true);
			link.setOpposite(new LinkSpecOpposite());
			link.getOpposite().setName("Parent Issue");
			link.setOrder(1);
			linkSpecService.create(link);
			
			link = new LinkSpec();
			link.setName("Related");
			link.setMultiple(true);
			link.setOrder(3);
			linkSpecService.create(link);
			
			settingService.saveIssueSetting(new GlobalIssueSetting());
		} 
		setting = settingService.findSetting(Key.PERFORMANCE);
		if (setting == null) {
			settingService.savePerformanceSetting(new PerformanceSetting());
		} 
		setting = settingService.findSetting(Key.AUTHENTICATOR);
		if (setting == null) {
			settingService.saveAuthenticator(null);
		}
		setting = settingService.findSetting(Key.JOB_EXECUTORS);
		if (setting == null) 
			settingService.saveJobExecutors(new ArrayList<>());
		setting = settingService.findSetting(Key.GROOVY_SCRIPTS);
		if (setting == null) {
			settingService.saveGroovyScripts(Lists.newArrayList());
		}
		setting = settingService.findSetting(Key.PULL_REQUEST);
		if (setting == null) {
			settingService.savePullRequestSetting(new GlobalPullRequestSetting());
		}
		setting = settingService.findSetting(Key.BUILD);
		if (setting == null) {
			settingService.saveBuildSetting(new GlobalBuildSetting());
		}
		setting = settingService.findSetting(Key.PACK);
		if (setting == null) {
			settingService.savePackSetting(new GlobalPackSetting());
		}
		setting = settingService.findSetting(Key.PROJECT);
		if (setting == null) {
			settingService.saveProjectSetting(new GlobalProjectSetting());
		}
		setting = settingService.findSetting(Key.SUBSCRIPTION_DATA);
		if (setting == null) {
			settingService.saveSubscriptionData(null);
		}
		setting = settingService.findSetting(Key.ALERT);
		if (setting == null) {
			settingService.saveAlertSetting(new AlertSetting());
		}
		setting = settingService.findSetting(Key.AGENT);
		if (setting == null) {
			settingService.saveAgentSetting(new AgentSetting());
		}
		setting = settingService.findSetting(Key.SERVICE_DESK_SETTING);
		if (setting == null) { 
			settingService.saveServiceDeskSetting(null);
		} else if (setting.getValue() != null && !validator.validate(setting.getValue()).isEmpty()) {
			manualConfigs.add(new ManualConfig("Specify Service Desk Setting", null, 
					setting.getValue(), new HashSet<>(), true) {
	
				@Override
				public void complete() {
					settingService.saveServiceDeskSetting((ServiceDeskSetting) getSetting());
				}
				
			});
		}
		setting = settingService.findSetting(Key.EMAIL_TEMPLATES);
		if (setting == null) {
			settingService.saveEmailTemplates(new EmailTemplates());
		} else {
			var emailTemplates = (EmailTemplates) setting.getValue();
			if (emailTemplates.getStopwatchOverdue() == null)
				emailTemplates.setStopwatchOverdue(EmailTemplates.DEFAULT_STOPWATCH_OVERDUE);
			if (emailTemplates.getPasswordReset() == null)
				emailTemplates.setPasswordReset(EmailTemplates.DEFAULT_PASSWORD_RESET);
			settingService.saveEmailTemplates(emailTemplates);
		}
		
		setting = settingService.findSetting(Key.CONTRIBUTED_SETTINGS);
		if (setting == null) 
			settingService.saveContributedSettings(new LinkedHashMap<>());
		
		setting = settingService.findSetting(Key.MAIL);
		if (setting == null) 
			settingService.saveMailConnector(null);
		
		setting = settingService.findSetting(Key.BACKUP);
		if (setting == null) {
			settingService.saveBackupSetting(null);
		} else if (setting.getValue() != null && !validator.validate(setting.getValue()).isEmpty()) {
			Serializable backupSetting = setting.getValue();
			manualConfigs.add(new ManualConfig("Specify Backup Setting", null, backupSetting) {

				@Override
				public void complete() {
					settingService.saveBackupSetting((BackupSetting) getSetting());
				}
				
			});
		}
		
		setting = settingService.findSetting(Key.BRANDING);
		if (setting == null) 
			settingService.saveBrandingSetting(new BrandingSetting());

		setting = settingService.findSetting(Key.CLUSTER_SETTING);
		if (setting == null) {
			ClusterSetting clusterSetting = new ClusterSetting();
			clusterSetting.setReplicaCount(1);
			settingService.saveClusterSetting(clusterSetting);
		}

		setting = settingService.findSetting(Key.AUDIT);
		if (setting == null) {
			AuditSetting auditSetting = new AuditSetting();
			settingService.saveAuditSetting(auditSetting);
		}
		
		if (roleService.get(Role.OWNER_ID) == null) {
			Role owner = new Role();
			owner.setName("Project Owner");
			owner.setId(Role.OWNER_ID);
			owner.setManageProject(true);
			roleService.replicate(owner);
			roleService.setupDefaults();
		}
		
		return manualConfigs;
	}

	@Override
	public void scheduleBackup(BackupSetting backupSetting) {
		if (backupTaskId != null)
			taskScheduler.unschedule(backupTaskId);
		if (backupSetting != null) { 
			backupTaskId = taskScheduler.schedule(new SchedulableTask() {

				@Override
				public void execute() {
					if (clusterService.isLeaderServer()) {
						File tempDir = FileUtils.createTempDir("backup");
						try {
							File backupDir = new File(Bootstrap.getSiteDir(), Upgrade.DB_BACKUP_DIR);
							FileUtils.createDir(backupDir);
							exportData(tempDir);
							File backupFile = new File(backupDir, 
									DateTimeFormat.forPattern(Upgrade.BACKUP_DATETIME_FORMAT).print(new DateTime()) + ".zip");
							ZipUtils.zip(tempDir, backupFile, null);
						} catch (Exception e) {
							notifyBackupError(e);
							throw ExceptionUtils.unchecked(e);
						} finally {
							FileUtils.deleteDir(tempDir);
						}
					}		
				}

				@Override
				public ScheduleBuilder<?> getScheduleBuilder() {
					return CronScheduleBuilder.cronSchedule(backupSetting.getSchedule());
				}
				
			});
		}
	}
	
	@Listen
	public void on(SystemStarted event) {
		scheduleBackup(settingService.getBackupSetting());
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Setting) {
			Setting setting = (Setting) event.getEntity();
			if (setting.getKey() == Setting.Key.BACKUP) {
				BackupSetting backupSetting = (BackupSetting) setting.getValue();
				transactionService.runAfterCommit(new ClusterRunnable() {

					private static final long serialVersionUID = 1L;

					@Override
					public void run() {
						clusterService.submitToAllServers(new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() throws Exception {
								scheduleBackup(backupSetting);
								return null;
							}
							
						});
					}
					
				});
			}
		}
	}
	
	@Sessional
	protected void notifyBackupError(Throwable e) {
		alertService.alert("Database auto-backup failed", escapeHtml5(getStackTraceAsString(e)), false);
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(DataService.class);
	}
	
}
