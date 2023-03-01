package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Role;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;

public interface RoleManager extends EntityManager<Role> {
	
	void replicate(Role role);
	
	void create(Role role, Collection<LinkSpec> authorizedLinks);
	
	void update(Role role, Collection<LinkSpec> authorizedLinks, @Nullable String oldName);
	
	@Nullable
	Role find(String name);
	
	Role getOwner();
	
	void setupDefaults();
	
	Collection<String> getUndefinedIssueFields();
	
	void fixUndefinedIssueFields(Map<String, UndefinedFieldResolution> resolutions);
	
	List<Role> query(@Nullable String term, int firstResult, int maxResult);
	
	int count(@Nullable String term);
	
}
