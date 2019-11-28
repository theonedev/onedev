package io.onedev.server.util.xstream;

import io.onedev.commons.utils.StringUtils;

public class StringConverter extends com.thoughtworks.xstream.converters.basic.StringConverter {

	@Override
	public Object fromString(String str) {
		return super.fromString(str);
	}

	@Override
	public String toString(Object obj) {
		String string = (String) obj;
		if (string != null && string.indexOf('\0') != -1)
			string = StringUtils.replace(string, "\0", "");
		return super.toString(string);
	}
	
}
