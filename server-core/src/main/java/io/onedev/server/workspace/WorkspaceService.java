package io.onedev.server.workspace;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.shiro.subject.Subject;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import io.onedev.agent.workspace.FileData;
import io.onedev.agent.workspace.GitExecutionResult;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.Workspace;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.service.EntityService;
import io.onedev.server.util.ProjectWorkspaceStatusStat;
import io.onedev.server.util.criteria.Criteria;

public interface WorkspaceService extends EntityService<Workspace> {

	@Nullable Workspace find(Project project, long number);

	Workspace create(User user, Project project, ObjectId commitId, @Nullable String branch, String specName);

	void update(Workspace workspace);

	List<Workspace> query(Subject subject, @Nullable Project project, String numberQuery, int count);

	List<Workspace> query(Subject subject, @Nullable Project project, WorkspaceQuery query,
                          int firstResult, int maxResults);

	int count(Subject subject, @Nullable Project project, Criteria<Workspace> criteria);

	int count(Project project, String branchName);

	List<ProjectWorkspaceStatusStat> queryStatusStats(Collection<Project> projects);

	void delete(Collection<Workspace> workspaces);

	void reset(Workspace workspace);

	/**
	 * Get the workspace log file. The file is stored on the project's active
	 * server in a dedicated log location independent of the provisioner so that
	 * it stays available regardless of where the provisioner places its
	 * runtime workspace data (server file system, PVC, remote agent, etc.).
	 */
	File getLogFile(Long projectId, Long workspaceNumber);

	String openShell(Workspace workspace, String label, @Nullable String command, 
		@Nullable ShellOutputCallback outputCallback);

	Map<String, String> getShellLabels(Workspace workspace);	

	Map<Integer, Integer> getPortMappings(Workspace workspace);

	String getPortHost(Workspace workspace);

	void terminateShell(Workspace workspace, String shellId);

	void onOpen(IWebSocketConnection connection, Workspace workspace, String shellId);

	void onClose(IWebSocketConnection connection);

	void onMessage(Workspace workspace, String shellId, String message);

	@Nullable
	WorkspaceContext getWorkspaceContext(String token, boolean mustExist);

	GitExecutionResult executeGitCommand(Workspace workspace, String[] gitArgs);

	@Nullable
	FileData readFileData(Workspace workspace, String path);

	void runPrompt(User ai, Project project, String branch, String prompt, TaskFailedCallback taskFailedCallback);
	
}
