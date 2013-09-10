package com.pmease.commons.editable.typeconverter;

import java.lang.reflect.Method;

public class BooleanConverter implements TypeConverter {

	public boolean accept(Method getter) {
		return getter.getReturnType() == boolean.class || 
				getter.getReturnType() == Boolean.class;
	}
	
	public Object toObject(Class<?> type, String string) {
		if (string == null)
			return null;
		
		if ("yes".equalsIgnoreCase(string) || "y".equalsIgnoreCase(string)
				|| "true".equalsIgnoreCase(string) || "t".equalsIgnoreCase(string))
			return Boolean.TRUE;

		if ("no".equalsIgnoreCase(string) || "n".equalsIgnoreCase(string)
				|| "false".equalsIgnoreCase(string) || "f".equalsIgnoreCase(string))
			return Boolean.FALSE;
		
		throw new ConversionException("Invalid boolean value: " + string);
	}

	public String toString(Object obj) {
		if (obj == null)
			return null;
		
		if ((Boolean)obj)
			return "yes";
		else
			return "no";
	}
	
}
