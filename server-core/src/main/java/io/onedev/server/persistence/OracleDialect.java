package io.onedev.server.persistence;

import org.hibernate.dialect.Oracle12cDialect;

import java.sql.Types;

public class OracleDialect extends Oracle12cDialect {
	@Override
	protected void registerCharacterTypeMappings() {
		super.registerCharacterTypeMappings();
		registerColumnType( Types.VARCHAR, "clob" );
	}
	
}
