package io.onedev.server.util.query;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.Maps;

public class PullRequestQueryConstants {
	
	public static final String FIELD_NUMBER = "Number";

	public static final String ATTR_NUMBER = "number";
	
	public static final String FIELD_STATUS = "Status";
	
	public static final String FIELD_TARGET_PROJECT = "Target Project";
	
	public static final String ATTR_TARGET_PROJECT = "targetProject";
	
	public static final String FIELD_TARGET_BRANCH = "Target Branch";
	
	public static final String ATTR_TARGET_BRANCH = "targetBranch";
	
	public static final String FIELD_SOURCE_PROJECT = "Source Project";
	
	public static final String ATTR_SOURCE_PROJECT = "sourceProject";
	
	public static final String FIELD_SOURCE_BRANCH = "Source Branch";
	
	public static final String ATTR_SOURCE_BRANCH = "sourceBranch";
	
	public static final String FIELD_TITLE = "Title";
	
	public static final String ATTR_TITLE = "title";
	
	public static final String FIELD_DESCRIPTION = "Description";
	
	public static final String ATTR_DESCRIPTION = "description";
	
	public static final String FIELD_COMMENT = "Comment";

	public static final String ATTR_COMMENTS = "comments";
	
	public static final String ATTR_CODE_COMMENT_RELATIONS = "codeCommentRelations";
	
	public static final String FIELD_COMMENT_COUNT = "Comment Count";
	
	public static final String ATTR_COMMENT_COUNT = "commentCount";

	public static final String FIELD_SUBMITTER = "Submitter";
	
	public static final String ATTR_SUBMITTER = "submitter";
	
	public static final String FIELD_SUBMIT_DATE = "Submit Date";
	
	public static final String ATTR_SUBMIT_DATE = "submitDate";
	
	public static final String FIELD_UPDATE_DATE = "Update Date";
	
	public static final String ATTR_UPDATE_DATE = "updateDate";
	
	public static final String FIELD_CLOSE_DATE = "Close Date";
	
	public static final String ATTR_CLOSE_DATE = "closeInfo.date";
	
	public static final String FIELD_MERGE_STRATEGY = "Merge Strategy";
	
	public static final String ATTR_MERGE_STRATEGY = "mergeStrategy";
	
	public static final String ATTR_CLOSE_STATUS = "closeInfo.status";
	
	public static final String ATTR_CLOSE_USER = "closeInfo.user";
	
	public static final String ATTR_LAST_MERGE_PREVIEW_MERGED = "lastMergePreview.merged";
	
	public static final String ATTR_LAST_MERGE_PREVIEW_REQUEST_HEAD = "lastMergePreview.requestHead";
	
	public static final String ATTR_ID = "id";
	
	public static final String ATTR_REVIEWS = "reviews";
	
	public static final String ATTR_PULL_REQUEST_BUILDS = "pullRequestBuilds";

	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			FIELD_NUMBER, FIELD_TITLE, FIELD_TARGET_PROJECT, FIELD_TARGET_BRANCH, 
			FIELD_SOURCE_PROJECT, FIELD_SOURCE_BRANCH, FIELD_DESCRIPTION, 
			FIELD_COMMENT, FIELD_SUBMIT_DATE, FIELD_UPDATE_DATE, 
			FIELD_CLOSE_DATE, FIELD_MERGE_STRATEGY, FIELD_COMMENT_COUNT);

	public static final Map<String, String> ORDER_FIELDS = Maps.newLinkedHashMap(
			FIELD_SUBMIT_DATE, ATTR_SUBMIT_DATE,
			FIELD_UPDATE_DATE, ATTR_UPDATE_DATE,
			FIELD_CLOSE_DATE, ATTR_CLOSE_DATE,
			FIELD_NUMBER, ATTR_NUMBER,
			FIELD_STATUS, ATTR_CLOSE_STATUS,
			FIELD_TARGET_PROJECT, ATTR_TARGET_PROJECT,
			FIELD_TARGET_BRANCH, ATTR_TARGET_BRANCH,
			FIELD_SOURCE_PROJECT, ATTR_SOURCE_PROJECT,
			FIELD_SOURCE_BRANCH, ATTR_SOURCE_BRANCH,
			FIELD_COMMENT_COUNT, ATTR_COMMENT_COUNT);

}
