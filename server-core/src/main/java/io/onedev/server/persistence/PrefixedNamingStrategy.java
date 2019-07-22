package io.onedev.server.persistence;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class PrefixedNamingStrategy extends PhysicalNamingStrategyStandardImpl {
	
	private static final long serialVersionUID = 1L;
	
	private String prefix;

	public PrefixedNamingStrategy(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
		return new Identifier(prefix+name.getText(), name.isQuoted());
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
		return new Identifier(prefix+name.getText(), name.isQuoted());
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
		return new Identifier(prefix+name.getText(), name.isQuoted());
	}
	
}
