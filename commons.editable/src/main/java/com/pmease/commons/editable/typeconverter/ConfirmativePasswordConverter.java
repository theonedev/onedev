package com.pmease.commons.editable.typeconverter;

import java.lang.reflect.Method;

import com.pmease.commons.editable.annotation.Password;
import com.pmease.commons.util.StringUtils;

public class ConfirmativePasswordConverter implements TypeConverter {

	public boolean accept(Method getter) {
		return getter.getAnnotation(Password.class) != null 
				&& getter.getAnnotation(Password.class).confirmative();
	}
	
	public Object toObject(Class<?> type, String string) {
		if (string != null) {
			if (string.indexOf('\n') != -1) {
				String[] parts = StringUtils.split(string, "\n");
				if (parts.length == 0) {
					return null;
				} else if (parts.length != 2 || !parts[0].equals(parts[1])) {
					throw new ConversionException("Password and it's " +
							"confirmation should be identical.");
				} else {
					if (parts[0].length() == 0)
						return null;
					else
						return parts[0];
				}
			} else {
				return string;
			}
		} else {
			return null;
		}
	}

	public String toString(Object obj) {
		return (String) obj;
	}
	
}
