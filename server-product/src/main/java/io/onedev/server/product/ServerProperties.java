package io.onedev.server.product;

import java.util.Map;
import java.util.Properties;

public class ServerProperties extends Properties {

	private static final long serialVersionUID = 1L;

	public ServerProperties(Properties properties) {
		for (Map.Entry<Object, Object> entry: properties.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

}
