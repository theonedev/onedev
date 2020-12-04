package io.onedev.server.persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.ManyToOne;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Interceptor;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.query.Query;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.migration.DataMigrator;
import io.onedev.server.migration.MigrationHelper;
import io.onedev.server.migration.VersionedXmlDoc;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.ModelVersion;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.validation.EntityValidator;

@Singleton
public class DefaultPersistManager implements PersistManager {

	private static final int BACKUP_BATCH_SIZE = 1000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultPersistManager.class);
	
	protected final PhysicalNamingStrategy physicalNamingStrategy;

	protected final HibernateProperties properties;
	
	protected final Interceptor interceptor;
	
	protected final IdManager idManager;
	
	protected final Dao dao;
	
	protected final EntityValidator validator;
	
	protected final StandardServiceRegistry serviceRegistry;
	
	protected final TransactionManager transactionManager;
	
	protected volatile SessionFactory sessionFactory;
	
	@Inject
	public DefaultPersistManager(PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator, 
			TransactionManager transactionManager) {
		this.physicalNamingStrategy = physicalNamingStrategy;
		this.properties = properties;
		this.interceptor = interceptor;
		this.idManager = idManager;
		this.dao = dao;
		this.validator = validator;
		this.transactionManager = transactionManager;
		serviceRegistry = new StandardServiceRegistryBuilder().applySettings(properties).build();
	}
	
	protected String getDialect() {
		return properties.getDialect();
	}
	
	protected void execute(List<String> sqls, boolean failOnError) {
		try (	Connection conn = getConnection();
				Statement stmt = conn.createStatement();) {
			for (String sql: sqls) {
				try {
					stmt.execute(sql);
				} catch (Exception e) {
					logger.error("Error executing sql: " + sql, e);
					if (failOnError) 
						throw e;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Metadata buildMetadata() {
		MetadataSources metadataSources = new MetadataSources(serviceRegistry);
		for (Class<? extends AbstractEntity> each: ClassUtils.findImplementations(AbstractEntity.class, AbstractEntity.class)) {
			metadataSources.addAnnotatedClass(each);
		}
		
		MetadataBuilder builder = metadataSources.getMetadataBuilder();
		builder.applyPhysicalNamingStrategy(physicalNamingStrategy);
		return builder.build();
	}
	
	protected SessionFactory buildSessionFactory(Metadata metadata) {
    	return metadata.getSessionFactoryBuilder().applyInterceptor(interceptor).build();
	}
	
	protected String checkDataVersion(boolean allowEmptyDB) {
		String dbDataVersion = readDbDataVersion();
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
	public void start() {
		String dialect = getDialect().toLowerCase();
		if (dialect.contains("hsql")) 
			execute(Lists.newArrayList("SET DATABASE TRANSACTION CONTROL MVCC"), true);
		
		String dbDataVersion = checkDataVersion(true);
		
		Metadata metadata = buildMetadata();
		
		if (dbDataVersion == null) {
			File tempFile = null;
        	try {
            	tempFile = File.createTempFile("schema", ".sql");
	        	new SchemaExport().setOutputFile(tempFile.getAbsolutePath())
	        			.setFormat(false).createOnly(EnumSet.of(TargetType.SCRIPT), metadata);
	        	List<String> sqls = new ArrayList<String>();
	        	for (String sql: FileUtils.readLines(tempFile, Charset.defaultCharset())) {
	        		if (shouldInclude(sql))
	        			sqls.add(sql);
	        	}
	        	execute(sqls, true);
        	} catch (IOException e) {
        		throw new RuntimeException(e);
        	} finally {
        		if (tempFile != null)
        			tempFile.delete();
        	}
        	
        	sessionFactory = buildSessionFactory(metadata);

    		idManager.init();
			
			transactionManager.run(new Runnable() {

				@Override
				public void run() {
					ModelVersion dataVersion = new ModelVersion();
					dataVersion.versionColumn = MigrationHelper.getVersion(DataMigrator.class);
					transactionManager.getSession().save(dataVersion);
				}
				
			});
		} else {
			sessionFactory = metadata.getSessionFactoryBuilder().applyInterceptor(interceptor).build();
    		idManager.init();
		}
	}

	@Override
	public void stop() {
		if (sessionFactory != null) {
			sessionFactory.close();
			sessionFactory = null;
		}
	}

	protected Connection getConnection() {
		try {
			Driver driver = (Driver) Class.forName(properties.getDriver(), true, 
					Thread.currentThread().getContextClassLoader()).newInstance();
			Properties connectProps = new Properties();
			String user = properties.getUser();
			String password = properties.getPassword();
	        if (user != null) 
	            connectProps.put("user", user);
	        if (password != null) 
	            connectProps.put("password", password);
			
			return driver.connect(properties.getUrl(), connectProps);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	private String getVersionTableName() {
		JdbcEnvironment environment = serviceRegistry.getService(JdbcEnvironment.class);
		Identifier identifier = Identifier.toIdentifier(ModelVersion.class.getSimpleName());
		return physicalNamingStrategy.toPhysicalTableName(identifier, environment).getText();
	}
	
	private String getVersionFieldName() {
		return ModelVersion.class.getFields()[0].getName();
	}
	
	private String getVersionColumnName() {
		JdbcEnvironment environment = serviceRegistry.getService(JdbcEnvironment.class);
		Identifier identifier = Identifier.toIdentifier(getVersionFieldName());
		return physicalNamingStrategy.toPhysicalColumnName(identifier, environment).getText();
	}
	
	protected String readDbDataVersion() {
		try (Connection conn = getConnection()) {
			conn.setAutoCommit(false);
			String versionTableName = getVersionTableName();
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
						ResultSet resultset = stmt.executeQuery("select " + getVersionColumnName() + " from " + versionTableName)) {
					if (!resultset.next())
						throw new RuntimeException("No data version found in database: this is normally caused "
								+ "by unsuccessful restore/upgrade, please clean the database and try again");
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
	
	protected void migrateData(File dataDir) {
		File versionFile = getVersionFile(dataDir);
		
		VersionedXmlDoc dom = VersionedXmlDoc.fromFile(versionFile);
		List<Element> elements = dom.getRootElement().elements();
		if (elements.size() != 1)
			throw new RuntimeException("Incorrect data format: illegal data version");
		Element versionElement = elements.iterator().next().element(getVersionFieldName());		
		if (versionElement == null) {
			throw new RuntimeException("Incorrect data format: no data version");
		}
		
		if (MigrationHelper.migrate(versionElement.getText(), new DataMigrator(), dataDir)) {
			// load version file again in case we changed something of it while migrating
			versionFile = getVersionFile(dataDir);
			dom = VersionedXmlDoc.fromFile(versionFile);
			elements = dom.getRootElement().elements();
			Preconditions.checkState(elements.size() == 1);
			versionElement = Preconditions.checkNotNull(elements.iterator().next().element(getVersionFieldName()));		
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
			if (field.getAnnotation(ManyToOne.class) != null) {
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
	private List<Class<?>> getEntityTypes(SessionFactory sessionFactory) {
		List<Class<?>> entityTypes = new ArrayList<>();
		for (EntityType<?> entityType: ((EntityManagerFactory)sessionFactory).getMetamodel().getEntities()) {
			entityTypes.add(entityType.getJavaType());
		}
		
		/* Collections.sort does not work here */
		List<Class<?>> sorted = new ArrayList<Class<?>>();
		while (!entityTypes.isEmpty()) {
			Class<?> dependencyLeaf = null;
			for (Class<?> entityType: entityTypes) {
				boolean hasDependents = false;
				for (Class<?> each: entityTypes) {
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
		Session session = sessionFactory.openSession();
		for (Class<?> entityType: getEntityTypes(sessionFactory)) {
			logger.info("Exporting table '" + entityType.getSimpleName() + "'...");
			
			logger.info("Querying table ids...");
			
			CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
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
		
		Query<?> query = session.createQuery("from " + entityType.getSimpleName() + " where id>=:fromId and id<=:toId");
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

	/*
	 * We do not use @Transactional annotation and will manage the session and transaction manually 
	 * in this method to reduce memory usage if importing a large database.
	 */
	@Sessional
	@Override
	public void importData(Metadata metadata, File dataDir) {
		Session session = dao.getSession();
		List<Class<?>> entityTypes = getEntityTypes(sessionFactory);
		Collections.reverse(entityTypes);
		for (Class<?> entityType: entityTypes) {
			File[] dataFiles = dataDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(entityType.getSimpleName() + "s.xml");
				}
				
			});
			for (File file: dataFiles) {
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
	
	protected void validateData(Metadata metadata, File dataDir) {
		List<Class<?>> entityTypes = getEntityTypes(sessionFactory);
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
						validator.validate(entity);
					}
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				}
			}
		}	
	}
	
	protected void applyConstraints(Metadata metadata) {
		File tempFile = null;
    	try {
        	tempFile = File.createTempFile("schema", ".sql");
        	new SchemaExport().setOutputFile(tempFile.getAbsolutePath())
        			.setFormat(false).createOnly(EnumSet.of(TargetType.SCRIPT), metadata);
        	List<String> sqls = new ArrayList<>();
        	for (String sql: FileUtils.readLines(tempFile, Charset.defaultCharset())) {
        		if (isApplyingConstraints(sql)) {
        			sqls.add(sql);
        		}
        	}
        	execute(sqls, true);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	} finally {
    		if (tempFile != null)
    			tempFile.delete();
    	}
	}
	
	protected void createTables(Metadata metadata) {
		File tempFile = null;
    	try {
        	tempFile = File.createTempFile("schema", ".sql");
        	new SchemaExport().setOutputFile(tempFile.getAbsolutePath())
        			.setFormat(false).createOnly(EnumSet.of(TargetType.SCRIPT), metadata);
        	List<String> sqls = new ArrayList<>();
        	for (String sql: FileUtils.readLines(tempFile, Charset.defaultCharset())) {
        		if (shouldInclude(sql) && !isApplyingConstraints(sql))
        			sqls.add(sql);
        	}
        	execute(sqls, true);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	} finally {
    		if (tempFile != null)
    			FileUtils.deleteFile(tempFile);
    	}
	}
	
	protected void dropConstraints(Metadata metadata) {
		File tempFile = null;
    	try {
        	tempFile = File.createTempFile("schema", ".sql");
        	new SchemaExport().setOutputFile(tempFile.getAbsolutePath())
        			.setFormat(false).drop(EnumSet.of(TargetType.SCRIPT), metadata);
        	List<String> sqls = new ArrayList<>();
        	for (String sql: FileUtils.readLines(tempFile, Charset.defaultCharset())) {
        		if (isDroppingConstraints(sql))
        			sqls.add(sql);
        	}
        	execute(sqls, false);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	} finally {
    		if (tempFile != null)
    			tempFile.delete();
    	}
	}
	
	protected void cleanDatabase(Metadata metadata) {
		File tempFile = null;
    	try {
        	tempFile = File.createTempFile("schema", ".sql");
        	new SchemaExport().setOutputFile(tempFile.getAbsolutePath())
        			.setFormat(false).drop(EnumSet.of(TargetType.SCRIPT), metadata);
        	List<String> sqls = new ArrayList<>();
        	for (String sql: FileUtils.readLines(tempFile, Charset.defaultCharset())) {
        		sqls.add(sql);
        	}
        	execute(sqls, false);
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
		if (!sql.toLowerCase().startsWith("create index") || !sql.toLowerCase().endsWith("_id)")) {
			return true;
		} else {
			String dialect = getDialect().toLowerCase();
			return !dialect.contains("mysql") && !dialect.contains("hsql");
		}
	}

	@Override
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
}
