package io.onedev.server.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import jersey.repackaged.com.google.common.collect.Lists;

public class BuildConstants {
	
	public static final String ATTR_ID = "id";

	public static final String FIELD_NUMBER = "Number";
	
	public static final String ATTR_NUMBER = "number";
	
	public static final String FIELD_PROJECT = "project";
	
	public static final String ATTR_PROJECT = "project";
	
	public static final String FIELD_JOB = "Job";
	
	public static final String ATTR_JOB = "jobName";
	
	public static final String FIELD_STATUS = "Status";
	
	public static final String ATTR_STATUS = "status";
	
	public static final String FIELD_SUBMITTER = "Submitter";
	
	public static final String ATTR_SUBMITTER = "submitter";
	
	public static final String FIELD_CANCELLER = "Canceller";
	
	public static final String ATTR_CANCELLER = "canceller";
	
	public static final String FIELD_SUBMIT_DATE = "Submit Date";
	
	public static final String ATTR_SUBMIT_DATE = "submitDate";
	
	public static final String FIELD_QUEUEING_DATE = "Queueing Date";
	
	public static final String ATTR_QUEUEING_DATE = "queueingDate";
	
	public static final String FIELD_RUNNING_DATE = "Running Date";
	
	public static final String ATTR_RUNNING_DATE = "runningDate";
	
	public static final String FIELD_FINISH_DATE = "Finish Date";
	
	public static final String ATTR_FINISH_DATE = "finishDate";
	
	public static final String FIELD_COMMIT = "Commit";
	
	public static final String ATTR_COMMIT = "commitHash";
	
	public static final String ATTR_PARAMS = "params";
	
	public static final Set<String> ALL_FIELDS = Sets.newHashSet(
			FIELD_NUMBER, FIELD_JOB, FIELD_STATUS, FIELD_SUBMITTER, FIELD_CANCELLER, 
			FIELD_SUBMIT_DATE, FIELD_QUEUEING_DATE, FIELD_RUNNING_DATE, FIELD_FINISH_DATE, 
			FIELD_COMMIT, FIELD_PROJECT);
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			FIELD_JOB, FIELD_NUMBER, FIELD_COMMIT, FIELD_SUBMIT_DATE, 
			FIELD_QUEUEING_DATE, FIELD_RUNNING_DATE, FIELD_FINISH_DATE);

	public static final Map<String, String> ORDER_FIELDS = new LinkedHashMap<>();
	
	static {
		ORDER_FIELDS.put(FIELD_JOB, ATTR_JOB);
		ORDER_FIELDS.put(FIELD_STATUS, ATTR_STATUS);
		ORDER_FIELDS.put(FIELD_NUMBER, ATTR_NUMBER);
		ORDER_FIELDS.put(FIELD_SUBMIT_DATE, ATTR_SUBMIT_DATE);
		ORDER_FIELDS.put(FIELD_QUEUEING_DATE, ATTR_QUEUEING_DATE);
		ORDER_FIELDS.put(FIELD_RUNNING_DATE, ATTR_RUNNING_DATE);
		ORDER_FIELDS.put(FIELD_FINISH_DATE, ATTR_FINISH_DATE);
		ORDER_FIELDS.put(FIELD_COMMIT, ATTR_COMMIT);
	}
	
}
