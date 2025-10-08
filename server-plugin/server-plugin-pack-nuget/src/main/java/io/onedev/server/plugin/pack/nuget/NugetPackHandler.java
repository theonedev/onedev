package io.onedev.server.plugin.pack.nuget;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.plugin.pack.nuget.NugetPackSupport.TYPE;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static io.onedev.server.util.IOUtils.copyWithMaxSize;
import static io.onedev.server.util.UrlUtils.decodePath;
import static io.onedev.server.util.UrlUtils.decodeQuery;
import static io.onedev.server.util.UrlUtils.encodePath;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.onedev.server.pack.PackHandler;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.service.PackService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.exception.DataTooLargeException;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.SemanticVersion;
import io.onedev.server.util.XmlUtils;
import io.onedev.server.web.UrlService;

@Singleton
public class NugetPackHandler implements PackHandler {
	
	public static final String HANDLER_ID = "nuget";
	
	private static final Logger logger = LoggerFactory.getLogger(NugetPackHandler.class);

	private static final String HEADER_API_KEY = "X-NuGet-ApiKey";
	
	private static final int MAX_NUSPEC_SIZE = 10000000;
	
	private static final int MAX_QUERY_COUNT = 1000;
	
	private static final Comparator<Pack> VERSION_COMPARATOR = (o1, o2) -> {
		try {
			return new SemanticVersion(o1.getVersion()).compareTo(new SemanticVersion(o2.getVersion()));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	};
	
	private final SessionService sessionService;
	
	private final TransactionService transactionService;
	
	private final PackBlobService packBlobService;
	
	private final PackService packService;
	
	private final ProjectService projectService;
	
	private final BuildService buildService;
	
	private final ObjectMapper objectMapper;
	
	private final UrlService urlService;

	@Inject
	public NugetPackHandler(SessionService sessionService, TransactionService transactionService,
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

	private byte[] writeJson(Object value) {
		try {
			return objectMapper.writeValueAsBytes(value);
		} catch (IOException e) {
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
		
		if (pathSegments.isEmpty()) {
			logger.warn("Invalid request path");
			throw new ClientException(SC_BAD_REQUEST);
		}
	
		var currentSegment = pathSegments.get(0);
		pathSegments = pathSegments.subList(1, pathSegments.size());
		if (currentSegment.equals("index.json")) {
			if (isGet) {
				var baseUrl = sessionService.call(() -> getBaseUrl(checkProject(projectId, false)));
				
				var resources = newArrayList(
						Map.of(
								"@type", "PackageBaseAddress/3.0.0",
								"@id", baseUrl + "/package"),
						Map.of(
								"@type", "PackagePublish/2.0.0",
								"@id", baseUrl + "/publish"),
						Map.of(
								"@type", "RegistrationsBaseUrl",
								"@id", baseUrl + "/registration"),
						Map.of(
								"@type", "RegistrationsBaseUrl/3.0.0-beta",
								"@id", baseUrl + "/registration"),
						Map.of(
								"@type", "RegistrationsBaseUrl/3.0.0-rc",
								"@id", baseUrl + "/registration"),
						Map.of(
								"@type", "SearchQueryService",
								"@id", baseUrl + "/query"),
						Map.of(
								"@type", "SearchQueryService/3.0.0-beta",
								"@id", baseUrl + "/query"),
						Map.of(
								"@type", "SearchQueryService/3.0.0-rc",
								"@id", baseUrl + "/query"),
						Map.of(
								"@type", "SymbolPackagePublish/4.9.0",
								"@id", baseUrl + "/publish")
				);
				
				sendResponse(response, Map.of(
						"version", "3.0.0", 
						"resources", resources));
				response.setStatus(SC_OK);
			} else {
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			}
		} else if (currentSegment.equals("publish")) {
			if (isPut) {
				sessionService.run(() -> {
					checkProject(projectId, true);
				});
				var upload = new ServletFileUpload();
				try {
					var items = upload.getItemIterator(request);
					if (items.hasNext()) {
						var item = items.next();
						var tempFile = FileUtils.createTempFile("upload", "nuget");
						try {
							try (
									var is = item.openStream();
									var os = new BufferedOutputStream(new FileOutputStream(tempFile), BUFFER_SIZE)) {
								IOUtils.copy(is, os, BUFFER_SIZE);
							}

							byte[] metadataBytes = null;
							try (var is = new ZipInputStream(new BufferedInputStream(new FileInputStream(tempFile), BUFFER_SIZE))) {
								ZipEntry entry;
								while ((entry = is.getNextEntry()) != null) {
									if (!entry.getName().contains("/") && entry.getName().endsWith(".nuspec")) {
										var baos = new ByteArrayOutputStream();
										copyWithMaxSize(is, baos, MAX_NUSPEC_SIZE);
										metadataBytes = baos.toByteArray();
										break;
									}
								}
							} catch (DataTooLargeException e) {
								logger.warn("Package metadata is too large");
								throw new ClientException(SC_REQUEST_ENTITY_TOO_LARGE);
							}
							if (metadataBytes == null) {
								logger.warn("Package metadata not found");
								throw new ClientException(SC_BAD_REQUEST);
							}

							var saxReader = newSAXReader();
							var document = readXml(saxReader, metadataBytes);
							
							var metadataElement = document.getRootElement().element("metadata");
							var name = metadataElement.elementText("id").trim();
							var version = new SemanticVersion(metadataElement.elementText("version").trim()).clearBuildMeta().toString();
							
							var packageTypesElement = metadataElement.element("packageTypes");
							boolean symbolsPackage;
							if (packageTypesElement != null) {
								symbolsPackage = packageTypesElement.elements().stream()
										.anyMatch(it -> "SymbolsPackage".equals(it.attributeValue("name")));
							} else {
								symbolsPackage = false;
							}
							
							var metadataBytesCopy = metadataBytes;
							LockUtils.run(getLockName(projectId, name), () -> transactionService.run(() -> {
								var project = projectService.load(projectId);
								var pack = packService.findByNameAndVersion(project, TYPE, name, version);

								if (!symbolsPackage) {
									if (pack != null) {
										var errorMessage = String.format("Package already exists (id: %s, version: %s)",
												name, version);
										logger.warn(errorMessage);
										throw new ClientException(SC_CONFLICT);
									}
									pack = new Pack();
									pack.setType(TYPE);
									pack.setName(name);
									pack.setVersion(version);
									pack.setProject(project);
									pack.setPrerelease(version.contains("-"));

									if (buildId != null)
										pack.setBuild(buildService.load(buildId));
									pack.setUser(SecurityUtils.getUser());
									pack.setPublishDate(new Date());

									PackBlob packBlob;
									try (var is = new FileInputStream(tempFile)) {
										packBlob = packBlobService.load(packBlobService.uploadBlob(projectId, is, null));
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
									pack.setData(new NugetData(packBlob.getSha256Hash(), null, 
											metadataBytesCopy));
									packService.createOrUpdate(pack, newArrayList(packBlob), true);
									response.setStatus(SC_CREATED);
								} else {
									if (pack == null) {
										var errorMessage = String.format("Package not found (id: %s, version: %s)",
												name, version);
										logger.warn(errorMessage);
										throw new ClientException(SC_NOT_FOUND);
									}

									PackBlob snupkgBlob;
									try (var is = new FileInputStream(tempFile)) {
										snupkgBlob = packBlobService.load(packBlobService.uploadBlob(projectId, is, null));
									} catch (IOException e) {
										throw new RuntimeException(e);
									}

									List<PackBlob> packBlobs = Lists.newArrayList(snupkgBlob);
									var data = (NugetData) pack.getData();
									var nupkgBlob = packBlobService.findBySha256Hash(projectId, data.getNupkgBlobSha256Hash());
									if (nupkgBlob != null)
										packBlobs.add(nupkgBlob);

									pack.setData(new NugetData(data.getNupkgBlobSha256Hash(), snupkgBlob.getSha256Hash(), 
											data.getMetadata()));
									packService.createOrUpdate(pack, packBlobs, false);
									response.setStatus(SC_CREATED);
								}
							}));
						} catch (ParseException e) {
							logger.warn("Package version is not a SemVer v2 compatible version");
							throw new ClientException(SC_BAD_REQUEST);
						} finally {
							FileUtils.deleteFile(tempFile);
						}
					} else {
						throw new ClientException(SC_BAD_REQUEST);
					}
				} catch (FileUploadException | IOException e) {
					throw new RuntimeException(e);
				}
			} else if (isDelete) {
				if (pathSegments.isEmpty()) {
					logger.warn("Package id is missing");
					throw new ClientException(SC_BAD_REQUEST);
				}
				var name = pathSegments.get(0);
				pathSegments = pathSegments.subList(1, pathSegments.size());
				if (pathSegments.isEmpty()) {
					logger.warn("Package version is missing");
					throw new ClientException(SC_BAD_REQUEST);
				}
				var version = pathSegments.get(0);

				LockUtils.run(getLockName(projectId, name), () -> transactionService.run(() -> {
					var project = checkProject(projectId, true);
					var pack = packService.findByNameAndVersion(project, TYPE, name, version);
					if (pack != null) {
						packService.delete(pack);
						response.setStatus(SC_NO_CONTENT);
					} else {
						response.setStatus(SC_NOT_FOUND);
					}
				}));
			} else {
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			}
		} else if (currentSegment.equals("query")) {
			sessionService.run(() -> {
				var nameQuery = request.getParameter("q");
				if (StringUtils.isBlank(nameQuery))
					nameQuery = null;
				else 
					nameQuery = decodeQuery(nameQuery);
				var skip = request.getParameter("skip");
				int offset = 0;
				if (StringUtils.isNotBlank(skip))
					offset = Integer.parseInt(skip);
				var count = 0;
				var take = request.getParameter("take");
				if (StringUtils.isNotBlank(take))
					count = Math.min(Integer.parseInt(take), MAX_QUERY_COUNT);
				
				boolean includePrerelease = "true".equals(request.getParameter("prerelease"));
				
				var project = checkProject(projectId, false);
				var totalHits = packService.countNames(project, TYPE, nameQuery, includePrerelease);
				var names = packService.queryNames(project, TYPE, nameQuery, includePrerelease, offset, count);
				Map<String, List<Pack>> packs;
				if (!names.isEmpty()) 
					packs = packService.loadPacks(names, includePrerelease, VERSION_COMPARATOR);
				else 
					packs = new LinkedHashMap<>();

				var saxReader = newSAXReader();
				var baseUrl = getBaseUrl(project);
				var dataValue = new ArrayList<Map<String, Object>>();
				for (var entry : packs.entrySet()) {
					if (!entry.getValue().isEmpty()) {
						var dataItemValue = new HashMap<String, Object>();
						dataItemValue.put("id", entry.getKey());
						var latestPack = entry.getValue().get(entry.getValue().size() - 1);
						dataItemValue.put("version", latestPack.getVersion());
						var latestData = (NugetData) latestPack.getData();
						var latestMetadataElement = readXml(saxReader, latestData.getMetadata()).getRootElement().element("metadata");
						populateMetadataValue(dataItemValue, latestMetadataElement);
						
						var versionsValue = new ArrayList<Map<String, Object>>();
						for (var pack : entry.getValue()) {
							var versionValue = new HashMap<String, Object>();
							versionValue.put("@id", getRegistrationLeafUrl(baseUrl, pack.getName(), pack.getVersion()));
							versionValue.put("version", pack.getVersion());
							versionsValue.add(versionValue);
						}
						dataItemValue.put("versions", versionsValue);
						dataValue.add(dataItemValue);
					}
				}
				var responseValue = new HashMap<String, Object>();
				responseValue.put("totalHits", totalHits);
				responseValue.put("data", dataValue);
				sendResponse(response, responseValue);
				response.setStatus(SC_OK);
			});
		} else if (currentSegment.equals("registration")) {
			if (pathSegments.size() != 2 && pathSegments.size() != 3)
				throw new ClientException(SC_BAD_REQUEST);
			if (!isGet)
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			var name = decodePath(pathSegments.get(0));
			if (pathSegments.size() == 2) {
				sessionService.run(() -> {
					var project = checkProject(projectId, false);
					var baseUrl = getBaseUrl(project);
					var packs = packService.queryByName(project, TYPE, name, VERSION_COMPARATOR);
					if (packs.isEmpty()) {
						response.setStatus(SC_NOT_FOUND);
					} else {
						var saxReader = newSAXReader();
						var responseValue = new HashMap<String, Object>();
						responseValue.put("count", 1);
						var pageValue = new HashMap<String, Object>();
						pageValue.put("@id", getRegistrationIndexUrl(baseUrl, name));
						pageValue.put("lower", packs.get(0).getVersion());
						pageValue.put("upper", packs.get(packs.size() - 1).getVersion());
						pageValue.put("parent", getRegistrationIndexUrl(baseUrl, name));
						pageValue.put("count", packs.size());
						var leavesValue = new ArrayList<Map<String, Object>>();
						for (var pack : packs)
							leavesValue.add(getLeafValue(pack, saxReader, baseUrl));
						pageValue.put("items", leavesValue);
						responseValue.put("items", Lists.newArrayList(pageValue));
						sendResponse(response, responseValue);
						response.setStatus(SC_OK);
					}
				});
			} else {
				var version = decodePath(pathSegments.get(1));
				sessionService.run(() -> {
					var project = checkProject(projectId, false);
					var saxReader = newSAXReader();
					var baseUrl = getBaseUrl(project);
					var pack = packService.findByNameAndVersion(project, TYPE, name, version);
					if (pack != null) {
						var leafValue = getLeafValue(pack, saxReader, baseUrl);
						leafValue.put("listed", true);
						var jsonDate = pack.getPublishDate().toInstant()
								.atZone(ZoneId.systemDefault()).format(ISO_OFFSET_DATE_TIME);
						leafValue.put("published", jsonDate);
						leafValue.put("registration", getRegistrationIndexUrl(baseUrl, name));
						sendResponse(response, leafValue);
						response.setStatus(SC_OK);
					} else {
						response.setStatus(SC_NOT_FOUND);
					}
				});
			}
		} else if (currentSegment.equals("package")) {
			if (pathSegments.size() != 2 && pathSegments.size() != 3)
				throw new ClientException(SC_BAD_REQUEST);
			if (!isGet)
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			var name = decodePath(pathSegments.get(0));
			if (pathSegments.size() == 2) {
				sessionService.run(() -> {
					var project = checkProject(projectId, false);
					var packs = packService.queryByName(project, TYPE, name, VERSION_COMPARATOR);
					if (!packs.isEmpty()) {
						var versionsValue = packs.stream().map(it->it.getVersion().toLowerCase()).collect(toList());
						sendResponse(response, Map.of("versions", versionsValue));
						response.setStatus(SC_OK);
					} else {
						response.setStatus(SC_NOT_FOUND);
					}
				});
			} else {
				var version = decodePath(pathSegments.get(1));
				var fileName = decodePath(pathSegments.get(2));
				sessionService.run(() -> {
					var project = checkProject(projectId, false);
					var pack = packService.findByNameAndVersion(project, TYPE, name, version);
					if (pack != null) {
						var data = (NugetData) pack.getData();
						if (fileName.endsWith(".nuspec")) {
							try {
								response.getOutputStream().write(data.getMetadata());
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						} else {
							PackBlob packBlob;
							if ((packBlob = packBlobService.checkPackBlob(projectId, data.getNupkgBlobSha256Hash())) != null) {
								try {
									packBlobService.downloadBlob(packBlob.getProject().getId(), 
											packBlob.getSha256Hash(), response.getOutputStream());
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							} else {
								throw new ExplicitException("Pack blob missing or corrupted: " + data.getNupkgBlobSha256Hash());
							}
						}
						response.setStatus(SC_OK);
					} else {
						response.setStatus(SC_NOT_FOUND);
					}
				});
			}
		} else {
			response.setStatus(SC_NOT_FOUND);
		}
	}
	
	private Map<String, Object> getLeafValue(Pack pack, SAXReader saxReader, String baseUrl) {
		var leafValue = new HashMap<String, Object>();
		leafValue.put("@id", getRegistrationLeafUrl(baseUrl, pack.getName(), pack.getVersion()));
		leafValue.put("packageContent", getPackageDownloadUrl(baseUrl, pack.getName(), pack.getVersion()));

		var metadataElement = readXml(saxReader, ((NugetData) pack.getData()).getMetadata())
				.getRootElement().element("metadata");

		var catalogEntryValue = new HashMap<String, Object>();
		catalogEntryValue.put("@id", getRegistrationLeafUrl(baseUrl, pack.getName(), pack.getVersion()));
		catalogEntryValue.put("packageContent", getPackageDownloadUrl(baseUrl, pack.getName(), pack.getVersion()));
		catalogEntryValue.put("id", pack.getName());
		catalogEntryValue.put("version", pack.getVersion());
		populateMetadataValue(catalogEntryValue, metadataElement);
		var jsonDate = pack.getPublishDate().toInstant()
				.atZone(ZoneId.systemDefault()).format(ISO_OFFSET_DATE_TIME);
		catalogEntryValue.put("published", jsonDate);

		var dependencyGroupsValue = new ArrayList<Map<String, Object>>();
		var dependenciesElement = metadataElement.element("dependencies");
		if (dependenciesElement != null) {
			for (var groupElement: dependenciesElement.elements()) {
				var dependencyGroupValue = new HashMap<String, Object>();
				dependencyGroupValue.put("targetFramework", groupElement.attributeValue("targetFramework"));
				var dependenciesValue = new ArrayList<Map<String, Object>>();
				for (var dependencyElement: groupElement.elements()) {
					var id = dependencyElement.attributeValue("id");
					var version = dependencyElement.attributeValue("version");
					if (id != null && version != null)
						dependenciesValue.add(Map.of("id", id, "version", version));
				}
				dependencyGroupValue.put("dependencies", dependenciesValue);
				dependencyGroupsValue.add(dependencyGroupValue);
			}
		}
		catalogEntryValue.put("dependencyGroups", dependencyGroupsValue);
		leafValue.put("catalogEntry", catalogEntryValue);
		return leafValue;		
	}
	
	private void populateMetadataValue(Map<String, Object> metadataValue, Element metadata) {
		metadataValue.put("description", metadata.elementText("description"));
		metadataValue.put("projectUrl", metadata.elementText("projectUrl"));
		metadataValue.put("licenseUrl", metadata.elementText("licenseUrl"));
		metadataValue.put("tags", metadata.elementText("tags"));
		metadataValue.put("title", metadata.elementText("title"));
		metadataValue.put("authors", metadata.elementText("authors"));
	}
	
	private SAXReader newSAXReader() {
		SAXReader reader = new SAXReader();
		XmlUtils.disallowDocTypeDecl(reader);
		return reader;
	}

	private Document readXml(SAXReader reader, byte[] content) {
		try {
			return reader.read(new ByteArrayInputStream(content));
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String getBaseUrl(Project project) {
		return urlService.urlFor(project, true) + "/~" + HANDLER_ID;
	}

	private String getRegistrationIndexUrl(String baseUrl, String name) {
		return baseUrl + "/registration/" + encodePath(name) + "/index.json";
	}
	
	private String getRegistrationLeafUrl(String baseUrl, String name, String version) {
		name = encodePath(name);
		version = encodePath(version);
		return baseUrl + "/registration/" + name + "/" + version + "/" + version + ".json";
	}

	private String getPackageDownloadUrl(String baseUrl, String name, String version) {
		name = encodePath(name);
		version = encodePath(version);
		return baseUrl + "/package/" + name + "/" + version + "/" + name + "." + version + ".nupkg";
	}
	
	private void sendResponse(HttpServletResponse response, Object value) {
		try {
			response.getOutputStream().write(writeJson(value));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Project checkProject(Long projectId, boolean needsToWrite) {
		var project = projectService.load(projectId);
		if (!project.isPackManagement()) {
			logger.warn("Package management not enabled for project '" + project.getPath() + "'");
			throw new ClientException(SC_NOT_ACCEPTABLE);
		} else if (needsToWrite && !SecurityUtils.canWritePack(project)) {
			throw new UnauthorizedException("No package write permission for project: " + project.getPath());
		} else if (!needsToWrite && !SecurityUtils.canReadPack(project)) {
			throw new UnauthorizedException("No package read permission for project: " + project.getPath());
		}
		return project;
	}

	@Override
	public String getApiKey(HttpServletRequest request) {
		return request.getHeader(HEADER_API_KEY);
	}
	
	@Override
	public List<String> normalize(List<String> pathSegments) {
		return pathSegments;
	}

}
