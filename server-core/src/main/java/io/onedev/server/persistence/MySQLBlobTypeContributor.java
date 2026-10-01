package io.onedev.server.persistence;

import java.sql.Types;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl;

public class MySQLBlobTypeContributor implements TypeContributor {

	@Override
	public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
		var dialect = serviceRegistry.requireService(JdbcEnvironment.class).getDialect();
		// MariaDBDialect also extends MySQLDialect. Hibernate 5 always used longblob
		// for binary LOBs here; honoring @Column.length would shrink existing columns
		// when an upgrade recreates the schema, preventing large values from restoring.
		if (dialect instanceof MySQLDialect) {
			typeContributions.getTypeConfiguration().getDdlTypeRegistry()
					.addDescriptor(new DdlTypeImpl(Types.BLOB, "longblob", "binary", dialect));
		}
	}

}
