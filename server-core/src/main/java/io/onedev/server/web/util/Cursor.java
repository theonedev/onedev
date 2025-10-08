package io.onedev.server.web.util;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.WebConstants;

public class Cursor implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String query;
	
	private final int count;
	
	private final int offset;
	
	private final Long projectId;
	
	public Cursor(String query, int count, int offset, @Nullable ProjectScope projectScope) {
		this.query = query;
		this.count = count;
		this.offset = offset;
		if (projectScope != null) 
			projectId = projectScope.getProject().getId();
		else 
			projectId = null;
	}
	
	public Cursor(String query, int count, int offset, @Nullable Project project) {
		this.query = query;
		this.count = count;
		this.offset = offset;
		projectId = Project.idOf(project);
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
	public ProjectScope getProjectScope() {
		if (projectId != null) 
			return new ProjectScope(OneDev.getInstance(ProjectService.class).load(projectId), true, true);
		else 
			return null;
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