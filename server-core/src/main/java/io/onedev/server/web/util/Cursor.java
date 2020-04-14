package io.onedev.server.web.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.web.WebConstants;

public class Cursor implements Serializable {
	
	private static final long serialVersionUID = 1L;

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