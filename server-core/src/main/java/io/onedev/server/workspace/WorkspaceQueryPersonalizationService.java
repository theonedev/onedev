package io.onedev.server.workspace;

import io.onedev.server.model.WorkspaceQueryPersonalization;
import io.onedev.server.service.EntityService;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public interface WorkspaceQueryPersonalizationService extends EntityService<WorkspaceQueryPersonalization> {

	WorkspaceQueryPersonalization find(Project project, User user);

	void createOrUpdate(WorkspaceQueryPersonalization personalization);

}
