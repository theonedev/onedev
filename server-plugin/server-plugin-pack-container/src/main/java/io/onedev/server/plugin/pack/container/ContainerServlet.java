package io.onedev.server.plugin.pack.container;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.*;
import io.onedev.server.exception.ChallengeAwareUnauthenticatedException;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackVersion;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackServlet;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.Digest;
import io.onedev.server.util.Pair;
import io.onedev.server.util.UrlUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;

import static io.onedev.commons.bootstrap.Bootstrap.BUFFER_SIZE;
import static io.onedev.server.model.PackVersion.MAX_DATA_LEN;
import static io.onedev.server.plugin.pack.container.ContainerPackSupport.TYPE;
import static io.onedev.server.util.Digest.SHA256;
import static java.lang.Long.parseLong;
import static java.util.regex.Pattern.compile;
import static javax.servlet.http.HttpServletResponse.*;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Singleton
public class ContainerServlet extends PackServlet {
	
	public static final String PATH = "/v2";

	private final SettingManager settingManager;
	
	private final SessionManager sessionManager;
	
	private final UserManager userManager;

	private final ProjectManager projectManager;
	
	private final PackBlobManager packBlobManager;
	
	private final PackVersionManager packVersionManager;
	
	@Inject
	public ContainerServlet(SettingManager settingManager, JobManager jobManager, 
							BuildManager buildManager, ObjectMapper objectMapper, 
							SessionManager sessionManager, UserManager userManager, 
							ProjectManager projectManager, PackBlobManager packBlobManager, 
							PackVersionManager packVersionManager) {
		super(jobManager, buildManager, objectMapper);
		this.settingManager = settingManager;
		this.sessionManager = sessionManager;
		this.userManager = userManager;
		this.projectManager = projectManager;
		this.packBlobManager = packBlobManager;
		this.packVersionManager = packVersionManager;
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		try {
			var method = request.getMethod();
			var pathInfo = request.getPathInfo();
			if (pathInfo == null)
				pathInfo = "";
			else
				pathInfo = StringUtils.strip(pathInfo, "/");

			String possibleJobToken;
			Long userId = null;
			var auth = request.getHeader("Authorization");
			if (auth != null && auth.startsWith("Bearer ")) {
				var bearerAuth = auth.substring("Bearer ".length());
				possibleJobToken = substringBefore(bearerAuth, ":");
				var accessToken = substringAfter(bearerAuth, ":");
				var user = userManager.findByAccessToken(accessToken);
				if (user != null)
					userId = user.getId();
			}

			if (userId != null)
				SecurityUtils.getSubject().runAs(SecurityUtils.asPrincipal(userId));
			try {
				Matcher matcher;
				if (pathInfo.equals("")) {
					if (SecurityUtils.getUserId().equals(0L))
						throw new ChallengeAwareUnauthenticatedException(getChallenge(request), "Please login");
					else
						response.setStatus(SC_OK);
				} else if (pathInfo.equals("token")) {
					var jsonObj = new HashMap<String, String>();
					String accessToken;
					var user = SecurityUtils.getUser();
					if (user != null)
						accessToken = userManager.createTemporalAccessToken(user.getId(), 3600);
					else
						accessToken = CryptoUtils.generateSecret();
					jsonObj.put("token", getPossibleJobToken(request) + ":" + accessToken);
					sendResponse(response, SC_OK, jsonObj);
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
							if (packBlobManager.finishUpload(projectId, uuid, digest.getHash())) {
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
									packBlobManager.delete(packBlob);
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
							var contentType = request.getContentType();
							if (contentType == null)
								throw new ClientException(SC_BAD_REQUEST, ErrorCode.MANIFEST_INVALID, "No content type specified");
							try (var is = request.getInputStream()) {
								if (copyLarge(is, baos, 0, MAX_DATA_LEN, new byte[BUFFER_SIZE]) >= MAX_DATA_LEN) 
									throw new ClientException(SC_BAD_REQUEST, ErrorCode.SIZE_INVALID, "Manifest is too large");									
							}
							sessionManager.run(() -> {
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

								var data = new ContainerData(bytes, contentType);
								var manifest = data.getManifest();
								var project = projectManager.load(projectId);
								Collection<PackBlob> packBlobs = new HashSet<>();

								if (data.isImageManifest()) {
									var blobNodes = new ArrayList<JsonNode>();
									blobNodes.add(manifest.get("config"));
									for (var layerNode : manifest.get("layers"))
										blobNodes.add(layerNode);

									for (var blobNode : blobNodes) {
										var digest = parseDigest(blobNode.get("digest").asText());
										var size = blobNode.get("size").asLong();
										var packBlob = packBlobManager.find(digest.getHash());
										if (packBlob != null && SecurityUtils.canReadPackBlob(packBlob)) {
											if (size != packBlob.getSize())
												throw new ClientException(SC_BAD_REQUEST, ErrorCode.SIZE_INVALID);
											packBlobs.add(packBlob);
										} else {
											throw new ClientException(SC_BAD_REQUEST, ErrorCode.MANIFEST_BLOB_UNKNOWN);
										}
									}
								} else if (data.isImageIndex()) {
									for (var manifestNode : manifest.get("manifests")) {
										var digest = parseDigest(manifestNode.get("digest").asText());
										var size = manifestNode.get("size").asLong();
										var packVersion = packVersionManager.findByDataHash(project, TYPE, digest.getHash());
										if (packVersion == null)
											throw new ClientException(SC_BAD_REQUEST, ErrorCode.MANIFEST_BLOB_UNKNOWN);
										else if (packVersion.getDataBytes().length != size)
											throw new ClientException(SC_BAD_REQUEST, ErrorCode.SIZE_INVALID);
									}
								}

								var packVersion = packVersionManager.findByName(project, TYPE, reference);
								if (packVersion == null) {
									packVersion = new PackVersion();
									packVersion.setProject(project);
									packVersion.setType(TYPE);
									packVersion.setName(reference);
								}
								packVersion.setDataBytes(bytes);
								packVersion.setDataHash(hash);
								packVersion.setExtraInfo(contentType);

								packVersionManager.createOrUpdate(packVersion, packBlobs);

								response.setStatus(SC_OK);
								response.setHeader("Docker-Content-Digest",
										"sha256:" + packVersion.getDataHash());
								response.setHeader("Location", getManifestUrl(projectPath, reference));
							});
							break;
						case "GET":
						case "HEAD":
							var versionInfo = sessionManager.call(() -> {
								var project = getProject(projectPath, false);
								PackVersion packVersion;
								if (isTag(reference))
									packVersion = packVersionManager.findByName(project, TYPE, reference);
								else
									packVersion = packVersionManager.findByDataHash(project, TYPE, parseDigest(reference).getHash());
								if (packVersion != null)
									return new ImmutableTriple<>(packVersion.getDataBytes(), packVersion.getDataHash(), packVersion.getExtraInfo());
								else
									return null;
							});
							if (versionInfo != null) {
								response.setStatus(SC_OK);
								response.setContentType(versionInfo.getRight());
								response.setHeader("Docker-Content-Digest",
										"sha256:" + versionInfo.getMiddle());
								response.setContentLength(versionInfo.getLeft().length);
								if (method.equals("GET")) {
									try (var os = response.getOutputStream()) {
										copy(new ByteArrayInputStream(versionInfo.getLeft()), os, BUFFER_SIZE);
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
									packVersionManager.deleteByName(project, TYPE, reference);
								else 
									packVersionManager.deleteByDataHash(project, TYPE, parseDigest(reference).getHash());									
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
							tags.addAll(packVersionManager.queryTags(project, TYPE, lastTag, count));
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
			} finally {
				if (userId != null)
					SecurityUtils.getSubject().releaseRunAs();
			}
		} catch (ClientException | UnauthenticatedException | UnauthorizedException e) {
			throw e;
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
		else if (needsToPush && !SecurityUtils.canWritePack(project))
			throw new UnauthorizedException("Not authorized to push to project: " + projectPath);
		else if (!needsToPush && !SecurityUtils.canReadPack(project))
			throw new UnauthorizedException("Not authorized to pull from project: " + projectPath);
		else
			return project;
	}

	private String getChallenge(HttpServletRequest request) {
		var serverUrl = settingManager.getSystemSetting().getServerUrl();
		try {
			var host = new URL(serverUrl).getHost();
			if (host.equals("localhost") || host.equals("127.0.0.1"))
				serverUrl = UrlUtils.getRootUrl(request.getRequestURL().toString());
			return "Bearer realm=\"" + serverUrl + "/v2/token\",service=\"onedev\",scope=\"*\"";
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
}
