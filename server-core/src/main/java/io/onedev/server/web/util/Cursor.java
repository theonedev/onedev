package io.onedev.server.web.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.WebConstants;

public class Cursor implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static String PARAM = "cursor";
	
	private final String query;
	
	private final int count;
	
	private final int offset;
	
	public Cursor(String query, int count, int offset) {
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
		params.add(PARAM, query + ":" + count + ":" + offset);
	}
	
	@Nullable
	public static Cursor from(PageParameters params) {
		String cursorStr = params.get(PARAM).toOptionalString();
		if (cursorStr != null) {
			String tempStr = StringUtils.substringBeforeLast(cursorStr, ":");
			int offset = Integer.parseInt(StringUtils.substringAfterLast(cursorStr, ":"));
			int count = Integer.parseInt(StringUtils.substringAfterLast(tempStr, ":"));
			String query = StringUtils.substringBeforeLast(tempStr, ":");
			return new Cursor(query, count, offset);
		} else {
			return null;
		}
	}
	
	@Nullable
	public static String getQuery(@Nullable Cursor cursor) {
		if (cursor != null)
			return cursor.getQuery();
		else
			return null;
	}
	
	public static int getPage(@Nullable Cursor cursor) {
		if (cursor != null)
			return cursor.getOffset() / WebConstants.PAGE_SIZE;
		else
			return 0;
	}
	
}