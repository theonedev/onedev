package io.onedev.server.plugin.pack.pypi;

import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.pack.PackHandler;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.service.PackService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.UrlUtils;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static io.onedev.server.plugin.pack.pypi.PypiPackSupport.TYPE;
import static io.onedev.server.util.GroovyUtils.evalTemplate;
import static java.lang.Integer.MAX_VALUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.*;

@Singleton
public class PypiPackHandler implements PackHandler {
	
	public static final String HANDLER_ID = "pypi";
	
	private static final String CONTENT_TYPE_SIMPLE = "application/vnd.pypi.simple.v1+html";

	private static final Comparator<Pack> VERSION_COMPARATOR = comparing(Pack::getVersion);
	
	private final SessionService sessionService;
	
	private final TransactionService transactionService;
	
	private final PackBlobService packBlobService;
	
	private final PackService packService;
	
	private final ProjectService projectService;
	
	private final BuildService buildService;
	
	@Inject
	public PypiPackHandler(SessionService sessionService, TransactionService transactionService,
						   PackBlobService packBlobService, PackService packService,
						   ProjectService projectService, BuildService buildService) {
		this.sessionService = sessionService;
		this.transactionService = transactionService;
		this.packBlobService = packBlobService;
		this.packService = packService;
		this.projectService = projectService;
		this.buildService = buildService;
	}
	
	@Override
	public String getHandlerId() {
		return HANDLER_ID;
	}

	private String getLockName(Long projectId, String name) {
		return "update-pack:" + projectId + ":" + TYPE + ":" + name;
	}
	
	private String getAttribute(Map<String, List<String>> attributes, String attributeKey) {
		var value = attributes.get(attributeKey);
		if (value == null || value.isEmpty())
			throw new ClientException(SC_BAD_REQUEST, "Attribute not found: " + attributeKey);
		return value.get(0);
	}
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, Long projectId, 
						Long buildId, List<String> pathSegments) {
		var method = request.getMethod();
		
		var isGet = method.equals("GET");
		var isPost = method.equals("POST");
		
		if (pathSegments.isEmpty()) {
			if (isPost) {
				var upload = new ServletFileUpload();
				try {
					var attributes = new LinkedHashMap<String, List<String>>();
					var items = upload.getItemIterator(request);
					while (items.hasNext()) {
						var item = items.next();
						try (var is = item.openStream()) {
							if (item.isFormField()) {
								var itemValue = Streams.asString(is, UTF_8.name());
								if (StringUtils.isNotBlank(itemValue)) 
									attributes.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>()).add(itemValue);
							} else {
								var name = getAttribute(attributes, "name");
								var version = getAttribute(attributes, "version");
								var sha256Hash = getAttribute(attributes, "sha256_digest");
								
								attributes.remove("name");
								attributes.remove("version");
								attributes.remove("filetype");
								attributes.remove("metadata_version");
								attributes.remove("pyversion");
								attributes.remove("sha256_digest");
								attributes.remove("md5_digest");
								attributes.remove("blake2_256_digest");
								attributes.remove(":action");
								attributes.remove("protocol_version");
								
								LockUtils.run(getLockName(projectId, name), () -> transactionService.run(() -> {
									var project = checkProject(projectId, true);
									var contentDisposition = item.getHeaders().getHeader("content-disposition"); 
									if (contentDisposition == null)
										throw new ClientException(SC_BAD_REQUEST, "Content disposition header not found in uploaded file");
									String fileName = null;
									for (var field: Splitter.on(";").omitEmptyStrings().trimResults().split(contentDisposition)) {
										if (field.startsWith("filename=")) {
											fileName = field.substring("filename=".length() + 1);
											fileName = fileName.substring(0, fileName.length() - 1);
											break;
										}
									}
									if (fileName == null) 
										throw new ClientException(SC_BAD_REQUEST, "File name not found in content disposition header of uploaded file");

									var packBlobId = packBlobService.uploadBlob(projectId, is, sha256Hash);																																								
									if (packBlobId == null)
										throw new ClientException(SC_BAD_REQUEST, "Digest mismatch");
									
									PypiData data;
									var pack = packService.findByNameAndVersion(project, TYPE, name, version);
									if (pack == null) {
										pack = new Pack();
										pack.setType(TYPE);
										pack.setName(name);
										pack.setVersion(version);
										pack.setProject(project);
										data = new PypiData(attributes, new LinkedHashMap<>());
										pack.setData(data);
									} else {
										data = (PypiData) pack.getData();
									}

									Build build = null;
									if (buildId != null)
										build = buildService.load(buildId);
									pack.setBuild(build);
									pack.setUser(SecurityUtils.getUser());
									pack.setPublishDate(new Date());
									
									if (data.getSha256BlobHashes().containsKey(fileName)) {
										var errorMessage = String.format("Package already exists (name: %s, version: %s)", name, version);
										throw new ClientException(SC_CONFLICT, errorMessage);
									} 
									data.getSha256BlobHashes().put(fileName, sha256Hash);
									
									var packBlobs = data.getSha256BlobHashes().values().stream()
											.map(hash -> packBlobService.findBySha256Hash(projectId, hash))
											.filter(Objects::nonNull)
											.collect(toList());
									packService.createOrUpdate(pack, packBlobs, data.getSha256BlobHashes().size() == 1);									
								}));
								response.setStatus(SC_OK);
								break;
							}
						}
					}
				} catch (IOException | FileUploadException e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			}
		} else {
			if (!isGet)
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			var currentSegment = pathSegments.get(0);
			pathSegments = pathSegments.subList(1, pathSegments.size());
			
			// https://peps.python.org/pep-0503/
			if (currentSegment.equals("simple")) { 
				if (pathSegments.isEmpty()) {
					sessionService.run(() -> {
						var project = checkProject(projectId, false);
						var names = packService.queryNames(project, TYPE, null, true, 0, MAX_VALUE);
						var bindings = new HashMap<String, Object>();
						bindings.put("names", names);
						try {
							URL tplUrl = Resources.getResource(getClass(), "packages.tpl");
							String template = Resources.toString(tplUrl, UTF_8);
							response.setContentType(CONTENT_TYPE_SIMPLE);
							sendResponse(response, evalTemplate(template, bindings));
							response.setStatus(SC_OK);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
				} else {
					var name = UrlUtils.decodePath(pathSegments.get(0));
					sessionService.run(() -> {
						var project = checkProject(projectId, false);
						var packs = packService.queryByName(project, TYPE, name, VERSION_COMPARATOR);
						if (!packs.isEmpty()) {
							var bindings = new HashMap<String, Object>();
							bindings.put("baseUrl", "/" + project.getPath() + "/~" + HANDLER_ID + "/files/" + UrlUtils.encodePath(name));
							bindings.put("packs", packs);
							try {
								URL tplUrl = Resources.getResource(getClass(), "package-versions.tpl");
								String template = Resources.toString(tplUrl, UTF_8);
								response.setContentType(CONTENT_TYPE_SIMPLE);
								sendResponse(response, evalTemplate(template, bindings));
								response.setStatus(SC_OK);
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						} else {
							response.setStatus(SC_NOT_FOUND);
						}
					});
				}
			} else if (currentSegment.equals("files")) {
				if (pathSegments.size() < 1)
					throw new ClientException(SC_BAD_REQUEST, "Package name param is missing");
				else if (pathSegments.size() < 2)
					throw new ClientException(SC_BAD_REQUEST, "Package version param is missing");
				else if (pathSegments.size() < 3)
					throw new ClientException(SC_BAD_REQUEST, "Package file name param is missing");
				
				var name = UrlUtils.decodePath(pathSegments.get(0));
				var version = UrlUtils.decodePath(pathSegments.get(1));
				var fileName = UrlUtils.decodePath(pathSegments.get(2));
				
				sessionService.run(() -> {
					var project = checkProject(projectId, false);
					var pack = packService.findByNameAndVersion(project, TYPE, name, version);
					if (pack != null) {
						var data = (PypiData) pack.getData();
						var sha256BlobHash = data.getSha256BlobHashes().get(fileName);
						if (sha256BlobHash != null) {
							PackBlob packBlob;
							if ((packBlob = packBlobService.checkPackBlob(projectId, sha256BlobHash)) != null) {
								response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
								try {
									packBlobService.downloadBlob(packBlob.getProject().getId(),
											packBlob.getSha256Hash(), response.getOutputStream());
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
								response.setStatus(SC_OK);
							} else {
								throw new ExplicitException("Pack blob missing or corrupted: " + sha256BlobHash);
							}
						} else {
							response.setStatus(SC_NOT_FOUND);							
						}
					} else {
						response.setStatus(SC_NOT_FOUND);
					}
				});
			} else {
				response.setStatus(SC_NOT_FOUND);
			}
		}
	}

	@Override
	public String getApiKey(HttpServletRequest request) {
		return null;
	}
	
	private void sendResponse(HttpServletResponse response, String content) {
		try {
			response.getOutputStream().print(content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Project checkProject(Long projectId, boolean needsToWrite) {
		var project = projectService.load(projectId);
		if (!project.isPackManagement()) {
			throw new ClientException(SC_NOT_ACCEPTABLE, "Package management not enabled for project '" + project.getPath() + "'");
		} else if (needsToWrite && !SecurityUtils.canWritePack(project)) {
			throw new UnauthorizedException("No package write permission for project: " + project.getPath());
		} else if (!needsToWrite && !SecurityUtils.canReadPack(project)) {
			throw new UnauthorizedException("No package read permission for project: " + project.getPath());
		}
		return project;
	}

	@Override
	public List<String> normalize(List<String> pathSegments) {
		return pathSegments;
	}

}
