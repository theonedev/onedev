/*
 * Copyright PMEase (c) 2005-2008,
 * Date: Feb 24, 2008
 * Time: 4:29:05 PM
 * All rights reserved.
 * 
 * Revision: $Id: PersistenceNamingStrategy.java 1209 2008-07-28 00:16:18Z robin $
 */
package com.pmease.commons.persistence;

import org.hibernate.AssertionFailure;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.internal.util.StringHelper;

public class PrefixedNamingStrategy extends ImprovedNamingStrategy {
	private static final long serialVersionUID = 1L;
	
	private String prefix;

	public PrefixedNamingStrategy(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public String classToTableName(String className) {
		return prefix + "_" + addUnderscores(
				StringHelper.unqualify(className)).toUpperCase();
	}
	
	@Override
	public String propertyToColumnName(String propertyName) {
		return prefix + "_" + addUnderscores(
				StringHelper.unqualify(propertyName)).toUpperCase();
	}

	@Override
	public String logicalCollectionColumnName(String columnName,
			String propertyName, String referencedColumn) {
		if ((propertyToColumnName(propertyName) + "_" + 
				referencedColumn.toUpperCase())
				.equalsIgnoreCase(columnName)) {
			return StringHelper.unqualify(propertyName);
		} else {
			return StringHelper.isNotEmpty( columnName ) ?
					columnName :
					StringHelper.unqualify( propertyName );
		}
	}

	@Override
	public String foreignKeyColumnName(String propertyName,
			String propertyEntityName, String propertyTableName,
			String referencedColumnName) {
		String header = propertyName != null ? 
				StringHelper.unqualify( propertyName ) : propertyTableName;
		if (header == null) 
			throw new AssertionFailure("NamingStrategy not properly filled");
		return prefix + "_" + columnName(header).toUpperCase() + 
				"_" + referencedColumnName.toUpperCase();
	}
}
