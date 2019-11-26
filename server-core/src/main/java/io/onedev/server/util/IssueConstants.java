package io.onedev.server.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.Maps;

public class IssueConstants {
	
	public static final String FIELD_NUMBER = "Number";
	
	public static final String ATTR_NUMBER = "number";
	
	public static final String FIELD_PROJECT = "Project";
	
	public static final String ATTR_PROJECT = "project";
	
	public static final String FIELD_STATE = "State";
	
	public static final String ATTR_STATE = "state";
	
	public static final String FIELD_TITLE = "Title";
	
	public static final String ATTR_TITLE = "title";
	
	public static final String FIELD_DESCRIPTION = "Description";
	
	public static final String ATTR_DESCRIPTION = "description";
	
	public static final String FIELD_COMMENT = "Comment";
	
	public static final String ATTR_COMMENTS = "comments";
	
	public static final String FIELD_SUBMITTER = "Submitter";
	
	public static final String ATTR_SUBMITTER = "submitter";
	
	public static final String FIELD_SUBMIT_DATE = "Submit Date";
	
	public static final String ATTR_SUBMIT_DATE = "submitDate";
	
	public static final String FIELD_VOTE_COUNT = "Vote Count";
	
	public static final String ATTR_VOTE_COUNT = "voteCount";
	
	public static final String FIELD_COMMENT_COUNT = "Comment Count";
	
	public static final String ATTR_COMMENT_COUNT = "commentCount";
	
	public static final String FIELD_UPDATE_DATE = "Update Date";
	
	public static final String ATTR_UPDATE_DATE = "updateDate";
	
	public static final String FIELD_MILESTONE = "Milestone";
	
	public static final String ATTR_MILESTONE = "milestone";
	
	public static final String ATTR_FIELDS = "fields";
		
	public static final String ATTR_ID = "id";
	
	public static final Set<String> ALL_FIELDS = Sets.newHashSet(
			FIELD_NUMBER, FIELD_STATE, FIELD_TITLE, FIELD_SUBMITTER, FIELD_DESCRIPTION, 
			FIELD_COMMENT, FIELD_SUBMIT_DATE, FIELD_UPDATE_DATE, FIELD_VOTE_COUNT, 
			FIELD_COMMENT_COUNT, FIELD_MILESTONE, FIELD_PROJECT);
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			FIELD_NUMBER, FIELD_STATE, FIELD_TITLE, FIELD_DESCRIPTION, FIELD_COMMENT, 
			FIELD_SUBMIT_DATE, FIELD_UPDATE_DATE, FIELD_VOTE_COUNT, FIELD_COMMENT_COUNT, 
			FIELD_MILESTONE);

	public static final Map<String, String> ORDER_FIELDS = Maps.newLinkedHashMap(
			FIELD_VOTE_COUNT, ATTR_VOTE_COUNT,
			FIELD_COMMENT_COUNT, ATTR_COMMENT_COUNT,
			FIELD_NUMBER, ATTR_NUMBER,
			FIELD_SUBMIT_DATE, ATTR_SUBMIT_DATE,
			FIELD_UPDATE_DATE, ATTR_UPDATE_DATE);		
	
}
