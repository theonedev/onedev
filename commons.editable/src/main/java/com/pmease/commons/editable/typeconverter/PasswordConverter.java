package com.pmease.commons.editable.typeconverter;

import java.lang.reflect.Method;

import com.pmease.commons.editable.annotation.Password;

public class PasswordConverter implements TypeConverter {

	public boolean accept(Method getter) {
		return getter.getReturnType() == String.class && 
				getter.getAnnotation(Password.class) != null && 
				!getter.getAnnotation(Password.class).confirmative();
	}
	
	public Object toObject(Class<?> type, String string) {
		return string;
	}

	public String toString(Object obj) {
		return (String) obj;
	}
	
}
