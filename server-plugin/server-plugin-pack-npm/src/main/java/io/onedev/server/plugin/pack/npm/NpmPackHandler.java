package io.onedev.server.plugin.pack.npm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.pack.PackHandler;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.service.PackService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.exception.DataTooLargeException;
import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Digest;
import io.onedev.server.web.UrlService;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.plugin.pack.npm.NpmPackSupport.TYPE;
import static io.onedev.server.util.IOUtils.copyWithMaxSize;
import static io.onedev.server.util.UrlUtils.decodePath;
import static io.onedev.server.util.UrlUtils.encodePath;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static javax.servlet.http.HttpServletResponse.*;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.commons.lang3.StringUtils.*;

@Singleton
public class NpmPackHandler implements PackHandler {
	
	public static final String HANDLER_ID = "npm";
	
	private static final int MAX_UPLOAD_METADATA_LEN = 10000000;
	
	private static final int MAX_TAG_BODY_LEN = 1000;
	
	private static final int MAX_QUERY_COUNT = 1000;

	private final SessionService sessionService;
	
	private final TransactionService transactionService;
	
	private final PackBlobService packBlobService;
	
	private final PackService packService;
	
	private final ProjectService projectService;
	
	private final BuildService buildService;
	
	private final ObjectMapper objectMapper;
	
	private final UrlService urlService;

	@Inject
	public NpmPackHandler(SessionService sessionService, TransactionService transactionService,
						  PackBlobService packBlobService, PackService packService,
						  ProjectService projectService, BuildService buildService,
						  ObjectMapper objectMapper, UrlService urlService) {
		this.sessionService = sessionService;
		this.transactionService = transactionService;
		this.packBlobService = packBlobService;
		this.packService = packService;
		this.projectService = projectService;
		this.buildService = buildService;
		this.objectMapper = objectMapper;
		this.urlService = urlService;
	}
	
	@Override
	public String getHandlerId() {
		return HANDLER_ID;
	}

	private String getLockName(Long projectId, String name) {
		return "update-pack:" + projectId + ":" + TYPE + ":" + name;
	}
	
	private ObjectNode readJson(byte[] jsonBytes) {
		try {
			return (ObjectNode) objectMapper.readTree(jsonBytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] writeJson(JsonNode json) {
		try {
			return objectMapper.writeValueAsBytes(json);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private byte[] decodeHex(String hexString) {
		try {
			return Hex.decodeHex(hexString);
		} catch (DecoderException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, Long projectId, 
						Long buildId, List<String> pathSegments) {
		var method = request.getMethod();
		
		var isGet = method.equals("GET");
		var isPut = method.equals("PUT");
		var isDelete = method.equals("DELETE");
		
		if (pathSegments.isEmpty())
			throw new ClientException(SC_BAD_REQUEST, "Invalid request path");
		
		var currentSegment = pathSegments.get(0);
		if (currentSegment.equals("-")) {
			pathSegments = pathSegments.subList(1, pathSegments.size());
			if (pathSegments.isEmpty())
				throw new ClientException(SC_BAD_REQUEST, "Invalid request path");				
			currentSegment = pathSegments.get(0);
			pathSegments = pathSegments.subList(1, pathSegments.size());
			if (currentSegment.equals("package")) {
				if (pathSegments.size() >= 2) {
					var packageName = decodePath(pathSegments.get(0));
					if (pathSegments.get(1).equals("dist-tags")) {
						if (pathSegments.size() == 2) {
							if (isGet) {
								sessionService.run(() -> {
									var project = checkProject(projectId, false);
									var packs = packService.queryByName(project, TYPE, packageName, null);
									var distTags = new HashMap<String, String>();
									for (var pack : packs) {
										var packData = (NpmData) pack.getData();
										for (var distTag : packData.getDistTags())
											distTags.put(distTag, pack.getVersion());
									}
									response.setStatus(SC_OK);
									response.setContentType(MediaType.APPLICATION_JSON);
									try {
										response.getOutputStream().write(objectMapper.writeValueAsBytes(distTags));
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								});
							} else {
								throw new ClientException(SC_METHOD_NOT_ALLOWED);
							}
						} else {
							sessionService.run(() -> {
								checkProject(projectId, true);
							});
							var tag = decodePath(pathSegments.get(2));
							LockUtils.run(getLockName(projectId, packageName), () -> {
								if (isPut) {
									var baos = new ByteArrayOutputStream();
									try (var is = request.getInputStream()) {
										copyWithMaxSize(is, baos, MAX_TAG_BODY_LEN);
									} catch (DataTooLargeException e) {
										throw new ClientException(SC_REQUEST_ENTITY_TOO_LARGE, "Tag body is too large");
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
									var version = StringUtils.strip(baos.toString(UTF_8), "\"");
									transactionService.run(() -> {
										var project = projectService.load(projectId);
										var pack = packService.findByNameAndVersion(project, TYPE, packageName, version);
										if (pack != null) {
											var packData = (NpmData) pack.getData();
											packData.getDistTags().add(tag);
										} else {
											throw new ClientException(SC_NOT_FOUND);
										}
									});
									response.setStatus(SC_OK);
								} else if (isDelete) {
									transactionService.run(() -> {
										var project = projectService.load(projectId);
										for (var pack: packService.queryByName(project, TYPE, packageName, null)) {
											var packData = (NpmData) pack.getData();
											packData.getDistTags().remove(tag);
										}
									});
									response.setStatus(SC_OK);
								} else {
									throw new ClientException(SC_METHOD_NOT_ALLOWED);
								}
							});
						}
					} else {
						throw new ClientException(SC_BAD_REQUEST, "Invalid request path");
					}
				} else {
					throw new ClientException(SC_BAD_REQUEST, "Invalid request path");
				}
			} else if (currentSegment.equals("v1")) {
				if (pathSegments.size() == 1 && pathSegments.get(0).equals("search")) {
					var query = request.getParameter("text");
					var offset = Integer.parseInt(request.getParameter("from"));
					var count = Math.min(Integer.parseInt(request.getParameter("size")), MAX_QUERY_COUNT);
					sessionService.run(() -> {
						var project = checkProject(projectId, false);
						var objectsNode = objectMapper.createArrayNode();
						for (var pack: packService.queryLatests(project, TYPE, query, true, offset, count)) {
							var packNode = objectMapper.createObjectNode();
							var packData = (NpmData) pack.getData();								
							var metadata = readJson(packData.getMetadata());
							packNode.set("name", metadata.get("name"));
							packNode.set("version", metadata.get("version"));
							var jsonDate = pack.getPublishDate().toInstant()
									.atZone(ZoneId.systemDefault()).format(ISO_OFFSET_DATE_TIME);
							packNode.put("date", jsonDate);
							
							var description = "";
							if (metadata.get("description") != null)
								description = metadata.get("description").asText();
							packNode.put("description", description);

							if (metadata.get("author") != null) 
								packNode.set("author", metadata.get("author"));
							
							var publisherNode = objectMapper.createObjectNode();
							publisherNode.put("name", pack.getUser().getDisplayName());
							packNode.set("publisher", publisherNode);
							
							packNode.set("maintainers", objectMapper.createArrayNode());
							
							var keywordsNode = metadata.get("keywords");
							if (keywordsNode != null)
								packNode.set("keywords", keywordsNode);
							
							var linksNode = objectMapper.createObjectNode();
							linksNode.put("npm", "/" + project.getPath() + "/~packages/" + pack.getId());
							var repositoryNode = metadata.get("repository");
							if (repositoryNode != null) 
								linksNode.put("repository", repositoryNode.get("url").asText());
							var homepageNode = metadata.get("homepage");
							if (homepageNode != null)
								linksNode.put("homepage", homepageNode.asText());
							packNode.set("links", linksNode);
							
							var objectNode = objectMapper.createObjectNode();
							objectNode.set("package", packNode);
							objectsNode.add(objectNode);
						}
						
						var responseNode = objectMapper.createObjectNode();
						responseNode.put("total", objectsNode.size());
						responseNode.set("objects", objectsNode);
						response.setContentType(MediaType.APPLICATION_JSON);
						try {
							response.getOutputStream().write(writeJson(responseNode));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						response.setStatus(SC_OK);
					});
				} else {
					throw new ClientException(SC_NOT_FOUND);
				}
			} else {
				throw new ClientException(SC_NOT_FOUND);
			}
		} else {
			String packageName;
			packageName = decodePath(currentSegment);
			pathSegments = pathSegments.subList(1, pathSegments.size());
			if (pathSegments.isEmpty()) {
				if (isGet) {
					var project = checkProject(projectId, false);
					sessionService.run(() -> {
						var packs = packService.queryByName(project, TYPE, packageName, null);
						var npmUrl = urlService.urlFor(project, true) + "/~" + getHandlerId() + "/" + encodePath(packageName);
						var distTagsNode = objectMapper.createObjectNode();
						var versionsNode = objectMapper.createObjectNode();
						NpmData latestPackData = null;
						for (var pack: packs) {
							var packData = (NpmData) pack.getData();
							for (var distTag: packData.getDistTags()) 
								distTagsNode.put(distTag, pack.getVersion());
							ObjectNode versionMetadata = readJson(packData.getMetadata());
							PackBlob packBlob;
							if ((packBlob = packBlobService.checkPackBlob(projectId, packData.getFileSha256BlobHash())) != null) {
								latestPackData = packData;
								var distNode = versionMetadata.putObject("dist");
								distNode.put("shasum", packBlobService.getSha1Hash(packBlob));
								distNode.put("integrity", "sha512-" + encodeBase64String(decodeHex(packBlobService.getSha512Hash(packBlob))));
								distNode.put("tarball", npmUrl + "/-/" + encodePath(pack.getVersion()) + "/" + encodePath(packData.getFileName()));
								versionsNode.set(pack.getVersion(), versionMetadata);
							}
						}
						if (latestPackData != null) {
							var packageMetadata = readJson(latestPackData.getPackageMetadata());
							packageMetadata.set("dist-tags", distTagsNode);
							packageMetadata.set("versions", versionsNode);
							response.setContentType(MediaType.APPLICATION_JSON);
							try {
								response.getOutputStream().write(writeJson(packageMetadata));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							response.setStatus(SC_OK);
						} else {
							response.setStatus(SC_NOT_FOUND);
						}	
					});
				} else if (isPut) {
					sessionService.run(() -> {
						checkProject(projectId, true);
					});
					try (var is = request.getInputStream()) {
						var baos = new ByteArrayOutputStream();
						copyWithMaxSize(is, baos, MAX_UPLOAD_METADATA_LEN);

						var packageMetadata = readJson(baos.toByteArray());

						var distTags = new HashMap<String, String>();
						var distTagsNode = packageMetadata.get("dist-tags");
						if (distTagsNode != null) {
							for (var it = distTagsNode.fields(); it.hasNext(); ) {
								var field = it.next();
								distTags.put(field.getKey(), field.getValue().asText());
							}
						}

						var attachments = new HashMap<String, byte[]>();
						var attachmentsNode = packageMetadata.get("_attachments");
						if (attachmentsNode != null) {
							for (var it = attachmentsNode.fields(); it.hasNext(); ) {
								var field = it.next();
								var fileName = field.getKey();
								var fileContent = Base64.decodeBase64(field.getValue().get("data").asText());
								if (fileContent.length != field.getValue().get("length").asInt()) {
									throw new ClientException(SC_BAD_REQUEST, "File length incorrect: " + fileName);
								}
								attachments.put(fileName, fileContent);
							}
						}

						var versionsNode = packageMetadata.get("versions");

						packageMetadata.remove("dist-tags");
						packageMetadata.remove("versions");
						packageMetadata.remove("_attachments");
						packageMetadata.remove("access");

						byte[] packageMetadataBytes = writeJson(packageMetadata);

						if (versionsNode != null) {
							for (var it = versionsNode.fields(); it.hasNext(); ) {
								var field = it.next();
								var version = field.getKey();
								var versionMetadata = (ObjectNode) field.getValue();
								var name = versionMetadata.get("name").asText();

								LockUtils.run(getLockName(projectId, name), () -> {
									transactionService.run(() -> {
										var project = projectService.load(projectId);
										var pack = packService.findByNameAndVersion(project, TYPE, name, version);
										if (pack != null) {
											var packData = (NpmData) pack.getData();
											if (packBlobService.checkPackBlob(projectId, packData.getFileSha256BlobHash()) != null) {
												var errorMessage = String.format("Package already exists (name: %s, version: %s)",
														name, version);
												throw new ClientException(SC_CONFLICT, errorMessage);
											}
										} else {
											pack = new Pack();
											pack.setType(TYPE);
											pack.setName(name);
											pack.setVersion(version);
											pack.setProject(project);
										}
										Build build = null;
										if (buildId != null)
											build = buildService.load(buildId);
										pack.setBuild(build);
										pack.setUser(SecurityUtils.getUser());
										pack.setPublishDate(new Date());

										var distTagsOfVersion = new LinkedHashSet<String>();
										for (var entry : distTags.entrySet()) {
											if (entry.getValue().equals(version))
												distTagsOfVersion.add(entry.getKey());
										}

										var distNode = versionMetadata.get("dist");
										versionMetadata.remove("dist");

										byte[] versionMetadataBytes = writeJson(versionMetadata);

										if (distNode != null) {
											var fileName = substringAfterLast(distNode.get("tarball").asText(), "-/");
											var fileContent = attachments.get(fileName);
											if (fileContent != null) {
												var integrity = distNode.get("integrity").asText();
												var algorithm = substringBefore(integrity, "-");
												var hash = Base64.decodeBase64(substringAfter(integrity, "-"));
												if (algorithm.equals("sha512")) {
													if (!Arrays.equals(decodeHex(Digest.sha512Of(fileContent).getHash()), hash)) {
														throw new ClientException(SC_BAD_REQUEST, "Integrity check failed: " + fileName);
													}
												} else if (algorithm.equals("sha1")) {
													if (!Arrays.equals(decodeHex(Digest.sha1Of(fileContent).getHash()), hash)) {
														throw new ClientException(SC_BAD_REQUEST, "Integrity check failed: " + fileName);
													}
												} else {
													var errorMessage = String.format("Unexpected integrity algorithm (file: %s, algorithm: %s)",
															fileName, algorithm);
													throw new ClientException(SC_BAD_REQUEST, errorMessage);
												}
												var packBlobId = packBlobService.uploadBlob(projectId, fileContent, null);
												var sha256Hash = packBlobService.load(packBlobId).getSha256Hash();
												pack.setData(new NpmData(packageMetadataBytes, versionMetadataBytes, distTagsOfVersion, fileName, sha256Hash));
												packService.createOrUpdate(pack, newArrayList(packBlobService.load(packBlobId)), true);
												response.setStatus(SC_CREATED);
											}
										}
									});
								});
							}
						}
					} catch (DataTooLargeException e) {
						throw new ClientException(SC_REQUEST_ENTITY_TOO_LARGE, "Package metadata is too large");
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					throw new ClientException(SC_METHOD_NOT_ALLOWED);
				}
			} else {
				currentSegment = decodePath(pathSegments.get(0));
				if (currentSegment.equals("-rev")) {
					if (isDelete) {
						LockUtils.run(getLockName(projectId, packageName), () -> {
							transactionService.run(() -> {
								var project = checkProject(projectId, true);
								var packs = packService.queryByName(project, TYPE, packageName, null);
								if (!packs.isEmpty()) {
									for (var pack: packs)
										packService.delete(pack);
									response.setStatus(SC_OK);									
								} else {
									response.setStatus(SC_NOT_FOUND);
								}
							});
						});
					} else if (isPut) {
						response.setStatus(SC_OK);						
					} else {
						throw new ClientException(SC_METHOD_NOT_ALLOWED);
					}
				} else if (currentSegment.equals("-")) {
					if (pathSegments.isEmpty())
						throw new ClientException(SC_BAD_REQUEST, "Missing version or file name");
					pathSegments = pathSegments.subList(1, pathSegments.size());
					if (pathSegments.size() == 1) {
						var fileName = decodePath(pathSegments.get(0));
						if (fileName.startsWith(packageName + "-")) {
							var version = substringBefore(fileName.substring(packageName.length() + 1), ".");
							sessionService.run(() -> {
								var project = checkProject(projectId, false);
								var pack = packService.findByNameAndVersion(project, TYPE, packageName, version);
								if (pack != null) {
									var packData = (NpmData) pack.getData();
									PackBlob packBlob;
									if ((packBlob = packBlobService.checkPackBlob(projectId, packData.getFileSha256BlobHash())) != null) {
										try {
											response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
											packBlobService.downloadBlob(packBlob.getProject().getId(), packBlob.getSha256Hash(), response.getOutputStream());
											response.setStatus(SC_OK);
										} catch (IOException e) {
											throw new RuntimeException(e);
										}
									} else {
										response.setStatus(SC_NOT_FOUND);
									}
								} else {
									response.setStatus(SC_NOT_FOUND);
								}
							});
						}
					} else {
						var version = decodePath(pathSegments.get(0));
						var fileName = decodePath(pathSegments.get(1));
						if (isGet) {
							sessionService.run(() -> {
								var project = checkProject(projectId, false);
								var pack = packService.findByNameAndVersion(project, TYPE, packageName, version);
								if (pack != null) {
									var packData = (NpmData) pack.getData();
									if (!packData.getFileName().equals(fileName)) 
										throw new ClientException(SC_BAD_REQUEST, "Incorrect file name requested");
									PackBlob packBlob;
									if ((packBlob = packBlobService.checkPackBlob(projectId, packData.getFileSha256BlobHash())) != null) {
										try {
											response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
											packBlobService.downloadBlob(packBlob.getProject().getId(), packData.getFileSha256BlobHash(), response.getOutputStream());
											response.setStatus(SC_OK);
										} catch (IOException e) {
											throw new RuntimeException(e);
										}
									} else {
										response.setStatus(SC_NOT_FOUND);
									}
								} else {
									response.setStatus(SC_NOT_FOUND);
								}
							});
						} else if (isDelete) {
							LockUtils.run(getLockName(projectId, packageName), () -> {
								transactionService.run(() -> {
									var project = checkProject(projectId, true);
									var pack = packService.findByNameAndVersion(project, TYPE, packageName, version);
									if (pack != null) {
										packService.delete(pack);
										response.setStatus(SC_OK);
									} else {
										response.setStatus(SC_NOT_FOUND);
									}
								});	
							});							
						} else {
							throw new ClientException(SC_METHOD_NOT_ALLOWED);
						}
					}
				} else {
					throw new ClientException(SC_BAD_REQUEST, "Invalid request path");
				}
			}
		}
	}

	@Override
	public String getApiKey(HttpServletRequest request) {
		var authzHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authzHeader != null&& authzHeader.toLowerCase().startsWith("bearer ")) 
			return StringUtils.substringAfter(authzHeader, " ");
		else
			return null;
	}

	private Project checkProject(Long projectId, boolean needsToWrite) {
		var project = projectService.load(projectId);
		if (!project.isPackManagement())
			throw new ClientException(SC_NOT_ACCEPTABLE, "Package management not enabled for project '" + project.getPath() + "'");
		else if (needsToWrite && !SecurityUtils.canWritePack(project))
			throw new UnauthorizedException("No package write permission for project: " + project.getPath());
		else if (!needsToWrite && !SecurityUtils.canReadPack(project))
			throw new UnauthorizedException("No package read permission for project: " + project.getPath());
		return project;
	}
	
	@Override
	public List<String> normalize(List<String> pathSegments) {
		return pathSegments;
	}

}
