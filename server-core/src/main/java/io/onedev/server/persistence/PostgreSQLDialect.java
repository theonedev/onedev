package io.onedev.server.persistence;

import java.sql.Types;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

/**
 * Map blob to bytea to avoid below issues:
 * <ul>
 * <li> Blobs are stored in a separate table and will not be removed automatically even 
 * if the associating row is removed
 * <li> It cannot load blobs in auto-commit mode, meaning that we cannot lazy load 
 * entities with blob fields out side of a transaction even if Hibernate session 
 * is available 
 */
public class PostgreSQLDialect extends org.hibernate.dialect.PostgreSQLDialect {

	@Override
	protected String columnType(int sqlTypeCode) {
		return sqlTypeCode == Types.BLOB ? "bytea" : super.columnType(sqlTypeCode);
	}

	@Override
	public void contributeTypes(TypeContributions contributions, ServiceRegistry registry) {
		super.contributeTypes(contributions, registry);
		// Use binary binding for nulls as well as setBytes/getBytes for values.
		// A BLOB JDBC type binds null as oid, which PostgreSQL rejects for bytea.
		contributions.getTypeConfiguration().getJdbcTypeRegistry()
				.addDescriptor(Types.BLOB, VarbinaryJdbcType.INSTANCE);
	}

}
