package io.onedev.server.plugin.pack.container;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.service.*;
import io.onedev.server.exception.DataTooLargeException;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.Digest;
import io.onedev.server.util.HttpUtils;
import io.onedev.server.util.Pair;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

import static io.onedev.server.plugin.pack.container.ContainerAuthenticationFilter.ATTR_BUILD_ID;
import static io.onedev.server.plugin.pack.container.ContainerManifest.isImageIndex;
import static io.onedev.server.plugin.pack.container.ContainerManifest.isImageManifest;
import static io.onedev.server.plugin.pack.container.ContainerPackSupport.TYPE;
import static io.onedev.server.util.Digest.SHA256;
import static io.onedev.server.util.IOUtils.copyWithMaxSize;
import static java.lang.Long.parseLong;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Pattern.compile;
import static javax.servlet.http.HttpServletResponse.*;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Singleton
public class ContainerServlet extends HttpServlet {
	
	public static final String PATH = "/v2";
	
	private static final int MAX_MANIFEST_SIZE = 10000000;

	private final SettingService settingService;
	
	private final SessionService sessionService;
	
	private final ProjectService projectService;
	
	private final PackBlobService packBlobService;
	
	private final PackService packService;
	
	private final BuildService buildService;
	
	private final ObjectMapper objectMapper;
	
	@Inject
	public ContainerServlet(SettingService settingService, BuildService buildService,
                            ObjectMapper objectMapper, SessionService sessionService,
                            ProjectService projectService, PackBlobService packBlobService,
                            PackService packService) {
		this.settingService = settingService;
		this.sessionService = sessionService;
		this.projectService = projectService;
		this.packBlobService = packBlobService;
		this.packService = packService;
		this.objectMapper = objectMapper;
		this.buildService = buildService;
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) 
			throws IOException {
		response.setHeader("Docker-Distribution-Api-Version", "registry/2.0");
					
		var method = request.getMethod();
		var pathInfo = request.getPathInfo();
		if (pathInfo == null)
			pathInfo = "";
		else
			pathInfo = StringUtils.strip(pathInfo, "/");
		
		try {
			Matcher matcher;
			if (pathInfo.equals("")) {
				if (SecurityUtils.isAnonymous()) {
					response.setStatus(SC_UNAUTHORIZED);
					response.setHeader("WWW-Authenticate", getChallenge());
				} else {
					response.setStatus(SC_OK);
				}
			} else if (pathInfo.equals("token")) {
				if (method.equals("GET")) {
					var jsonObj = new HashMap<String, String>();
					String accessTokenValue = SecurityUtils.createTemporalAccessTokenIfUserPrincipal(3600);
					if (accessTokenValue == null)
						accessTokenValue = CryptoUtils.generateSecret();
					var jobToken = HttpUtils.getAuthBasicUser(request);
					if (jobToken == null)
						jobToken = UUID.randomUUID().toString();
					jsonObj.put("token", jobToken + ":" + accessTokenValue);

					response.setStatus(SC_OK);
					try {
						response.getOutputStream().write(objectMapper.writeValueAsString(jsonObj).getBytes(UTF_8));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					throw new ClientException(SC_METHOD_NOT_ALLOWED, ErrorCode.UNSUPPORTED);
				}
			} else if ((matcher = compile("(.+)/([^/]+)/blobs/uploads").matcher(pathInfo)).matches()) {
				var projectPath = matcher.group(1);
				var repository = matcher.group(2);
				var digestString = request.getParameter("mount");
				sessionService.run(() -> {
					var project = checkProject(projectPath, true);
					if (digestString != null) {
						var hash = parseDigest(digestString).getHash();
						if (packBlobService.checkPackBlob(project.getId(), hash) != null) {
							response.setStatus(SC_CREATED);
							response.setHeader("Location", getBlobUrl(projectPath, repository, digestString));
						}
					}
					if (response.getStatus() != SC_CREATED) {
						response.setStatus(SC_ACCEPTED);
						var uuid = UUID.randomUUID().toString();
						packBlobService.initUpload(project.getId(), uuid);
						response.setHeader("Location", getUploadUrl(projectPath, repository, uuid));
						response.setHeader("Content-Length", "0");
						response.setHeader("Range", "0-0");
						response.setHeader("Docker-Upload-UUID", uuid);
					}
				});
			} else if ((matcher = compile("(.+)/([^/]+)/blobs/uploads/([^/]+)").matcher(pathInfo)).matches()) {
				var projectPath = matcher.group(1);
				var repository = matcher.group(2);
				var uuid = matcher.group(3);
				response.setHeader("Location", getUploadUrl(projectPath, repository, uuid));
				response.setHeader("Docker-Upload-UUID", uuid);
				var projectId = sessionService.call(() -> checkProject(projectPath, true).getId());
				switch (method) {
					case "PATCH": {
						var uploadedSize = packBlobService.getUploadFileSize(projectId, uuid);
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
							uploadedSize += packBlobService.uploadBlob(projectId, uuid, is);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						response.setStatus(SC_ACCEPTED);
						response.setHeader("Range", "0-" + (uploadedSize - 1));
						break;
					}
					case "PUT": {
						if (packBlobService.getUploadFileSize(projectId, uuid) == -1)
							throw new NotFoundException(ErrorCode.BLOB_UPLOAD_UNKNOWN);
						var contentLength = request.getHeader("Content-Length");
						if (contentLength != null) {
							var parsedContentLength = parseLong(contentLength);
							if (parsedContentLength != 0) {
								try (var is = request.getInputStream()) {
									packBlobService.uploadBlob(projectId, uuid, is);
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
						if (packBlobService.finishUpload(projectId, uuid, digest.getHash()) != null) {
							response.setStatus(SC_CREATED);
							response.setHeader("Location", getBlobUrl(projectPath, repository, digestString));
							response.setHeader("Docker-Content-Digest", digestString);
						} else {
							throw new ClientException(SC_BAD_REQUEST, ErrorCode.DIGEST_INVALID,
									"Invalid blob digest");
						}
						break;
					}
					case "GET": {
						var uploadedSize = packBlobService.getUploadFileSize(projectId, uuid);
						if (uploadedSize == -1)
							throw new NotFoundException(ErrorCode.BLOB_UPLOAD_UNKNOWN);
						response.setStatus(SC_NO_CONTENT);
						response.setHeader("Range", "0-" + (uploadedSize - 1));
						response.setHeader("Docker-Upload-UUID", uuid);
						break;
					}
					case "DELETE": {
						packBlobService.cancelUpload(projectId, uuid);
						response.setStatus(SC_NO_CONTENT);
						break;
					}
					default: {
						throw new BadRequestException("Invalid http method for blob upload: " + method);
					}
				}
			} else if ((matcher = compile("(.+)/([^/]+)/blobs/([^/]+)").matcher(pathInfo)).matches()) {
				var projectPath = matcher.group(1);
				var digestString = matcher.group(3);
				if (method.equals("GET") || method.equals("HEAD")) {
					var packBlobInfo = sessionService.call(() -> {
						var project = checkProject(projectPath, false);
						var digest = parseDigest(digestString);
						var hash = digest.getHash();
						PackBlob packBlob;
						if ((packBlob = packBlobService.checkPackBlob(project.getId(), hash)) != null) {
							response.setStatus(SC_OK);	
							response.setHeader("Content-Length", String.valueOf(packBlob.getSize()));
							response.setHeader("Docker-Content-Digest", digestString);
							return new Pair<>(packBlob.getProject().getId(), packBlob.getSha256Hash());
						} else {
							throw new NotFoundException(ErrorCode.BLOB_UNKNOWN);
						}
					});
					if (method.equals("GET")) {
						packBlobService.downloadBlob(packBlobInfo.getLeft(), packBlobInfo.getRight(),
								response.getOutputStream());
					}
				} else if (method.equals("DELETE")) {
					throw new ClientException(SC_METHOD_NOT_ALLOWED, ErrorCode.UNSUPPORTED);
				} else {
					throw new BadRequestException("Invalid http method for blob pull: " + method);
				}
			} else if ((matcher = compile("(.+)/([^/]+)/manifests/([^/]+)").matcher(pathInfo)).matches()) {
				var projectPath = matcher.group(1);
				var repository = matcher.group(2);
				var reference = matcher.group(3);
				switch (method) {
					case "PUT":
						var projectId = sessionService.call(() -> checkProject(projectPath, true).getId());
						var baos = new ByteArrayOutputStream();
						try (var is = request.getInputStream()) {
							copyWithMaxSize(is, baos, MAX_MANIFEST_SIZE);
						} catch (DataTooLargeException e) {
							throw new ClientException(SC_BAD_REQUEST, ErrorCode.SIZE_INVALID, "Manifest is too large");
						}

						var bytes = baos.toByteArray();
						String hash;
						if (isTag(reference)) {
							var packBlobId = packBlobService.uploadBlob(projectId, bytes, null);
							// Do not use lamda here as it may cause compilation error on terminal
							hash = LockUtils.call(getLockName(projectId, repository), new Callable<String>() {
								@Override
								public String call() {
									return sessionService.call(new Callable<>() {

										private PackBlob loadPackBlob(Map<String, PackBlob> packBlobs, String hash, long size) {
											var packBlob = packBlobs.get(hash);
											if (packBlob == null) {
												packBlob = packBlobService.findBySha256Hash(projectId, hash);
												if (packBlob != null) {
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

										private void loadReferencedPackBlobs(Map<String, PackBlob> packBlobs, byte[] bytes) {
											var manifest = new ContainerManifest(bytes);
											if (manifest.isImageIndex()) {
												for (var manifestNode : manifest.getJson().get("manifests")) {
													var hash = parseDigest(manifestNode.get("digest").asText()).getHash();
													var size = manifestNode.get("size").asLong();
													var packBlob = loadPackBlob(packBlobs, hash, size);
													var mediaType = manifestNode.get("mediaType").asText();
													if (isImageIndex(mediaType) || isImageManifest(mediaType)) {
														var baos = new ByteArrayOutputStream();
														packBlobService.downloadBlob(packBlob.getProject().getId(), hash, baos);
														loadReferencedPackBlobs(packBlobs, baos.toByteArray());
													}
												}
											} else if (manifest.isImageManifest()) {
												var blobNodes = new ArrayList<JsonNode>();
												blobNodes.add(manifest.getJson().get("config"));
												for (var layerNode : manifest.getJson().get("layers"))
													blobNodes.add(layerNode);
												for (var blobNode : blobNodes) {
													var hash = parseDigest(blobNode.get("digest").asText()).getHash();
													var size = blobNode.get("size").asLong();
													loadPackBlob(packBlobs, hash, size);
												}
											}
										}

										@Override
										public String call() {
											var packBlobs = new HashMap<String, PackBlob>();
											loadReferencedPackBlobs(packBlobs, bytes);
											var packBlob = packBlobService.load(packBlobId);
											packBlobs.put(packBlob.getSha256Hash(), packBlob);

											var project = projectService.load(projectId);
											var pack = packService.findByNameAndVersion(project, TYPE, repository, reference);
											if (pack == null) {
												pack = new Pack();
												pack.setProject(project);
												pack.setType(TYPE);
												pack.setName(repository);
												pack.setVersion(reference);
											}

											Build build = null;
											Long buildId = (Long) request.getAttribute(ATTR_BUILD_ID);
											if (buildId != null)
												build = buildService.load(buildId);
											pack.setBuild(build);
											pack.setUser(SecurityUtils.getUser());
											pack.setData(packBlob.getSha256Hash());
											pack.setPublishDate(new Date());

											packService.createOrUpdate(pack, packBlobs.values(), true);

											return packBlob.getSha256Hash();
										}
									});
								}
							});
						} else {
							hash = parseDigest(reference).getHash();
							if (packBlobService.uploadBlob(projectId, bytes, hash) == null) {
								throw new ClientException(SC_BAD_REQUEST, ErrorCode.DIGEST_INVALID,
										"Invalid manifest digest");
							}
						}
						
						response.setStatus(SC_OK);
						response.setHeader("Docker-Content-Digest",
								"sha256:" + hash);
						response.setHeader("Location", getManifestUrl(projectPath, repository, reference));
						break;
					case "GET":
					case "HEAD":
						var manifestInfo = sessionService.call(() -> {
							PackBlob packBlob;
							var project = checkProject(projectPath, false);
							if (isTag(reference)) {
								var pack = packService.findByNameAndVersion(project, TYPE, repository, reference);
								if (pack != null)
									packBlob = packBlobService.findBySha256Hash(project.getId(), (String)pack.getData());
								else
									packBlob = null;
							} else {
								packBlob = packBlobService.findBySha256Hash(project.getId(), parseDigest(reference).getHash());
							}
							if (packBlob != null && packBlob.getSize() < MAX_MANIFEST_SIZE 
									&& packBlobService.checkPackBlobFile(packBlob.getProject().getId(), packBlob.getSha256Hash(), packBlob.getSize())) {
								response.setHeader("Docker-Content-Digest", "sha256:" + packBlob.getSha256Hash());
								response.setContentLengthLong(packBlob.getSize());
								return new Pair<>(packBlob.getProject().getId(), packBlob.getSha256Hash());
							} else {
								return null;
							}
						});
						if (manifestInfo != null) {
							response.setStatus(SC_OK);
							baos = new ByteArrayOutputStream();
							packBlobService.downloadBlob(manifestInfo.getLeft(), manifestInfo.getRight(), baos);
							bytes = baos.toByteArray();
							response.setContentType(new ContainerManifest(bytes).getMediaType());
							if (method.equals("GET")) 
								response.getOutputStream().write(bytes);
						} else {
							throw new NotFoundException(ErrorCode.MANIFEST_UNKNOWN);
						}
						break;
					case "DELETE":
						sessionService.run(() -> {
							var project = checkProject(projectPath, true);
							if (isTag(reference))
								packService.deleteByNameAndVersion(project, TYPE, repository, reference);
							response.setStatus(SC_ACCEPTED);
						});
						break;
					default:
						throw new BadRequestException("Invalid http method for manifest pull: " + method);
				}
			} else if ((matcher = compile("(.+)/([^/]+)/tags/list").matcher(pathInfo)).matches()) {
				var projectPath = matcher.group(1);
				var repository = matcher.group(2);
				sessionService.run(() -> {
					var project = checkProject(projectPath, false);

					var result = new HashMap<String, Object>();
					result.put("name", projectPath + "/" + repository);
					var tags = new ArrayList<String>();
					result.put("tags", tags);

					int count = Integer.MAX_VALUE;
					var countParam = request.getParameter("n");
					if (countParam != null)
						count = Integer.parseInt(countParam);
					if (count != 0) {
						var lastTag = request.getParameter("last");
						tags.addAll(packService.queryVersions(project, TYPE, repository, lastTag, count));
					}

					response.setStatus(SC_OK);
					try {
						response.getOutputStream().write(objectMapper.writeValueAsBytes(result));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			} else {
				throw new NotFoundException(ErrorCode.UNSUPPORTED);
			}
		} catch (ClientException e) {
			throw e;
		} catch (UnauthorizedException e) {
			if (!SecurityUtils.isAnonymous()) {
				throw new ClientException(SC_FORBIDDEN, ErrorCode.UNAUTHORIZED, e.getMessage());
			} else {
				response.setStatus(SC_UNAUTHORIZED);
				response.setHeader("WWW-Authenticate", getChallenge());
			}
		} catch (Exception e) {
			var httpResponse = ExceptionUtils.buildResponse(e);
			if (httpResponse != null) {
				var statusCode = httpResponse.getStatus();
				if (statusCode >= 400 && statusCode < 500) {
					throw new ClientException(statusCode, ErrorCode.DENIED,
							httpResponse.getBody() != null? httpResponse.getBody().getText(): HttpStatus.getMessage(httpResponse.getStatus()));
				}
			}
			throw e;
		} 
	}

	private String getLockName(Long projectId, String repository) {
		return "update-pack:" + projectId + ":" + TYPE + ":" + repository;
	}
	
	private String getUploadUrl(String projectPath, String repository, String uuid) {
		return "/v2/" + projectPath + "/" + repository + "/blobs/uploads/" + uuid;		
	}
	
	private String getBlobUrl(String projectPath, String repository, String digest) {
		return "/v2/" + projectPath + "/" + repository + "/blobs/" + digest;
	}

	private String getManifestUrl(String projectPath, String repository, String reference) {
		return "/v2/" + projectPath + "/" + repository + "/manifests/" + reference;
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
	
	private Project checkProject(String projectPath, boolean needsToPush) {
		var project = projectService.findByPath(projectPath);
		if (project == null) 
			throw new NotFoundException(ErrorCode.NAME_UNKNOWN, "Unknown project: " + projectPath);
		else if (!project.isPackManagement())
			throw new ClientException(SC_NOT_ACCEPTABLE, ErrorCode.DENIED, "Package management not enabled for project: " + projectPath);
		else if (needsToPush && !SecurityUtils.canWritePack(project))
			throw new UnauthorizedException("No package write permission for project: " + project.getPath());
		else if (!needsToPush && !SecurityUtils.canReadPack(project))
			throw new UnauthorizedException("No package read permission for project: " + project.getPath());
		else
			return project;
	}

	private String getChallenge() {
		var serverUrl = settingService.getSystemSetting().getServerUrl();
		return "Bearer realm=\"" + serverUrl + "/v2/token\",service=\"onedev\",scope=\"*\"";
	}
	
}
