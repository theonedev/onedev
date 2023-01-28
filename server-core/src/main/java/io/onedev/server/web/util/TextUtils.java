package io.onedev.server.web.util;

import org.apache.commons.lang3.StringUtils;

public class TextUtils {

	public static String getDisplayValue(boolean value) {
		return value? "Yes": "No";
	}

	public static String getDisplayValue(Enum<?> value) {
		return StringUtils.capitalize(value.name().replace('_', ' ').toLowerCase());		
	}
	
}
