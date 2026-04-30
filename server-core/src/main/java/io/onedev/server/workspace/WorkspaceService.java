package io.onedev.server.workspace;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.shiro.subject.Subject;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.service.EntityService;
import io.onedev.server.util.FileData;
import io.onedev.server.util.ProjectWorkspaceStatusStat;
import io.onedev.server.util.criteria.Criteria;

public interface WorkspaceService extends EntityService<Workspace> {

	@Nullable Workspace find(Project project, long number);

	void create(Workspace workspace);

	void update(Workspace workspace);

	List<Workspace> query(Subject subject, @Nullable Project project, String numberQuery, int count);

	List<Workspace> query(Subject subject, @Nullable Project project, WorkspaceQuery query,
                          int firstResult, int maxResults);

	int count(Subject subject, @Nullable Project project, Criteria<Workspace> criteria);

	int count(Project project, String branchName);

	List<ProjectWorkspaceStatusStat> queryStatusStats(Collection<Project> projects);

	void delete(Collection<Workspace> workspaces);

	void requestToReprovision(Workspace workspace);

	File getWorkspaceDir(Long projectId, Long workspaceNumber);

	String openShell(Workspace workspace, String label);

	Map<String, String> getShellLabels(Workspace workspace);

	Map<Integer, Integer> getPortMappings(Workspace workspace);

	void terminateShell(Workspace workspace, String shellId);

	void onOpen(IWebSocketConnection connection, Workspace workspace, String shellId);

	void onClose(IWebSocketConnection connection);

	void onMessage(IWebSocketConnection connection, Workspace workspace, 
					String shellId, String message);
	
	@Nullable
	WorkspaceContext getWorkspaceContext(String token, boolean mustExist);

	boolean hasLfsObjects(Long projectId, Long workspaceNumber);

	GitExecutionResult executeGitCommand(Workspace workspace, String[] gitArgs);

	@Nullable
	FileData readFileData(Workspace workspace, String path);

}
