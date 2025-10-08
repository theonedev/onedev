package io.onedev.server.git;

import static com.google.common.hash.Hashing.sha256;
import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.server.model.Project.decodeFullRepoNameAsPath;
import static io.onedev.server.util.CollectionUtils.newHashMap;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NOT_IMPLEMENTED;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;
import static org.apache.tika.mime.MimeTypes.OCTET_STREAM;
import static org.glassfish.jersey.client.ClientProperties.REQUEST_ENTITY_PROCESSING;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashingInputStream;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.GitLfsLockService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.GitLfsLock;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.CodePullAuthorizationSource;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.facade.ProjectFacade;

@Singleton
public class GitLfsFilter implements Filter {

	private static final String CONTENT_TYPE = "application/vnd.git-lfs+json";
	
	private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
	
	public static final int MAX_PAGE_SIZE = 100;
	
	private static final Logger logger = LoggerFactory.getLogger(GitLfsFilter.class);
	
	private final ProjectService projectService;
	
	private final ObjectMapper objectMapper;
	
	private final SessionService sessionService;
	
	private final SettingService settingService;
	
	private final GitLfsLockService lockService;
	
	private final ClusterService clusterService;
	
	private final Set<CodePullAuthorizationSource> codePullAuthorizationSources;
	
	@Inject
	public GitLfsFilter(ProjectService projectService, ObjectMapper objectMapper, SessionService sessionService,
                        SettingService settingService, GitLfsLockService lockService, ClusterService clusterService,
                        Set<CodePullAuthorizationSource> codePullAuthorizationSources) {
		this.projectService = projectService;
		this.objectMapper = objectMapper;
		this.sessionService = sessionService;
		this.settingService = settingService;
		this.lockService = lockService;
		this.clusterService = clusterService;
		this.codePullAuthorizationSources = codePullAuthorizationSources;
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	private long getMaxLFSFileSize() {
		return (long) settingService.getPerformanceSetting().getMaxGitLFSFileSize() * 1024 * 1024;
	}
	
	private boolean canReadCode(HttpServletRequest request, Project project) {
		if (!SecurityUtils.canReadCode(project)) {
			for (CodePullAuthorizationSource source: codePullAuthorizationSources) {
				if (source.canPullCode(request, project)) 
					return true;
			}
			return false;
		} else {
			return true;
		}
	}

	private boolean canAccessProject(HttpServletRequest request, Project project) {
		if (!SecurityUtils.canAccessProject(project)) {
			for (CodePullAuthorizationSource source: codePullAuthorizationSources) {
				if (source.canPullCode(request, project)) 
					return true;
			}
			return false;
		} else {
			return true;
		}
	}

	private String getObjectUrl(HttpServletRequest request, String projectPath, String objectId) {
		var serverUrl = settingService.getSystemSetting().getServerUrl();
		return String.format("%s/%s.git/lfs/objects/%s?lfs-objects=true", 
				StringUtils.stripEnd(serverUrl, "/\\"), projectPath, objectId);
	}

	private String getProjectPath(String pathInfo) {
		String projectPath =  substringBeforeLast(pathInfo, ".git/");
		if (StringUtils.isBlank(projectPath))
			throw new ExplicitException("Project not specified");
		return decodeFullRepoNameAsPath(projectPath);
	}

	private void reportProjectNotFoundOrInaccessible(HttpServletResponse response, String projectPath) {
		if (SecurityUtils.getUser() != null) {
			sendBatchError(response, SC_NOT_FOUND, "Project not found or inaccessible: " + projectPath);
		} else {
			response.addHeader("LFS-Authenticate", "Basic realm=\"OneDev\"");
			sendBatchError(response, SC_UNAUTHORIZED, "Authentication required");
		}
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String uri = httpRequest.getRequestURI();
		String pathInfo = uri.substring(httpRequest.getContextPath().length());
		pathInfo = StringUtils.stripStart(pathInfo, "/");
		boolean clusterAccess = SecurityUtils.isSystem();
		
		if ("true".equals(httpRequest.getParameter("lfs-objects"))) {
			String projectPath = getProjectPath(pathInfo);
			String objectId = StringUtils.substringAfterLast(pathInfo, "/");
			
			if (httpRequest.getMethod().equals("GET")) {
				LfsObject lfsObject = null;
				if (clusterAccess) {
					lfsObject = new LfsObject(projectService.findFacadeByPath(projectPath).getId(), objectId);
				} else {
					sessionService.openSession();
					try {
						Project project = projectService.findByPath(projectPath);
						if (project == null || !canAccessProject(httpRequest, project))
							reportProjectNotFoundOrInaccessible(httpResponse, projectPath);
						else if (canReadCode(httpRequest, project))  
							lfsObject = new LfsObject(project.getId(), objectId);
						else 
							sendAuthorizationError(httpResponse);
					} finally {
						sessionService.closeSession();
					}
				}

				if (lfsObject != null) {
					httpResponse.setContentType(OCTET_STREAM);
					String activeServer = projectService.getActiveServer(lfsObject.getProjectId(), true);
					if (activeServer.equals(clusterService.getLocalServerAddress())) {
						try (
								InputStream is = lfsObject.getInputStream();
								OutputStream os = httpResponse.getOutputStream()) {
							IOUtils.copy(is, os, BUFFER_SIZE);
						}
					} else {
						Client client = ClientBuilder.newClient();
						try {
							String serverUrl = clusterService.getServerUrl(activeServer);
							WebTarget target = client.target(serverUrl)
									.path("~api/cluster/lfs")
									.queryParam("projectId", lfsObject.getProjectId())
									.queryParam("objectId", lfsObject.getObjectId());
							Invocation.Builder builder =  target.request();
							builder.header(AUTHORIZATION, BEARER + " " + clusterService.getCredential());
							try (Response lfsResponse = builder.get()){
								KubernetesHelper.checkStatus(lfsResponse);
								try (
										InputStream is = lfsResponse.readEntity(InputStream.class);
										OutputStream os = httpResponse.getOutputStream()) {
									IOUtils.copy(is, os, BUFFER_SIZE);
								}
							}
						} finally {
							client.close();
						}
					}
				}
			} else {
				LfsObject lfsObject = null;
				if (clusterAccess) {
					lfsObject = new LfsObject(projectService.findFacadeByPath(projectPath).getId(), objectId);
				} else {
					sessionService.openSession();
					try {
						Project project = projectService.findByPath(getProjectPath(pathInfo));
						if (project == null || !canAccessProject(httpRequest, project))
							reportProjectNotFoundOrInaccessible(httpResponse, projectPath);
						else if (SecurityUtils.canWriteCode(project))  
							lfsObject = new LfsObject(project.getId(), objectId);
						else 
							sendAuthorizationError(httpResponse);
					} finally {
						sessionService.closeSession();
					}
				}

				if (lfsObject != null) {
					String activeServer = projectService.getActiveServer(lfsObject.getProjectId(), true);
					var hash = new AtomicReference<String>(null);
					try {
						if (activeServer.equals(clusterService.getLocalServerAddress())) {
							try (
									HashingInputStream is = new HashingInputStream(
											sha256(), httpRequest.getInputStream());
									OutputStream os = lfsObject.getOutputStream()) {
								IOUtils.copy(is, os, BUFFER_SIZE);
								hash.set(Hex.encodeHexString(is.hash().asBytes()));
							}
						} else {
							Client client = ClientBuilder.newClient();
							client.property(REQUEST_ENTITY_PROCESSING, "CHUNKED");
							try {
								String serverUrl = clusterService.getServerUrl(activeServer);
								WebTarget target = client.target(serverUrl)
										.path("~api/cluster/lfs")
										.queryParam("projectId", lfsObject.getProjectId())
										.queryParam("objectId", lfsObject.getObjectId());
								Invocation.Builder builder = target.request();
								builder.header(AUTHORIZATION,
										BEARER + " " + clusterService.getCredential());

								StreamingOutput os = output -> {
									try (HashingInputStream is = new HashingInputStream(
											sha256(), httpRequest.getInputStream())) {
										IOUtils.copy(is, output, BUFFER_SIZE);
										hash.set(Hex.encodeHexString(is.hash().asBytes()));
									} finally {
										output.close();
									}
								};

								try (Response lfsResponse = builder.post(Entity.entity(os, APPLICATION_OCTET_STREAM))) {
									KubernetesHelper.checkStatus(lfsResponse);
								}
							} finally {
								client.close();
							}
						}
					} finally {
						if (!objectId.equals(hash.get())) {
							lfsObject.delete();
							throw new RuntimeException("Invalid uploaded content: hash not equals to object id");
						}
					}
				}
			}				
		} else if (httpRequest.getContentType() != null 
					&& httpRequest.getContentType().startsWith(CONTENT_TYPE)
				|| httpRequest.getHeader("Accept") != null 
					&& httpRequest.getHeader("Accept").startsWith(CONTENT_TYPE)) {
			String projectPath = getProjectPath(pathInfo);
			
			if (clusterAccess) {
				ProjectFacade project = projectService.findFacadeByPath(projectPath);
				if (project == null) {
					sendBatchError(httpResponse, SC_NOT_FOUND, "Project not found: " + projectPath);
				} else {
					httpResponse.setContentType(CONTENT_TYPE);
					if (pathInfo.endsWith("/batch")) {
						processBatch(httpRequest, httpResponse, project, new BooleanSupplier() {
							
							@Override
							public boolean getAsBoolean() {
								return true;
							}
							
						}, () -> true, clusterService.getCredential());
					} else {
						httpResponse.setStatus(SC_NOT_IMPLEMENTED);
					}
				}
			} else {
				sessionService.openSession();
				try {
					Project project = projectService.findByPath(projectPath);
					if (project == null || !canAccessProject(httpRequest, project)) {
						reportProjectNotFoundOrInaccessible(httpResponse, projectPath);
					} else {
						httpResponse.setContentType(CONTENT_TYPE);
						if (pathInfo.endsWith("/batch")) {
							String accessToken = SecurityUtils.createTemporalAccessTokenIfUserPrincipal(300);
							processBatch(httpRequest, httpResponse, project.getFacade(), 
									() -> canReadCode(httpRequest, project), 
									() -> SecurityUtils.canWriteCode(project), 
									accessToken);
						} else if (pathInfo.endsWith("/locks")) {
							if (httpRequest.getMethod().equals("POST")) {
								if (SecurityUtils.canWriteCode(project)) {
									JsonNode lockRequestNode;
									try (InputStream is = httpRequest.getInputStream()) {
										lockRequestNode = objectMapper.readTree(is);
									}
									String path = lockRequestNode.get("path").asText();
									GitLfsLock lock = lockService.find(path);
									if (lock == null) {
										lock = new GitLfsLock();
										lock.setPath(path);
										lock.setOwner(SecurityUtils.getUser());
										lockService.create(lock);
										httpResponse.setStatus(SC_CREATED);
									} else {
										httpResponse.setStatus(SC_CONFLICT);
									}
									Map<Object, Object> lockResponse = newHashMap("lock", toMap(lock));
									if (httpResponse.getStatus() == SC_CONFLICT)
										lockResponse.put("message", "Lock exists");
									writeTo(httpResponse, lockResponse);
								} else {
									sendAuthorizationError(httpResponse);
								}
							} else {
								if (canReadCode(httpRequest, project)) {
									String path = httpRequest.getParameter("path");
									
									Long id = null;
									String idString = httpRequest.getParameter("id");
									if (idString != null)
										id = Long.valueOf(idString);
									
									int cursor = 0;
									String cursorString = httpRequest.getParameter("cursor");
									if (cursorString != null)
										cursor = Integer.parseInt(cursorString);
									
									int limit = MAX_PAGE_SIZE;
									String limitString = httpRequest.getParameter("limit");
									if (limitString != null)
										limit = Integer.parseInt(limitString);
									if (limit > MAX_PAGE_SIZE)
										limit = MAX_PAGE_SIZE;
									
									EntityCriteria<GitLfsLock> criteria = EntityCriteria.of(GitLfsLock.class);
									if (path != null)
										criteria.add(Restrictions.eq(GitLfsLock.PROP_PATH, path));
									if (id != null)
										criteria.add(Restrictions.eq(GitLfsLock.PROP_ID, id));
									
									List<Map<Object, Object>> locks = new ArrayList<>();
									for (GitLfsLock lock: lockService.query(criteria, cursor, limit))
										locks.add(toMap(lock));
									Map<Object, Object> locksResponse = newHashMap("locks", locks);
									if (locks.size() == limit)
										locksResponse.put("next_cursor", String.valueOf(cursor+limit));
									writeTo(httpResponse, locksResponse);
								} else {
									sendAuthorizationError(httpResponse);
								}
							}
						} else if (pathInfo.endsWith("/locks/verify")) {
							if (SecurityUtils.canWriteCode(project)) {
								JsonNode lockVerifyNode;
								try (InputStream is = httpRequest.getInputStream()) {
									lockVerifyNode = objectMapper.readTree(is);
								}
		
								String path = null;
								JsonNode pathNode = lockVerifyNode.get("path");
								if (pathNode != null)
									path = pathNode.asText();
								
								Long id = null;
								JsonNode idNode = lockVerifyNode.get("id");
								if (idNode != null)
									id = idNode.asLong();
								
								int cursor = 0;
								JsonNode cursorNode = lockVerifyNode.get("cursor");
								if (cursorNode != null)
									cursor = cursorNode.intValue();
		
								int limit = MAX_PAGE_SIZE;
								JsonNode limitNode = lockVerifyNode.get("limit");
								if (limitNode != null)
									limit = limitNode.asInt();
								if (limit > MAX_PAGE_SIZE)
									limit = MAX_PAGE_SIZE;
								
								EntityCriteria<GitLfsLock> criteria = EntityCriteria.of(GitLfsLock.class);
								if (path != null)
									criteria.add(Restrictions.eq(GitLfsLock.PROP_PATH, path));
								if (id != null)
									criteria.add(Restrictions.eq(GitLfsLock.PROP_ID, id));
								
								List<Map<Object, Object>> ourLocks =  new ArrayList<>();
								List<Map<Object, Object>> theirLocks = new ArrayList<>();
								
								for (GitLfsLock lock: lockService.query(criteria, cursor, limit)) {
									if (lock.getOwner().equals(SecurityUtils.getUser()))
										ourLocks.add(toMap(lock));
									else
										theirLocks.add(toMap(lock));
								}
								Map<Object, Object> verifyResponse = newHashMap(
										"ours", ourLocks, 
										"theirs", theirLocks);
								if (ourLocks.size() + theirLocks.size() == limit)
									verifyResponse.put("next_cursor", String.valueOf(cursor+limit));
								writeTo(httpResponse, verifyResponse);
							} else {
								sendAuthorizationError(httpResponse);
							}
						} else if (pathInfo.endsWith("/unlock")) {
							if (SecurityUtils.canWriteCode(project)) {
								Long id = Long.valueOf(StringUtils.substringAfterLast(
										substringBeforeLast(pathInfo, "/"), "/"));
								
								JsonNode lockDeleteNode;
								try (InputStream is = httpRequest.getInputStream()) {
									lockDeleteNode = objectMapper.readTree(is);
								}
								
								boolean force = false;
								JsonNode forceNode = lockDeleteNode.get("force");
								if (forceNode != null)
									force = forceNode.asBoolean();

								GitLfsLock lock = lockService.load(id);
								if (lock.getOwner().equals(SecurityUtils.getUser())) {
									lockService.delete(lock);
									writeTo(httpResponse, newHashMap("lock", toMap(lock)));
								} else if (force) {
									if (SecurityUtils.canManageProject(project)) {
										lockService.delete(lock);
										writeTo(httpResponse, newHashMap("lock", toMap(lock)));
									} else {
										sendBatchError(httpResponse, SC_FORBIDDEN, "Only project managers can unlock forcibly");
									}
								} else {
									sendBatchError(httpResponse, SC_FORBIDDEN, "Lock is created by other users");
								}
							} else {
								sendAuthorizationError(httpResponse);
							}
						}
					}
				} catch (Exception e) {
					logger.error("Error handling LFS request", e);
					sendBatchError(httpResponse, SC_INTERNAL_SERVER_ERROR, 
							"Internal server error, please check server log");
				} finally {
					sessionService.closeSession();
				}				
			}
		} else {
			chain.doFilter(request, response);
		}
	}
	
	private void processBatch(HttpServletRequest httpRequest, HttpServletResponse httpResponse, 
							  ProjectFacade project, BooleanSupplier readCheck, 
							  BooleanSupplier writeCheck, @Nullable String accessToken) {
		JsonNode batchRequestNode;
		try (InputStream is = httpRequest.getInputStream()) {
			batchRequestNode = objectMapper.readTree(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		boolean supportBasicTransfer;
		JsonNode transfersNode = batchRequestNode.get("transfers");
		if (transfersNode != null) {
			supportBasicTransfer = false;
			for (JsonNode transferNode: transfersNode) {
				if (transferNode.asText().equals("basic")) {
					supportBasicTransfer = true;
					break;
				}
			}
		} else {
			supportBasicTransfer = true;
		}
		if (!supportBasicTransfer) {
			sendBatchError(httpResponse, SC_NOT_ACCEPTABLE, 
					"This server can only accept basic transfer");
		} else {
			boolean supportSha256;
			JsonNode hashAlgoNode = batchRequestNode.get("hash_algo");
			if (hashAlgoNode != null)
				supportSha256 = hashAlgoNode.asText().equals("sha256");
			else
				supportSha256 = true;
			if (!supportSha256) {
				sendBatchError(httpResponse, SC_NOT_ACCEPTABLE, 
						"This server can only accept sha256 hash algorithm");
			} else {
				boolean upload = batchRequestNode.get("operation").asText().equals("upload");
				boolean authorized = false;
				if (upload) {
					if (!writeCheck.getAsBoolean()) 
						sendAuthorizationError(httpResponse);
					else
						authorized = true;
				} else {
					if (!readCheck.getAsBoolean())
						sendAuthorizationError(httpResponse);
					else
						authorized = true;
				}
				if (authorized) {
					List<Map<String, Object>> objectsResponse = new ArrayList<>();
					for (JsonNode objectNode: batchRequestNode.get("objects")) {
						String objectId = objectNode.get("oid").asText();
						long objectSize = objectNode.get("size").asLong();
						objectsResponse.add(getObjectResponse(
								httpRequest, project, upload, objectId, objectSize, accessToken));
					}
					
					Map<String, Object> batchResponse = new HashMap<>();
					batchResponse.put("objects", objectsResponse);
					writeTo(httpResponse, batchResponse);
				}
			}
		}			
	}
	
	private void sendAuthorizationError(HttpServletResponse response) {
		if (SecurityUtils.getUser() != null) {
			sendBatchError(response, SC_FORBIDDEN, "Permission denied");
		} else {
			response.addHeader("LFS-Authenticate", "Basic realm=\"OneDev\"");
			sendBatchError(response, SC_UNAUTHORIZED, "Authentication required");
		}
	}
	
	private void writeTo(HttpServletResponse response, Object object) {
		try (OutputStream os = response.getOutputStream()) {
			os.write(objectMapper.writeValueAsBytes(object));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Map<Object, Object> toMap(GitLfsLock lock) {
		return newHashMap(
				"id", String.valueOf(lock.getId()), 
				"path", lock.getPath(),
				"locked_at", new SimpleDateFormat(DATETIME_FORMAT).format(lock.getDate()),
				"owner", newHashMap(
						"name", lock.getOwner().getDisplayName()));
	}

	private Map<Object, Object> getActionResponse(HttpServletRequest request, ProjectFacade project, 
												  String objectId, @Nullable String accessToken) {
		Map<Object, Object> actionResponse = newHashMap(
				"href", getObjectUrl(request, project.getPath(), objectId));
		if (accessToken != null) {
			actionResponse.put(
					"header", newHashMap(
							"Authorization", BEARER + " " + accessToken));
		}
		return actionResponse;
	}
	
	private Map<String, Object> getObjectResponse(HttpServletRequest request, ProjectFacade project, 
												  boolean upload, String objectId, long objectSize, 
												  @Nullable String accessToken) {
		Map<String, Object> objectResponse = new HashMap<>();
		objectResponse.put("oid", objectId);
		objectResponse.put("size", objectSize);
		LfsObject lfsObject = new LfsObject(project.getId(), objectId);
		if (objectSize > getMaxLFSFileSize()) {
			objectResponse.put("error", newHashMap(
					"code", SC_NOT_ACCEPTABLE, 
					"message", "Exceeded max acceptable LFS file size " + getMaxLFSFileSize()));
		} else if (upload) {
			if (!lfsObject.exists()) {
				objectResponse.put(
						"actions", newHashMap(
								"upload", getActionResponse(request, project, objectId, accessToken)));
			}
		} else if (lfsObject.exists()) {
			objectResponse.put(
					"actions", newHashMap(
							"download", getActionResponse(request, project, objectId, accessToken)));
		} else {
			objectResponse.put("error", newHashMap(
					"code", SC_NOT_FOUND, 
					"message", "Object not found"));
		}
		return objectResponse;
	}
	
	private void sendBatchError(HttpServletResponse response, int statusCode, String errorMessage) {
		response.setContentType(CONTENT_TYPE);
		response.setStatus(statusCode);
		Map<String, String> batchResponse = new HashMap<>();
		batchResponse.put("message", errorMessage);
		writeTo(response, batchResponse);
	}

	@Override
	public void destroy() {
	}

}
