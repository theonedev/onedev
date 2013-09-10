package com.pmease.commons.editable.typeconverter;

import java.lang.reflect.Method;

import com.pmease.commons.editable.annotation.Numeric;

public class NumericConverter implements TypeConverter {

	public boolean accept(Method getter) {
		Class<?> type = getter.getReturnType();
		return type == int.class || type == long.class || type == Integer.class || 
			type == Long.class || type == float.class || type == Float.class || 
			type == double.class || type == Double.class || getter.getAnnotation(Numeric.class) != null;
	}
	
	public Object toObject(Class<?> type, String string) {
		if (string == null) {
			if (type == int.class || type == long.class 
					|| type == float.class || type == double.class) {
				throw new ConversionException("Should not be empty.");
			} else {
				return null;
			}
		}
		
		try {
			if (type == int.class || type == Integer.class) {
				return Integer.parseInt(string);
			} else if (type == long.class || type == Long.class) {
				return Long.parseLong(string);
			} else if (type == float.class || type == Float.class) {
				return Float.parseFloat(string);
			} else if (type == double.class || type == Double.class) {
				return Double.parseDouble(string);
			} else {
				Long.parseLong(string);
				return string;
			}
		} catch (NumberFormatException e) {
			throw new ConversionException("Invalid numeric value.");
		}
	}

	public String toString(Object obj) {
		if (obj == null)
			return null;
		else
			return obj.toString();
	}
	
}
