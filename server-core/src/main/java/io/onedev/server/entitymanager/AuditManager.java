package io.onedev.server.entitymanager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.entitymanager.support.AuditQuery;
import io.onedev.server.model.Audit;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface AuditManager extends EntityManager<Audit> {
		
	void audit(@Nullable Project project, String action, @Nullable String oldContent, @Nullable String newContent);

	List<Audit> query(@Nullable Project project, AuditQuery query, int firstResult, int maxResults);

	int count(@Nullable Project project, AuditQuery query);
	
}
