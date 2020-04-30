package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Role;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;

public interface RoleManager extends EntityManager<Role> {
	
	void replicate(Role role);
	
	void save(Role role, @Nullable String oldName);
	
	@Nullable
	Role find(String name);
	
	Role getOwner();
	
	void setupDefaults();
	
	Collection<String> getUndefinedIssueFields();
	
	void fixUndefinedIssueFields(Map<String, UndefinedFieldResolution> resolutions);
	
}
