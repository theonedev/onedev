package io.onedev.server.xodus;

import com.google.common.collect.Lists;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.codecomment.CodeCommentEvent;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.event.project.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.model.*;
import io.onedev.server.persistence.annotation.Transactional;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Date;

import static java.lang.Long.valueOf;

/**
 * Store project visit information here as we only need to load a single database to sort projects based on user 
 * visit information
 * 
 * @author robin
 *
 */
@Singleton
public class DefaultVisitInfoService extends AbstractEnvironmentService
		implements VisitInfoService, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultVisitInfoService.class);
	
	private static final int INFO_VERSION = 6;
	
	private static final String PULL_REQUEST_STORE = "pullRequestVisit";
	
	private static final String PULL_REQUEST_CODE_COMMENT_STORE = "pullRequestCodeCommentsVisit";
	
	private static final String CODE_COMMENT_STORE = "codeCommentVisit";

	private static final String ISSUE_STORE = "issueVisit";
	
	private final ProjectService projectService;
	
	private final ClusterService clusterService;
	
	@Inject
	public DefaultVisitInfoService(ProjectService projectService, ClusterService clusterService) {
		this.projectService = projectService;
		this.clusterService = clusterService;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(VisitInfoService.class);
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			String activeServer = projectService.getActiveServer(projectId, false);
			if (activeServer != null) {
				clusterService.runOnServer(activeServer, () -> {
					removeEnv(projectId.toString());
					return null;
				});
			}
		} 
	}

	@Override
	protected File getEnvDir(String envKey) {
		File infoDir = new File(projectService.getInfoDir(valueOf(envKey)), "visit");
		FileUtils.createDir(infoDir);
		return infoDir;
	}

	@Override
	protected int getEnvVersion() {
		return INFO_VERSION;
	}
	
	@Override
	public void visitIssue(User user, Issue issue) {
		Long projectId = issue.getProject().getId();
		Long userId = user.getId();
		Long issueId = issue.getId();
		projectService.submitToReplicaServers(projectId, () -> {
			try {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, ISSUE_STORE);
				env.executeInTransaction(txn -> {
					long time = new DateTime().plusSeconds(1).getMillis();
					writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, issueId)), time);
				});
			} catch (Exception e) {
				logger.error("Error writing issue visit timestamp", e);
			}
			return null;
		});
	}

	@Override
	public void visitPullRequest(User user, PullRequest request) {
		Long projectId = request.getProject().getId();
		Long userId = user.getId();
		Long requestId = request.getId();
		projectService.submitToReplicaServers(projectId, () -> {
			try {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, PULL_REQUEST_STORE);
				env.executeInTransaction(txn -> {
					long time = new DateTime().plusSeconds(1).getMillis();
					writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, requestId)), time);
				});
			} catch (Exception e) {
				logger.error("Error writing pull request visit timestamp", e);
			}
			return null;
		});
	}
	
	@Override
	public void visitCodeComment(User user, CodeComment comment) {
		Long projectId = comment.getProject().getId();
		Long userId = user.getId();
		Long commentId = comment.getId();
		projectService.submitToReplicaServers(projectId, () -> {
			try {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, CODE_COMMENT_STORE);
				env.executeInTransaction(txn -> {
					long time = new DateTime().plusSeconds(1).getMillis();
					writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, commentId)), time);
				});
			} catch (Exception e) {
				logger.error("Error writing code comment visit timestamp", e);
			}
			return null;
		});
	}

	@Override
	public void visitPullRequestCodeComments(User user, PullRequest request) {
		Long userId = user.getId();
		Long projectId = request.getProject().getId();
		Long requestId = request.getId();
		projectService.submitToReplicaServers(projectId, () -> {
			try {
				Environment env = getEnv(projectId.toString());
				Store store = getStore(env, PULL_REQUEST_CODE_COMMENT_STORE);
				env.executeInTransaction(txn -> {
					long time = new DateTime().plusSeconds(1).getMillis();
					writeLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, requestId)), time);
				});
			} catch (Exception e) {
				logger.error("Error writing pull request code comments visit timestamp", e);
			}
			return null;
		});
	}

	@Override
	public Date getPullRequestVisitDate(User user, PullRequest request) {
		Long userId = user.getId();
		Long requestId = request.getId();
		Long projectId = request.getProject().getId();
		return projectService.runOnActiveServer(projectId, () -> {
			Environment env = getEnv(projectId.toString());
			Store store = getStore(env, PULL_REQUEST_STORE);
			return env.computeInTransaction(txn -> {
				long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, requestId)), -1);
				if (millis != -1)
					return new Date(millis);
				else
					return null;
			});
		});
	}

	@Override
	public Date getIssueVisitDate(User user, Issue issue) {
		Long userId = user.getId();
		Long projectId = issue.getProject().getId();
		Long issueId = issue.getId();
		return projectService.runOnActiveServer(projectId, () -> {
			Environment env = getEnv(projectId.toString());
			Store store = getStore(env, ISSUE_STORE);
			return env.computeInTransaction(txn -> {
				long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, issueId)), -1);
				if (millis != -1)
					return new Date(millis);
				else
					return null;
			});
		});
	}
	
	@Override
	public Date getCodeCommentVisitDate(User user, CodeComment comment) {
		Long projectId = comment.getProject().getId();
		Long commentId = comment.getId();
		Long userId = user.getId();
		return projectService.runOnActiveServer(projectId, () -> {
			Environment env = getEnv(projectId.toString());
			Store store = getStore(env, CODE_COMMENT_STORE);
			return env.computeInTransaction(txn -> {
				long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, commentId)), -1);
				if (millis != -1)
					return new Date(millis);
				else
					return null;
			});
		});
	}

	@Override
	public void syncVisitInfo(Long projectId, String syncWithServer) {
		var envDir = getEnvDir(projectId.toString());
		if (!getVersionFile(envDir).exists()) {
			Client client = ClientBuilder.newClient();
			try {
				String serverUrl = clusterService.getServerUrl(syncWithServer);
				WebTarget target = client.target(serverUrl)
						.path("~api/cluster/visit-info")
						.queryParam("projectId", projectId);
				Invocation.Builder builder = target.request();
				builder.header(HttpHeaders.AUTHORIZATION,
						KubernetesHelper.BEARER + " " + clusterService.getCredential());
				try (Response response = builder.get()) {
					KubernetesHelper.checkStatus(response);
					try (var is = response.readEntity(InputStream.class)) {
						TarUtils.untar(is, getEnvDir(projectId.toString()), false);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} finally {
				client.close();
			}
		}
	}

	@Override
	public Date getPullRequestCodeCommentsVisitDate(User user, PullRequest request) {
		Long userId = user.getId();
		Long projectId = request.getProject().getId();
		Long requestId = request.getId();
		return projectService.runOnActiveServer(projectId, () -> {
			Environment env = getEnv(projectId.toString());
			Store store = getStore(env, PULL_REQUEST_CODE_COMMENT_STORE);
			return env.computeInTransaction(txn -> {
				long millis = readLong(store, txn, new LongsByteIterable(Lists.newArrayList(userId, requestId)), -1);
				if (millis != -1)
					return new Date(millis);
				else
					return null;
			});
		});
	}
	
	@Listen
	public void on(IssueEvent event) {
		if (event.getUser() != null)
			visitIssue(event.getUser(), event.getIssue());
	}
	
	@Listen
	public void on(CodeCommentEvent event) {
		if (event.getUser() != null)
			visitCodeComment(event.getUser(), event.getComment());
	}

	@Listen
	public void on(PullRequestEvent event) {
		if (event.getUser() != null) {
			visitPullRequest(event.getUser(), event.getRequest());
			if (event instanceof PullRequestCodeCommentEvent)
				visitPullRequestCodeComments(event.getUser(), event.getRequest());
		}
	}

	@Override
	public void export(Long projectId, File targetDir) {
		export(projectId.toString(), targetDir);
	}
	
}
