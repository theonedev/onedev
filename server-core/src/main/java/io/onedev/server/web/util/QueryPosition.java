package io.onedev.server.web.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.WebConstants;

public class QueryPosition implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static String QUERY_POSITION_PARAM = "query-position";
	
	private final String query;
	
	private final int count;
	
	private final int offset;
	
	public QueryPosition(String query, int count, int offset) {
		this.query = query;
		this.count = count;
		this.offset = offset;
	}
	
	public String getQuery() {
		return query;
	}
	
	public int getOffset() {
		return offset;
	}

	public int getCount() {
		return count;
	}

	public void fill(PageParameters params) {
		params.add(QUERY_POSITION_PARAM, query + ":" + count + ":" + offset);
	}
	
	@Nullable
	public static QueryPosition from(PageParameters params) {
		String positionStr = params.get(QUERY_POSITION_PARAM).toOptionalString();
		if (positionStr != null) {
			String tempStr = StringUtils.substringBeforeLast(positionStr, ":");
			int offset = Integer.parseInt(StringUtils.substringAfterLast(positionStr, ":"));
			int count = Integer.parseInt(StringUtils.substringAfterLast(tempStr, ":"));
			String query = StringUtils.substringBeforeLast(tempStr, ":");
			return new QueryPosition(query, count, offset);
		} else {
			return null;
		}
	}
	
	@Nullable
	public static String getQuery(@Nullable QueryPosition position) {
		if (position != null)
			return position.getQuery();
		else
			return null;
	}
	
	public static int getPage(@Nullable QueryPosition position) {
		if (position != null)
			return position.getOffset() / WebConstants.PAGE_SIZE;
		else
			return 0;
	}
	
}