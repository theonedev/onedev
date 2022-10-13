package io.onedev.server.event;

import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

public class CodeStorageInitialized extends ProjectEvent {
	
	public CodeStorageInitialized(Project project) {
		super(SecurityUtils.getUser(), project.getCreateDate(), project);
	}

	@Override
	public String getActivity() {
		return "storage initialized";
	}

	@Override
	public ProjectEvent cloneIn(Dao dao) {
		return new CodeStorageInitialized(dao.load(Project.class, getProject().getId()));
	}

}
