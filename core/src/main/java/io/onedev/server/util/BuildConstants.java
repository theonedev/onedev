package io.onedev.server.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jersey.repackaged.com.google.common.collect.Lists;

public class BuildConstants {
	
	public static final String ATTR_ID = "id";

	public static final String FIELD_NUMBER = "Number";
	
	public static final String ATTR_NUMBER = "number";
	
	public static final String ATTR_PROJECT = "project";
	
	public static final String FIELD_JOB = "Job";
	
	public static final String ATTR_JOB = "jobName";
	
	public static final String FIELD_STATUS = "Status";
	
	public static final String ATTR_STATUS = "status";
	
	public static final String FIELD_BUILD_DATE = "Build Date";
	
	public static final String ATTR_BUILD_DATE = "date";
	
	public static final String FIELD_COMMIT = "Commit";
	
	public static final String ATTR_COMMIT = "commitHash";
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			FIELD_JOB, FIELD_NUMBER, FIELD_BUILD_DATE, FIELD_COMMIT);

	public static final Map<String, String> ORDER_FIELDS = new LinkedHashMap<>();
	
	static {
		ORDER_FIELDS.put(FIELD_JOB, ATTR_JOB);
		ORDER_FIELDS.put(FIELD_STATUS, ATTR_STATUS);
		ORDER_FIELDS.put(FIELD_NUMBER, ATTR_NUMBER);
		ORDER_FIELDS.put(FIELD_BUILD_DATE, ATTR_BUILD_DATE);
		ORDER_FIELDS.put(FIELD_COMMIT, ATTR_COMMIT);
	}
	
}
