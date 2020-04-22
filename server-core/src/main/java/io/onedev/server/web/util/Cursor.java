package io.onedev.server.web.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.web.WebConstants;

public class Cursor implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String query;
	
	private final int count;
	
	private final int offset;
	
	private final boolean inProject;
	
	public Cursor(String query, int count, int offset, boolean inProject) {
		this.query = query;
		this.count = count;
		this.offset = offset;
		this.inProject = inProject;
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

	public boolean isInProject() {
		return inProject;
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