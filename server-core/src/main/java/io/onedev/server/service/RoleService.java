package io.onedev.server.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Role;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;

public interface RoleService extends EntityService<Role> {
	
	void replicate(Role role);
	
	void create(Role role, @Nullable Collection<LinkSpec> authorizedLinks);
	
	void update(Role role, @Nullable Collection<LinkSpec> authorizedLinks, @Nullable String oldName);
	
	@Nullable
	Role find(String name);
	
	Role getOwner();
	
	void setupDefaults();
	
	Collection<String> getUndefinedIssueFields();
	
	void fixUndefinedIssueFields(Map<String, UndefinedFieldResolution> resolutions);
	
	List<Role> query(@Nullable String term, int firstResult, int maxResult);
	
	int count(@Nullable String term);
	
}
