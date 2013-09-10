package com.pmease.commons.editable;

import java.lang.reflect.Method;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.editable.typeconverter.TypeConverter;

@Singleton
public class DefaultTypeConverterRegistry implements TypeConverterRegistry {

	private Set<TypeConverter> typeConverters;
	
	@Inject
	public DefaultTypeConverterRegistry(Set<TypeConverter> typeConverters) {
		this.typeConverters = typeConverters;
	}
	
	@Override
	public TypeConverter getTypeConverter(Method getter) {
		for (TypeConverter each: typeConverters) {
			if (each.accept(getter))
				return each;
		}
		return null;
	}

}
