package io.onedev.server.web.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class QueryPosition implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static String PAGE_PARAM = "query-position";
	
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
		params.add(PAGE_PARAM, query + ":" + count + ":" + offset);
	}
	
	@Nullable
	public static QueryPosition from(PageParameters params) {
		String positionStr = params.get(PAGE_PARAM).toOptionalString();
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
}