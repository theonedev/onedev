package io.onedev.server.util;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.Maps;

public class CodeCommentConstants {
	
	public static final String ATTR_PROJECT = "project";
	
	public static final String FIELD_CONTENT = "Content";
	
	public static final String ATTR_CONTENT = "content";
	
	public static final String FIELD_REPLY = "Reply";
	
	public static final String FIELD_PATH = "Path";
	
	public static final String ATTR_PATH = "markPos.path";
	
	public static final String FIELD_REPLY_COUNT = "Reply Count";
	
	public static final String ATTR_REPLY_COUNT = "replyCount";
	
	public static final String ATTR_COMMIT = "markPos.commit";
	
	public static final String FIELD_CREATE_DATE = "Create Date";
	
	public static final String ATTR_CREATE_DATE = "date";
	
	public static final String FIELD_UPDATE_DATE = "Update Date";
	
	public static final String ATTR_UPDATE_DATE = "updateDate";
	
	public static final String ATTR_USER = "user";
	
	public static final String ATTR_RELATIONS = "relations";
	
	public static final String ATTR_REPLIES = "replies";

	public static final String ATTR_ID = "id";
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			FIELD_CONTENT, FIELD_REPLY, FIELD_PATH, FIELD_CREATE_DATE, FIELD_UPDATE_DATE, FIELD_REPLY_COUNT);

	public static final Map<String, String> ORDER_FIELDS = Maps.newLinkedHashMap(
			FIELD_CREATE_DATE, ATTR_CREATE_DATE,
			FIELD_UPDATE_DATE, ATTR_UPDATE_DATE,
			FIELD_REPLY_COUNT, ATTR_REPLY_COUNT);
	
}
