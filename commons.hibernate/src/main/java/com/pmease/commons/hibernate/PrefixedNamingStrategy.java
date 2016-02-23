/*
 * Copyright PMEase (c) 2005-2008,
 * Date: Feb 24, 2008
 * Time: 4:29:05 PM
 * All rights reserved.
 * 
 * Revision: $Id: PersistenceNamingStrategy.java 1209 2008-07-28 00:16:18Z robin $
 */
package com.pmease.commons.hibernate;

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
		return super.toPhysicalSequenceName(name, context);
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
		return super.toPhysicalSequenceName(name, context);
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
		return super.toPhysicalColumnName(name, context);
	}
	
}
