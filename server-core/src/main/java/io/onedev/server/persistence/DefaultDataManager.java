package io.onedev.server.persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Driver;
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
import java.util.Properties;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
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
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.commandhandler.Upgrade;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.mail.MailManager;
import io.onedev.server.migration.DataMigrator;
import io.onedev.server.migration.MigrationHelper;
import io.onedev.server.migration.VersionedXmlDoc;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.ModelVersion;
import io.onedev.server.model.Role;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.AgentSetting;
import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.model.support.administration.BrandingSetting;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.GlobalProjectSetting;
import io.onedev.server.model.support.administration.GlobalPullRequestSetting;
import io.onedev.server.model.support.administration.GpgSetting;
import io.onedev.server.model.support.administration.PerformanceSetting;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.administration.ServiceDeskSetting;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.administration.mailsetting.MailSetting;
import io.onedev.server.model.support.administration.notificationtemplate.NotificationTemplateSetting;
import io.onedev.server.model.support.issue.LinkSpecOpposite;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;
import io.onedev.server.web.util.editablebean.NewUserBean;

@Singleton
public class DefaultDataManager implements DataManager, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultDataManager.class);
	
	private static final String ENV_INITIAL_USER = "initial_user";
	
	private static final String ENV_INITIAL_PASSWORD = "initial_password";
	
	private static final String ENV_INITIAL_EMAIL = "initial_email";
	
	private static final String ENV_INITIAL_SERVER_URL = "initial_server_url";
	
	private static final String ENV_INITIAL_SSH_ROOT_URL = "initial_ssh_root_url";
	
	private static final int BACKUP_BATCH_SIZE = 1000;
	
	private final PhysicalNamingStrategy physicalNamingStrategy;

	private final HibernateConfig hibernateConfig;
	
	private final Validator validator;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final Dao dao;
	
	private final UserManager userManager;
	
	private final SettingManager settingManager;
	
	private final MailManager mailManager;
	
	private final PasswordService passwordService;
	
	private final TaskScheduler taskScheduler;
	
	private final RoleManager roleManager;
	
	private final LinkSpecManager linkSpecManager;
	
	private final EmailAddressManager emailAddressManager;
	
	private final ClusterManager clusterManager;
	
	private final TransactionManager transactionManager;
	
	private String backupTaskId;

	@Inject
	public DefaultDataManager(PhysicalNamingStrategy physicalNamingStrategy, HibernateConfig hibernateConfig, 
			Validator validator, SessionManager sessionManager, Dao dao, SessionFactoryManager sessionFactoryManager,
			SettingManager settingManager, MailManager mailManager, TaskScheduler taskScheduler, 
			PasswordService passwordService, RoleManager roleManager, LinkSpecManager linkSpecManager, 
			EmailAddressManager emailAddressManager, UserManager userManager, ClusterManager clusterManager, 
			TransactionManager transactionManager) {
		this.physicalNamingStrategy = physicalNamingStrategy;
		this.hibernateConfig = hibernateConfig;
		this.validator = validator;
		this.sessionFactoryManager = sessionFactoryManager;
		this.dao = dao;
		
		this.userManager = userManager;
		this.settingManager = settingManager;
		this.taskScheduler = taskScheduler;
		this.mailManager = mailManager;
		this.passwordService = passwordService;
		this.roleManager = roleManager;
		this.linkSpecManager = linkSpecManager;
		this.emailAddressManager = emailAddressManager;
		this.clusterManager = clusterManager;
		this.transactionManager = transactionManager;
	}
	
	@Override
	public <T> T callWithConnection(ConnectionCallable<T> callable, boolean autoCommit) {
		try (Connection conn = openConnection()) {
			if (autoCommit) {
				conn.setAutoCommit(true);
				return callable.call(conn);
			} else {
				conn.setAutoCommit(false);
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				try {
					T result = callable.call(conn);
					conn.commit();
					return result;
				} catch (Exception e) {
					conn.rollback();
					throw ExceptionUtils.unchecked(e);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public <T> T callWithConnection(ConnectionCallable<T> callable) {
		return callWithConnection(callable, false);
	}
	
	private Metadata getMetadata() {
		return sessionFactoryManager.getMetadata();
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
		
		if (!allowEmptyDB && dbDataVersion == null) {
			logger.error("Database is not populated yet");
			System.exit(1);
		}
		String appDataVersion = MigrationHelper.getVersion(DataMigrator.class);
		if (dbDataVersion != null && !dbDataVersion.equals(appDataVersion)) {
			logger.error("Data version mismatch (app data version: {}, db data version: {})", appDataVersion, dbDataVersion);
			System.exit(1);
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
            	tempFile = File.createTempFile("schema", ".sql");
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

	private Connection openConnection() {
		try {
			Driver driver = (Driver) Class.forName(hibernateConfig.getDriver(), true, 
					Thread.currentThread().getContextClassLoader()).getDeclaredConstructor().newInstance();
			Properties connectProps = new Properties();
			String user = hibernateConfig.getUser();
			String password = hibernateConfig.getPassword();
	        if (user != null) 
	            connectProps.put("user", user);
	        if (password != null) 
	            connectProps.put("password", password);
			
			return driver.connect(hibernateConfig.getUrl(), connectProps);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
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
			String versionTableName = getTableName(ModelVersion.class);
			boolean versionTableExists = false;
			try (ResultSet resultset = conn.getMetaData().getTables(null, null, versionTableName.toUpperCase(), null)) {
				if (resultset.next())
					versionTableExists = true;
			}
			if (!versionTableExists) {
				try (ResultSet resultset = conn.getMetaData().getTables(null, null, versionTableName.toLowerCase(), null)) {
					if (resultset.next()) {
						versionTableExists = true;
					}
				}
			}
			if (!versionTableExists) {
				try (ResultSet resultset = conn.getMetaData().getTables(null, null, versionTableName, null)) {
					if (resultset.next()) {
						versionTableExists = true;
					}
				}
			}
			if (!versionTableExists) {
				return null;
			} else {
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
		for (Class<?> entityType: entityTypes) {
			File[] dataFiles = dataDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(entityType.getSimpleName() + "s.xml");
				}
				
			});
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
	public void validateData(File dataDir) {
		var entityTypes = getEntityTypes();
		Collections.reverse(entityTypes);
		for (Class<?> entityType: entityTypes) {
			File[] dataFiles = dataDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(entityType.getSimpleName() + "s.xml");
				}
				
			});
			for (File file: dataFiles) {
				try {
					logger.info("Validating data file '" + file.getName() + "'...");
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					
					for (Element element: dom.getRootElement().elements()) {
						element.detach();
						AbstractEntity entity = (AbstractEntity) new VersionedXmlDoc(DocumentHelper.createDocument(element)).toBean();
						if (entity.getId() > 0) {
							for (ConstraintViolation<?> violation: validator.validate(entity)) 
								reportError(entity, violation);
						}
					}
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				}
			}
		}	
	}
	
	private void reportError(AbstractEntity entity, ConstraintViolation<?> violation) {
		String errorInfo = String.format("Error validating entity (entity class: %s, entity id: %d, entity property: %s, error message: %s)", 
				entity.getClass(), entity.getId(), violation.getPropertyPath().toString(), violation.getMessage());
		throw new RuntimeException(errorInfo);
	}
	
	@Override
	public void applyConstraints(Connection conn) {
		File tempFile = null;
    	try {
        	tempFile = File.createTempFile("schema", ".sql");
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
        	tempFile = File.createTempFile("schema", ".sql");
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
        	tempFile = File.createTempFile("schema", ".sql");
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
        	tempFile = File.createTempFile("schema", ".sql");
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
		userManager.replicate(user);

		EmailAddress primaryEmailAddress = null;
		for (EmailAddress emailAddress: emailAddressManager.query()) { 
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
		emailAddressManager.save(primaryEmailAddress);
	}	

	@SuppressWarnings({"serial"})
	@Transactional
	@Override
	public List<ManualConfig> checkData() {
		List<ManualConfig> manualConfigs = new ArrayList<ManualConfig>();
		User system = userManager.get(User.SYSTEM_ID);
		if (system == null) {
			system = new User();
			system.setId(User.SYSTEM_ID);
			system.setName(User.SYSTEM_NAME.toLowerCase());
			system.setFullName(User.SYSTEM_NAME);
			system.setPassword("no password");
    		userManager.replicate(system);
		}
		User unknown = userManager.get(User.UNKNOWN_ID);
		if (unknown == null) {
			unknown = new User();
			unknown.setId(User.UNKNOWN_ID);
			unknown.setName(User.UNKNOWN_NAME.toLowerCase());
			unknown.setFullName(User.UNKNOWN_NAME);
			unknown.setPassword("no password");
    		userManager.replicate(unknown);
		}		
		
		if (userManager.get(User.ROOT_ID) == null) {
			NewUserBean bean = new NewUserBean();
			bean.setName(System.getenv(ENV_INITIAL_USER));
			bean.setPassword(System.getenv(ENV_INITIAL_PASSWORD));
			bean.setEmailAddress(System.getenv(ENV_INITIAL_EMAIL));
			
			if (validator.validate(bean).isEmpty()) {
				createRoot(bean);
			} else {
				manualConfigs.add(new ManualConfig("Create Administrator Account", null, bean) {
	
					@Override
					public void complete() {
						createRoot((NewUserBean) getSetting());
					}
					
				});
			}
		}
		
		Setting setting = settingManager.findSetting(Key.SYSTEM);
		SystemSetting systemSetting = null;

		String ingressUrl = OneDev.getInstance().getIngressUrl();
		
		if (setting == null || setting.getValue() == null) {
		    systemSetting = new SystemSetting();
	    	systemSetting.setSshRootUrl(System.getenv(ENV_INITIAL_SSH_ROOT_URL));
		    String serverUrl = System.getenv(ENV_INITIAL_SERVER_URL);
		    if (ingressUrl != null) {
				systemSetting.setServerUrl(ingressUrl);
				settingManager.saveSystemSetting(systemSetting);
				systemSetting = null;
		    } else if (serverUrl != null) {
		    	systemSetting.setServerUrl(StringUtils.stripEnd(serverUrl, "/\\"));
		    	settingManager.saveSystemSetting(systemSetting);
		    	systemSetting = null;
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
			Collection<String> excludedProps = Sets.newHashSet("sshRootUrl", "gravatarEnabled");
			if (Bootstrap.isInDocker()) {
				excludedProps.add("gitConfig");
				excludedProps.add("curlConfig");
			}
			if (ingressUrl != null)
				excludedProps.add("serverUrl");

			manualConfigs.add(new ManualConfig("Specify System Setting", null, 
					systemSetting, excludedProps) {
	
				@Override
				public void complete() {
					settingManager.saveSystemSetting((SystemSetting) getSetting());
				}
				
			});
		}

		setting = settingManager.findSetting(Key.SSH);
		if (setting == null || setting.getValue() == null) {
			SshSetting sshSetting = new SshSetting();
            sshSetting.setPemPrivateKey(SshKeyUtils.generatePEMPrivateKey());
            
            settingManager.saveSshSetting(sshSetting);
        }
		
		setting = settingManager.findSetting(Key.GPG);
		if (setting == null || setting.getValue() == null) {
			GpgSetting gpgSetting = new GpgSetting();
            settingManager.saveGpgSetting(gpgSetting);
        }
		
		setting = settingManager.findSetting(Key.SECURITY);
		if (setting == null) {
			settingManager.saveSecuritySetting(new SecuritySetting());
		} 
		setting = settingManager.findSetting(Key.ISSUE);
		if (setting == null) {
			LinkSpec link = new LinkSpec();
			link.setName("Child Issue");
			link.setMultiple(true);
			link.setOpposite(new LinkSpecOpposite());
			link.getOpposite().setName("Parent Issue");
			link.setOrder(1);
			linkSpecManager.save(link);
			
			link = new LinkSpec();
			link.setName("Blocked By");
			link.setMultiple(true);
			link.setOpposite(new LinkSpecOpposite());
			link.getOpposite().setName("Blocking");
			link.getOpposite().setMultiple(true);
			link.setOrder(2);
			linkSpecManager.save(link);
			
			settingManager.saveIssueSetting(new GlobalIssueSetting());
		} 
		setting = settingManager.findSetting(Key.PERFORMANCE);
		if (setting == null) {
			settingManager.savePerformanceSetting(new PerformanceSetting());
		} 
		setting = settingManager.findSetting(Key.AUTHENTICATOR);
		if (setting == null) {
			settingManager.saveAuthenticator(null);
		}
		setting = settingManager.findSetting(Key.JOB_EXECUTORS);
		if (setting == null) 
			settingManager.saveJobExecutors(new ArrayList<>());
		setting = settingManager.findSetting(Key.SSO_CONNECTORS);
		if (setting == null) {
			settingManager.saveSsoConnectors(Lists.newArrayList());
		}
		setting = settingManager.findSetting(Key.GROOVY_SCRIPTS);
		if (setting == null) {
			settingManager.saveGroovyScripts(Lists.newArrayList());
		}
		setting = settingManager.findSetting(Key.PULL_REQUEST);
		if (setting == null) {
			settingManager.savePullRequestSetting(new GlobalPullRequestSetting());
		}
		setting = settingManager.findSetting(Key.BUILD);
		if (setting == null) {
			settingManager.saveBuildSetting(new GlobalBuildSetting());
		}
		setting = settingManager.findSetting(Key.PROJECT);
		if (setting == null) {
			settingManager.saveProjectSetting(new GlobalProjectSetting());
		}
		setting = settingManager.findSetting(Key.AGENT);
		if (setting == null) {
			settingManager.saveAgentSetting(new AgentSetting());
		}
		setting = settingManager.findSetting(Key.SERVICE_DESK_SETTING);
		if (setting == null) { 
			settingManager.saveServiceDeskSetting(null);
		} else if (setting.getValue() != null && !validator.validate(setting.getValue()).isEmpty()) {
			manualConfigs.add(new ManualConfig("Specify Service Desk Setting", null, 
					setting.getValue(), new HashSet<>(), true) {
	
				@Override
				public void complete() {
					settingManager.saveServiceDeskSetting((ServiceDeskSetting) getSetting());
				}
				
			});
		}
		setting = settingManager.findSetting(Key.NOTIFICATION_TEMPLATE_SETTING);
		if (setting == null) {
			settingManager.saveNotificationTemplateSetting(new NotificationTemplateSetting());
		}
		
		setting = settingManager.findSetting(Key.CONTRIBUTED_SETTINGS);
		if (setting == null) 
			settingManager.saveContributedSettings(new LinkedHashMap<>());
		
		setting = settingManager.findSetting(Key.MAIL);
		if (setting == null) {
			settingManager.saveMailSetting(null);
		} else if (setting.getValue() != null && !validator.validate(setting.getValue()).isEmpty()) {
			manualConfigs.add(new ManualConfig("Specify Mail Setting", null, setting.getValue()) {

				@Override
				public void complete() {
					settingManager.saveMailSetting((MailSetting) getSetting());
				}
				
			});
		}
		
		setting = settingManager.findSetting(Key.BACKUP);
		if (setting == null) {
			settingManager.saveBackupSetting(null);
		} else if (setting.getValue() != null && !validator.validate(setting.getValue()).isEmpty()) {
			Serializable backupSetting = setting.getValue();
			manualConfigs.add(new ManualConfig("Specify Backup Setting", null, backupSetting) {

				@Override
				public void complete() {
					settingManager.saveBackupSetting((BackupSetting) getSetting());
				}
				
			});
		}
		
		setting = settingManager.findSetting(Key.BRANDING);
		if (setting == null) 
			settingManager.saveBrandingSetting(new BrandingSetting());
		
		if (roleManager.get(Role.OWNER_ID) == null) {
			Role owner = new Role();
			owner.setName("Project Owner");
			owner.setId(Role.OWNER_ID);
			owner.setManageProject(true);
			roleManager.replicate(owner);
			roleManager.setupDefaults();
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
					if (clusterManager.isLeaderServer()) {
						File tempDir = FileUtils.createTempDir("backup");
						try {
							File backupDir = new File(Bootstrap.getSiteDir(), Upgrade.DB_BACKUP_DIR);
							FileUtils.createDir(backupDir);
							exportData(tempDir);
							File backupFile = new File(backupDir, 
									DateTimeFormat.forPattern(Upgrade.BACKUP_DATETIME_FORMAT).print(new DateTime()) + ".zip");
							FileUtils.zip(tempDir, backupFile, null);
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
		scheduleBackup(settingManager.getBackupSetting());
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Setting) {
			Setting setting = (Setting) event.getEntity();
			if (setting.getKey() == Setting.Key.BACKUP) {
				BackupSetting backupSetting = (BackupSetting) setting.getValue();
				transactionManager.runAfterCommit(new ClusterRunnable() {

					private static final long serialVersionUID = 1L;

					@Override
					public void run() {
						clusterManager.submitToAllServers(new ClusterTask<Void>() {

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
		User root = userManager.getRoot();
		String url = settingManager.getSystemSetting().getServerUrl();
		String htmlBody = String.format(""
				+ "OneDev url: <a href='%s'>%s</a>"
				+ "<p style='margin: 16px 0;'>"
				+ "<b>Error detail:</b>"
				+ "<pre style='font-family: monospace;'>%s</pre>"
				+ "<p style='margin: 16px 0;'>"
				+ "-- Sent by OneDev", 
				url, url, Throwables.getStackTraceAsString(e));
		String textBody = String.format(""
				+ "OneDev url: %s\n\n"
				+ "Error detail:\n"
				+ "%s",
				url, Throwables.getStackTraceAsString(e));
		
		EmailAddress emailAddress = root.getPrimaryEmailAddress();
		if (emailAddress != null && emailAddress.isVerified()) {
			mailManager.sendMail(Lists.newArrayList(emailAddress.getValue()), Lists.newArrayList(),
					Lists.newArrayList(), "[Backup] OneDev Database Auto-backup Failed", 
					htmlBody, textBody, null, null);
		}
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(DataManager.class);
	}
	
}
