package com.pmease.commons.editable.typeconverter;

import java.lang.reflect.Method;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EnumConverter implements TypeConverter {

	public boolean accept(Method getter) {
		return Enum.class.isAssignableFrom(getter.getReturnType());
	}
	
	public Object toObject(Class type, String string) {
		if (string == null)
			return null;
		
		try {
			return Enum.valueOf(type, string);
		} catch (IllegalArgumentException e) {
			throw new ConversionException("Invalid value of enum '" + 
					type.getName() + "'.");
		}
	}

	public String toString(Object obj) {
		if (obj == null)
			return null;
		else
			return obj.toString();
	}
	
}
