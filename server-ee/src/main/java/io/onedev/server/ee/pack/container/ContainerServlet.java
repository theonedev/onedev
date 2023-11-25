package io.onedev.server.ee.pack.container;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.entitymanager.*;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.Digest;
import io.onedev.server.util.HttpUtils;
import io.onedev.server.util.Pair;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

import static io.onedev.commons.bootstrap.Bootstrap.BUFFER_SIZE;
import static io.onedev.server.ee.pack.container.ContainerAuthenticationFilter.ATTR_JOB_TOKEN;
import static io.onedev.server.ee.pack.container.ContainerPackSupport.TYPE;
import static io.onedev.server.model.Pack.MAX_DATA_LEN;
import static io.onedev.server.util.Digest.SHA256;
import static java.lang.Long.parseLong;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Pattern.compile;
import static javax.servlet.http.HttpServletResponse.*;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Singleton
public class ContainerServlet extends HttpServlet {
	
	public static final String PATH = "/v2";

	private final SettingManager settingManager;
	
	private final SessionManager sessionManager;
	
	private final UserManager userManager;

	private final ProjectManager projectManager;
	
	private final PackBlobManager packBlobManager;
	
	private final PackManager packManager;
	
	private final JobManager jobManager;
	
	private final BuildManager buildManager;
	
	private final ObjectMapper objectMapper;
	
	private final SubscriptionManager subscriptionManager;
	
	@Inject
	public ContainerServlet(SettingManager settingManager, JobManager jobManager, 
							BuildManager buildManager, ObjectMapper objectMapper, 
							SessionManager sessionManager, UserManager userManager, 
							ProjectManager projectManager, PackBlobManager packBlobManager, 
							PackManager packManager, SubscriptionManager subscriptionManager) {
		this.settingManager = settingManager;
		this.sessionManager = sessionManager;
		this.userManager = userManager;
		this.projectManager = projectManager;
		this.packBlobManager = packBlobManager;
		this.packManager = packManager;
		this.objectMapper = objectMapper;
		this.jobManager = jobManager;
		this.buildManager = buildManager;
		this.subscriptionManager = subscriptionManager;
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		if (!subscriptionManager.isSubscriptionActive()) {
			throw new ClientException(SC_NOT_ACCEPTABLE, ErrorCode.DENIED, 
					"This feature requires an active subscription. Visit https://onedev.io/pricing to get a 30 days free trial");
		}
					
		var method = request.getMethod();
		var pathInfo = request.getPathInfo();
		if (pathInfo == null)
			pathInfo = "";
		else
			pathInfo = StringUtils.strip(pathInfo, "/");
		
		try {
			Matcher matcher;
			if (pathInfo.equals("")) {
				if (SecurityUtils.getUserId().equals(0L)) {
					response.setStatus(SC_UNAUTHORIZED);
					response.setHeader("WWW-Authenticate", getChallenge(request));
				} else {
					response.setStatus(SC_OK);
				}
			} else if (pathInfo.equals("token")) {
				var jsonObj = new HashMap<String, String>();
				String accessToken;
				var userId = SecurityUtils.getUserId();
				if (!userId.equals(0L))
					accessToken = userManager.createTemporalAccessToken(userId, 3600);
				else
					accessToken = CryptoUtils.generateSecret();
				
				var jobToken = HttpUtils.getAuthBasicUser(request);
				if (jobToken == null)
					jobToken = UUID.randomUUID().toString();
				jsonObj.put("token", jobToken + ":" + accessToken);
				
				response.setStatus(SC_OK);
				try (var out = response.getOutputStream()) {
					out.write(objectMapper.writeValueAsString(jsonObj).getBytes(UTF_8));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else if ((matcher = compile("(.+)/blobs/uploads").matcher(pathInfo)).matches()) {
				var projectPath = matcher.group(1);
				var digestString = request.getParameter("mount");
				sessionManager.run(() -> {
					var project = getProject(projectPath, true);
					if (digestString != null) {
						var hash = parseDigest(digestString).getHash();
						var packBlob = packBlobManager.find(hash);
						if (packBlob != null && SecurityUtils.canReadPackBlob(packBlob)) {
							if (packBlobManager.checkPackBlobFile(packBlob.getProject().getId(), hash, packBlob.getSize())) {
								response.setStatus(SC_CREATED);
								response.setHeader("Location", getBlobUrl(projectPath, digestString));
							} else {
								packBlobManager.delete(packBlob);
							}
						}
					}
					if (response.getStatus() != SC_CREATED) {
						response.setStatus(SC_ACCEPTED);
						var uuid = UUID.randomUUID().toString();
						packBlobManager.initUpload(project.getId(), uuid);
						response.setHeader("Location", getUploadUrl(projectPath, uuid));
						response.setHeader("Content-Length", "0");
						response.setHeader("Range", "0-0");
						response.setHeader("Docker-Upload-UUID", uuid);
					}
				});
			} else if ((matcher = compile("(.+)/blobs/uploads/([^/]+)").matcher(pathInfo)).matches()) {
				var projectPath = matcher.group(1);
				var uuid = matcher.group(2);
				response.setHeader("Location", getUploadUrl(projectPath, uuid));
				response.setHeader("Docker-Upload-UUID", uuid);
				var projectId = sessionManager.call(() -> getProject(projectPath, true).getId());
				switch (method) {
					case "PATCH": {
						var uploadedSize = packBlobManager.getUploadFileSize(projectId, uuid);
						if (uploadedSize == -1)
							throw new NotFoundException(ErrorCode.BLOB_UPLOAD_UNKNOWN);
						var contentRange = request.getHeader("Content-Range");
						if (contentRange != null) {
							var chunkBegin = parseLong(substringBefore(contentRange, "-"));
							if (uploadedSize != chunkBegin) {
								response.setHeader("Range", "0-" + (uploadedSize - 1));
								throw new ClientException(SC_REQUESTED_RANGE_NOT_SATISFIABLE,
										ErrorCode.BLOB_UPLOAD_INVALID, "Invalid chunk range");
							}
						} else if (uploadedSize != 0) {
							throw new ClientException(SC_REQUESTED_RANGE_NOT_SATISFIABLE, ErrorCode.BLOB_UPLOAD_INVALID,
									"Content range header expected after first upload");
						}
						try (var is = request.getInputStream()) {
							uploadedSize += packBlobManager.uploadBlob(projectId, uuid, is);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						response.setStatus(SC_ACCEPTED);
						response.setHeader("Range", "0-" + (uploadedSize - 1));
						break;
					}
					case "PUT": {
						if (packBlobManager.getUploadFileSize(projectId, uuid) == -1)
							throw new NotFoundException(ErrorCode.BLOB_UPLOAD_UNKNOWN);
						var contentLength = request.getHeader("Content-Length");
						if (contentLength != null) {
							var parsedContentLength = parseLong(contentLength);
							if (parsedContentLength != 0) {
								try (var is = request.getInputStream()) {
									packBlobManager.uploadBlob(projectId, uuid, is);
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
						}
						var digestString = request.getParameter("digest");
						if (digestString == null) {
							throw new ClientException(SC_BAD_REQUEST, ErrorCode.DIGEST_INVALID,
									"Digest expected to finish blob upload");
						}

						var digest = parseDigest(digestString);
						if (packBlobManager.finishUpload(projectId, uuid, digest.getHash()) != null) {
							response.setStatus(SC_CREATED);
							response.setHeader("Location", getBlobUrl(projectPath, digestString));
							response.setHeader("Docker-Content-Digest", digestString);
						} else {
							throw new ClientException(SC_BAD_REQUEST, ErrorCode.DIGEST_INVALID,
									"Invalid blob digest");
						}
						break;
					}
					case "GET": {
						var uploadedSize = packBlobManager.getUploadFileSize(projectId, uuid);
						if (uploadedSize == -1)
							throw new NotFoundException(ErrorCode.BLOB_UPLOAD_UNKNOWN);
						response.setStatus(SC_NO_CONTENT);
						response.setHeader("Range", "0-" + (uploadedSize - 1));
						response.setHeader("Docker-Upload-UUID", uuid);
						break;
					}
					case "DELETE": {
						packBlobManager.cancelUpload(projectId, uuid);
						response.setStatus(SC_NO_CONTENT);
						break;
					}
					default: {
						throw new BadRequestException("Invalid http method for blob upload: " + method);
					}
				}
			} else if ((matcher = compile("(.+)/blobs/([^/]+)").matcher(pathInfo)).matches()) {
				var projectPath = matcher.group(1);
				var digestString = matcher.group(2);
				if (method.equals("GET") || method.equals("HEAD")) {
					var packBlobInfo = sessionManager.call(() -> {
						getProject(projectPath, false);
						var digest = parseDigest(digestString);
						var hash = digest.getHash();
						var packBlob = packBlobManager.find(hash);
						if (packBlob != null && SecurityUtils.canReadPackBlob(packBlob)) {
							if (packBlobManager.checkPackBlobFile(packBlob.getProject().getId(), hash, packBlob.getSize())) {
								response.setStatus(SC_OK);
								response.setHeader("Content-Length", String.valueOf(packBlob.getSize()));
								response.setHeader("Docker-Content-Digest", digestString);
								return new Pair<>(packBlob.getProject().getId(), packBlob.getHash());
							} else {
								throw new NotFoundException(ErrorCode.BLOB_UNKNOWN);
							}
						} else {
							throw new NotFoundException(ErrorCode.BLOB_UNKNOWN);
						}
					});
					if (method.equals("GET")) {
						try (var os = response.getOutputStream()) {
							packBlobManager.downloadBlob(packBlobInfo.getLeft(), packBlobInfo.getRight(), os);
						}
					}
				} else if (method.equals("DELETE")) {
					throw new ClientException(SC_METHOD_NOT_ALLOWED, ErrorCode.UNSUPPORTED);
				} else {
					throw new BadRequestException("Invalid http method for blob pull: " + method);
				}
			} else if ((matcher = compile("(.+)/manifests/([^/]+)").matcher(pathInfo)).matches()) {
				var projectPath = matcher.group(1);
				var reference = matcher.group(2);
				switch (method) {
					case "PUT":
						var projectId = sessionManager.call(() -> getProject(projectPath, true).getId());
						var baos = new ByteArrayOutputStream();
						try (var is = request.getInputStream()) {
							if (copyLarge(is, baos, 0, MAX_DATA_LEN, new byte[BUFFER_SIZE]) >= MAX_DATA_LEN)
								throw new ClientException(SC_BAD_REQUEST, ErrorCode.SIZE_INVALID, "Manifest is too large");
						}

						var bytes = baos.toByteArray();
						String hash;
						if (!isTag(reference)) {
							var digest = parseDigest(reference);
							if (!digest.matches(bytes)) {
								throw new ClientException(SC_BAD_REQUEST, ErrorCode.DIGEST_INVALID,
										"Invalid manifest digest");
							}
							hash = digest.getHash();
						} else {
							hash = Digest.sha256Of(bytes).getHash();
						}

						var packBlobId = packBlobManager.uploadBlob(projectId, bytes, hash);

						sessionManager.run(new Runnable() {

							private PackBlob loadPackBlob(Map<String, PackBlob> packBlobs, String hash, long size) {
								var packBlob = packBlobs.get(hash);
								if (packBlob == null) {
									packBlob = packBlobManager.find(hash);
									if (packBlob != null && SecurityUtils.canReadPackBlob(packBlob)) {
										if (packBlob.getSize() == size)
											packBlobs.put(hash, packBlob);
										else
											throw new ClientException(SC_BAD_REQUEST, ErrorCode.SIZE_INVALID);
									} else {
										throw new ClientException(SC_BAD_REQUEST, ErrorCode.MANIFEST_BLOB_UNKNOWN);
									}
								}
								return packBlob;
							}

							private void loadReferencedPackBlobs(Map<String, PackBlob> packBlobs,
																 byte[] bytes) {
								var data = new ContainerData(bytes);
								if (data.isImageIndex()) {
									for (var manifestNode : data.getManifest().get("manifests")) {
										var hash = parseDigest(manifestNode.get("digest").asText()).getHash();
										var size = manifestNode.get("size").asLong();
										var packBlob = loadPackBlob(packBlobs, hash, size);
										var baos = new ByteArrayOutputStream();
										packBlobManager.downloadBlob(packBlob.getProject().getId(), hash, baos);
										loadReferencedPackBlobs(packBlobs, baos.toByteArray());
									}
								} else if (data.isImageManifest()) {
									var blobNodes = new ArrayList<JsonNode>();
									blobNodes.add(data.getManifest().get("config"));
									for (var layerNode : data.getManifest().get("layers"))
										blobNodes.add(layerNode);
									for (var blobNode : blobNodes) {
										var hash = parseDigest(blobNode.get("digest").asText()).getHash();
										var size = blobNode.get("size").asLong();
										loadPackBlob(packBlobs, hash, size);
									}
								}
							}

							@Override
							public void run() {
								if (isTag(reference)) {
									var packBlobs = new HashMap<String, PackBlob>();
									loadReferencedPackBlobs(packBlobs, bytes);
									packBlobs.put(hash, packBlobManager.load(packBlobId));

									var project = projectManager.load(projectId);
									var pack = packManager.find(project, TYPE, reference);
									if (pack == null) {
										pack = new Pack();
										pack.setProject(project);
										pack.setType(TYPE);
										pack.setVersion(reference);
									}
									
									Build build = null;
									String jobToken = (String) request.getAttribute(ATTR_JOB_TOKEN);
									if (jobToken != null) {
										var jobContext = jobManager.getJobContext(jobToken, false);
										if (jobContext != null)
											build = buildManager.load(jobContext.getBuildId());
									}
									pack.setBuild(build);
									pack.setUser(SecurityUtils.getUser());
									pack.setBlobHash(hash);
									pack.setPublishDate(new Date());

									packManager.createOrUpdate(pack, packBlobs.values());
								}

								response.setStatus(SC_OK);
								response.setHeader("Docker-Content-Digest",
										"sha256:" + hash);
								response.setHeader("Location", getManifestUrl(projectPath, reference));
							}
						});
						break;
					case "GET":
					case "HEAD":
						var manifestInfo = sessionManager.call(() -> {
							PackBlob packBlob;
							var project = getProject(projectPath, false);
							if (isTag(reference)) {
								var pack = packManager.find(project, TYPE, reference);
								if (pack != null)
									packBlob = packBlobManager.find(pack.getBlobHash());
								else
									packBlob = null;
							} else {
								packBlob = packBlobManager.find(parseDigest(reference).getHash());
							}
							if (packBlob != null && SecurityUtils.canReadPackBlob(packBlob)
									&& packBlobManager.checkPackBlobFile(packBlob.getProject().getId(), packBlob.getHash(), packBlob.getSize())) {
								response.setHeader("Docker-Content-Digest", "sha256:" + packBlob.getHash());
								response.setContentLengthLong(packBlob.getSize());
								return new Pair<>(packBlob.getProject().getId(), packBlob.getHash());
							} else {
								return null;
							}
						});
						if (manifestInfo != null) {
							response.setStatus(SC_OK);
							baos = new ByteArrayOutputStream();
							packBlobManager.downloadBlob(manifestInfo.getLeft(), manifestInfo.getRight(), baos);
							bytes = baos.toByteArray();
							String manifest = new String(bytes);
							response.setContentType(new ContainerData(bytes).getMediaType());
							if (method.equals("GET")) {
								try (var os = response.getOutputStream()) {
									os.write(bytes);
								}
							}
						} else {
							throw new NotFoundException(ErrorCode.MANIFEST_UNKNOWN);
						}
						break;
					case "DELETE":
						sessionManager.run(() -> {
							var project = getProject(projectPath, true);
							if (isTag(reference))
								packManager.delete(project, TYPE, reference);
							response.setStatus(SC_ACCEPTED);
						});
						break;
					default:
						throw new BadRequestException("Invalid http method for manifest pull: " + method);
				}
			} else if ((matcher = compile("(.+)/tags/list").matcher(pathInfo)).matches()) {
				var projectPath = matcher.group(1);
				sessionManager.run(() -> {
					var project = getProject(projectPath, false);

					var result = new HashMap<String, Object>();
					result.put("name", projectPath);
					var tags = new ArrayList<String>();
					result.put("tags", tags);

					int count = Integer.MAX_VALUE;
					var countParam = request.getParameter("n");
					if (countParam != null)
						count = Integer.parseInt(countParam);
					if (count != 0) {
						var lastTag = request.getParameter("last");
						tags.addAll(packManager.queryTags(project, TYPE, lastTag, count));
					}

					response.setStatus(SC_OK);
					try (var os = response.getOutputStream()) {
						os.write(objectMapper.writeValueAsBytes(result));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			} else if (compile("(.+)/referrers/([^/]+)").matcher(pathInfo).matches()) {
				throw new NotFoundException(ErrorCode.UNSUPPORTED);
			} else {
				throw new BadRequestException("Invalid request url: " + request.getRequestURI());
			}
		} catch (ClientException e) {
			throw e;
		} catch (UnauthorizedException e) {
			if (!SecurityUtils.getUserId().equals(0L)) {
				throw new ClientException(SC_FORBIDDEN, ErrorCode.UNAUTHORIZED, e.getMessage());
			} else {
				response.setStatus(SC_UNAUTHORIZED);
				response.setHeader("WWW-Authenticate", getChallenge(request));
			}
		} catch (Exception e) {
			var httpResponse = ExceptionUtils.buildResponse(e);
			if (httpResponse != null) {
				var statusCode = httpResponse.getStatusCode();
				if (statusCode >= 400 && statusCode < 500) {
					throw new ClientException(statusCode, ErrorCode.DENIED,
							httpResponse.getResponseBody());
				}
			}
			throw e;
		} 
	}
	
	private String getUploadUrl(String projectPath, String uuid) {
		return "/v2/" + projectPath + "/blobs/uploads/" + uuid;		
	}
	
	private String getBlobUrl(String projectPath, String digest) {
		return "/v2/" + projectPath + "/blobs/" + digest;
	}

	private String getManifestUrl(String projectPath, String reference) {
		return "/v2/" + projectPath + "/manifests/" + reference;
	}
	
	private boolean isTag(String reference) {
		return !reference.contains(":");
	}
	
	private Digest parseDigest(String digestString) {
		if (digestString.startsWith("sha256:")) {
			return new Digest(SHA256, digestString.substring("sha256:".length()));
		} else {
			throw new ClientException(SC_NOT_ACCEPTABLE, ErrorCode.UNSUPPORTED, 
					"Unsupported digest: " + digestString);
		}
	}
	
	private Project getProject(String projectPath, boolean needsToPush) {
		var project = projectManager.findByPath(projectPath);
		if (project == null) 
			throw new NotFoundException(ErrorCode.NAME_UNKNOWN, "Unknown project: " + projectPath);
		else if (!project.isPackManagement())
			throw new ClientException(SC_NOT_ACCEPTABLE, ErrorCode.DENIED, "Package management not enabled for project: " + projectPath);
		else if (needsToPush && !SecurityUtils.canWritePack(project))
			throw new UnauthorizedException("Not authorized to push to project: " + projectPath);
		else if (!needsToPush && !SecurityUtils.canReadPack(project))
			throw new UnauthorizedException("Not authorized to pull from project: " + projectPath);
		else
			return project;
	}

	private String getChallenge(HttpServletRequest request) {
		var serverUrl = settingManager.getSystemSetting().getServerUrl();
		return "Bearer realm=\"" + serverUrl + "/v2/token\",service=\"onedev\",scope=\"*\"";
	}
	
}
