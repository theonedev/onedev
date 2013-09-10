package com.pmease.commons.editable.typeconverter;

import java.lang.reflect.Method;

import com.pmease.commons.editable.annotation.Password;

public class StringConverter implements TypeConverter {

	public boolean accept(Method getter) {
		if (getter.getAnnotation(Password.class) != null)
			return false;
		else
			return getter.getReturnType() == String.class;
	}
	
	public Object toObject(Class<?> type, String string) {
		return string;
	}
	
	public String toString(Object obj) {
		if (obj == null)
			return null;
		else
			return obj.toString();
	}
	
}
