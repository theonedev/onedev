package io.onedev.server.model.support.build;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jersey.repackaged.com.google.common.collect.Lists;

public class BuildConstants {
	
	public static final String ATTR_ID = "id";
	
	public static final String FIELD_CONFIGURATION = "Configuration";
	
	public static final String ATTR_CONFIGURATION = "configuration";
	
	public static final String FIELD_STATUS = "Status";
	
	public static final String ATTR_STATUS = "status";
	
	public static final String FIELD_VERSION = "Version";
	
	public static final String ATTR_VERSION = "version";
	
	public static final String FIELD_BUILD_DATE = "Build Date";
	
	public static final String ATTR_BUILD_DATE = "date";
	
	public static final String FIELD_COMMIT = "Commit";
	
	public static final String ATTR_COMMIT = "commitHash";
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			FIELD_CONFIGURATION, FIELD_VERSION, FIELD_BUILD_DATE, FIELD_COMMIT);

	public static final Map<String, String> ORDER_FIELDS = new LinkedHashMap<>();
	
	static {
		ORDER_FIELDS.put(FIELD_CONFIGURATION, ATTR_CONFIGURATION);
		ORDER_FIELDS.put(FIELD_STATUS, ATTR_STATUS);
		ORDER_FIELDS.put(FIELD_VERSION, ATTR_VERSION);
		ORDER_FIELDS.put(FIELD_BUILD_DATE, ATTR_BUILD_DATE);
		ORDER_FIELDS.put(FIELD_COMMIT, ATTR_COMMIT);
	}
	
}
