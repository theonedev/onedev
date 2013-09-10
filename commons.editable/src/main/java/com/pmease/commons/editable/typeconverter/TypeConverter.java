package com.pmease.commons.editable.typeconverter;

import java.lang.reflect.Method;

public interface TypeConverter {
	
	boolean accept(Method getter);
	
	String toString(Object obj);
	
	Object toObject(Class<?> type, String string);
}
