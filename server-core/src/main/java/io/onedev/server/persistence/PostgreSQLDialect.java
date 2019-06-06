package io.onedev.server.persistence;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * Map blob to bytea to avoid below issues:
 * <ul>
 * <li> Blobs are stored in a separate table and will not be removed automatically even 
 * if the associating row is removed
 * <li> It can not load blobs in auto-commit mode, meaning that we can not lazy load 
 * entities with blob fields out side of a transaction even if Hibernate session 
 * is available 
 */
public class PostgreSQLDialect extends PostgreSQL82Dialect {

	public PostgreSQLDialect() {
		super();
		registerColumnType(Types.BLOB, "bytea");
	}

	@Override
	public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
		if (sqlTypeDescriptor.getSqlType() == java.sql.Types.BLOB) {
			return BinaryTypeDescriptor.INSTANCE;
		}
		return super.remapSqlTypeDescriptor(sqlTypeDescriptor);
	}

}
