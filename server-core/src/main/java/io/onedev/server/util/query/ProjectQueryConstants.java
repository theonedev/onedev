package io.onedev.server.util.query;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.Maps;

public class ProjectQueryConstants {
	
	public static final String FIELD_NAME = "Name";
	
	public static final String ATTR_NAME = "name";
	
	public static final String FIELD_UPDATE_DATE = "Update Date";
	
	public static final String ATTR_UPDATE_DATE = "updateDate";
	
	public static final String FIELD_DESCRIPTION = "Description";
	
	public static final String ATTR_DESCRIPTION = "description";
	
	public static final String FIELD_OWNER = "Owner";
	
	public static final String ATTR_OWNER = "owner";
	
	public static final String ATTR_ID = "id";
	
	public static final String ATTR_FORKED_FROM = "forkedFrom";
	
	public static final List<String> QUERY_FIELDS = 
			Lists.newArrayList(FIELD_NAME, FIELD_DESCRIPTION, FIELD_UPDATE_DATE);

	public static final Map<String, String> ORDER_FIELDS = Maps.newLinkedHashMap(
			FIELD_NAME, ATTR_NAME, 
			FIELD_OWNER, ATTR_OWNER, 
			FIELD_UPDATE_DATE, ATTR_UPDATE_DATE);
	
}
