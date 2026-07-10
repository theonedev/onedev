package io.onedev.server.plugin.pack.cargo;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.plugin.pack.cargo.CargoPackSupport.TYPE;
import static io.onedev.server.util.UrlUtils.decodePath;
import static java.util.Comparator.comparing;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.UnauthorizedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackHandler;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.service.PackService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;

@Singleton
public class CargoPackHandler implements PackHandler {

	public static final String HANDLER_ID = "cargo";

	private static final int MAX_METADATA_SIZE = 10 * 1024 * 1024;

	private static final int MAX_CRATE_SIZE = 100 * 1024 * 1024;

	private final SessionService sessionService;

	private final TransactionService transactionService;

	private final PackBlobService packBlobService;

	private final PackService packService;

	private final ProjectService projectService;

	private final BuildService buildService;

	private final SettingService settingService;

	private final ObjectMapper objectMapper;

	@Inject
	public CargoPackHandler(SessionService sessionService, TransactionService transactionService,
							PackBlobService packBlobService, PackService packService,
							ProjectService projectService, BuildService buildService,
							SettingService settingService, ObjectMapper objectMapper) {
		this.sessionService = sessionService;
		this.transactionService = transactionService;
		this.packBlobService = packBlobService;
		this.packService = packService;
		this.projectService = projectService;
		this.buildService = buildService;
		this.settingService = settingService;
		this.objectMapper = objectMapper;
	}

	@Override
	public String getHandlerId() {
		return HANDLER_ID;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
					   Long projectId, Long buildId, List<String> pathSegments) {
		var method = request.getMethod();
		var isGet = method.equals("GET");
		var isPut = method.equals("PUT");
		var isDelete = method.equals("DELETE");

		try {
			if (pathSegments.equals(newArrayList("api", "v1", "crates", "new"))) {
				if (!isPut)
					throw new ClientException(SC_METHOD_NOT_ALLOWED);
				publish(request, response, projectId, buildId);
			} else if (pathSegments.size() == 6
					&& pathSegments.subList(0, 3).equals(newArrayList("api", "v1", "crates"))
					&& pathSegments.get(5).equals("download")) {
				if (!isGet)
					throw new ClientException(SC_METHOD_NOT_ALLOWED);
				download(response, projectId, decodePath(pathSegments.get(3)), decodePath(pathSegments.get(4)));
			} else if (pathSegments.size() == 6
					&& pathSegments.subList(0, 3).equals(newArrayList("api", "v1", "crates"))
					&& pathSegments.get(5).equals("yank")) {
				if (!isDelete)
					throw new ClientException(SC_METHOD_NOT_ALLOWED);
				setYanked(response, projectId, decodePath(pathSegments.get(3)), decodePath(pathSegments.get(4)), true);
			} else if (pathSegments.size() == 6
					&& pathSegments.subList(0, 3).equals(newArrayList("api", "v1", "crates"))
					&& pathSegments.get(5).equals("unyank")) {
				if (!isPut)
					throw new ClientException(SC_METHOD_NOT_ALLOWED);
				setYanked(response, projectId, decodePath(pathSegments.get(3)), decodePath(pathSegments.get(4)), false);
			} else if (isGet) {
				serveIndex(response, projectId, pathSegments);
			} else {
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			}
		} catch (ClientException e) {
			response.setStatus(e.getStatusCode());
			if (e.getMessage() != null)
				writeError(response, e.getMessage());
		}
	}

	private void publish(HttpServletRequest request, HttpServletResponse response, Long projectId, Long buildId) {
		var upload = readPublishBody(request);
		ObjectNode metadata;
		try {
			var metadataNode = objectMapper.readTree(upload.metadata);
			if (metadataNode instanceof ObjectNode)
				metadata = (ObjectNode) metadataNode;
			else
				throw new ClientException(SC_BAD_REQUEST, "Invalid package metadata");
		} catch (IOException e) {
			throw new ClientException(SC_BAD_REQUEST, "Invalid package metadata");
		}

		var name = metadata.path("name").asText(null);
		var version = metadata.path("vers").asText(null);
		if (StringUtils.isBlank(name))
			throw new ClientException(SC_BAD_REQUEST, "Package name not specified");
		if (StringUtils.isBlank(version))
			throw new ClientException(SC_BAD_REQUEST, "Package version not specified");
		if (!name.equals(name.toLowerCase()))
			throw new ClientException(SC_BAD_REQUEST, "Package name should be lower case");

		LockUtils.run(getLockName(projectId, name), () -> transactionService.run(() -> {
			var project = checkProject(projectId, true);
			if (packService.findByNameAndVersion(project, TYPE, name, version) != null) {
				throw new ClientException(SC_CONFLICT,
						String.format("Package already exists (name: %s, version: %s)", name, version));
			}

			var packBlob = packBlobService.load(packBlobService.uploadBlob(projectId, upload.crateFile, null));
			var pack = new Pack();
			pack.setType(TYPE);
			pack.setName(name);
			pack.setVersion(version);
			pack.setPrerelease(version.contains("-"));
			pack.setProject(project);
			pack.setData(new CargoData(upload.metadata, packBlob.getSha256Hash()));
			Build build = null;
			if (buildId != null)
				build = buildService.load(buildId);
			pack.setBuild(build);
			pack.setUser(SecurityUtils.getUser());
			pack.setPublishDate(new Date());
			packService.createOrUpdate(pack, List.of(packBlob), true);
			response.setStatus(SC_OK);
			writeJson(response, Map.of("warnings", Map.of(
					"invalid_categories", List.of(),
					"invalid_badges", List.of(),
					"other", List.of())));
		}));
	}

	private void download(HttpServletResponse response, Long projectId, String name, String version) {
		sessionService.run(() -> {
			var project = checkProject(projectId, false);
			var pack = packService.findByNameAndVersion(project, TYPE, name, version);
			if (pack != null) {
				var data = (CargoData) pack.getData();
				PackBlob packBlob;
				if ((packBlob = packBlobService.checkPackBlob(projectId, data.getSha256BlobHash())) != null) {
					try {
						response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
						response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "-" + version + ".crate\"");
						packBlobService.downloadBlob(packBlob.getProject().getId(), packBlob.getSha256Hash(), response.getOutputStream());
						response.setStatus(SC_OK);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					throw new ExplicitException("Pack blob missing or corrupted: " + data.getSha256BlobHash());
				}
			} else {
				response.setStatus(SC_NOT_FOUND);
			}
		});
	}

	private void setYanked(HttpServletResponse response, Long projectId, String name, String version, boolean yanked) {
		LockUtils.run(getLockName(projectId, name), () -> transactionService.run(() -> {
			var project = checkProject(projectId, true);
			var pack = packService.findByNameAndVersion(project, TYPE, name, version);
			if (pack != null) {
				var data = (CargoData) pack.getData();
				data.setYanked(yanked);
				var packBlob = packBlobService.findBySha256Hash(projectId, data.getSha256BlobHash());
				if (packBlob == null)
					throw new ExplicitException("Pack blob missing or corrupted: " + data.getSha256BlobHash());
				packService.createOrUpdate(pack, List.of(packBlob), false);
				response.setStatus(SC_OK);
				writeJson(response, Map.of("ok", true));
			} else {
				response.setStatus(SC_NOT_FOUND);
			}
		}));
	}

	private void serveIndex(HttpServletResponse response, Long projectId, List<String> pathSegments) {
		if (pathSegments.size() == 1 && pathSegments.get(0).equals("config.json")) {
			sessionService.run(() -> {
				var project = checkProject(projectId, false);
				var baseUrl = settingService.getSystemSetting().getServerUrl() + "/" + project.getPath() + "/~" + HANDLER_ID;
				writeJson(response, Map.of(
						"dl", baseUrl + "/api/v1/crates",
						"api", baseUrl,
						"auth-required", true));
				response.setStatus(SC_OK);
			});
		} else {
			var name = getNameFromIndexPath(pathSegments);
			if (name == null) {
				response.setStatus(SC_NOT_FOUND);
				return;
			}
			sessionService.run(() -> {
				var project = checkProject(projectId, false);
				var packs = packService.queryByName(project, TYPE, name, comparing(Pack::getVersion));
				if (!packs.isEmpty()) {
					response.setContentType("text/plain");
					try {
						for (var pack : packs) {
							response.getOutputStream().write(getIndexEntry(pack));
							response.getOutputStream().write('\n');
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					response.setStatus(SC_OK);
				} else {
					response.setStatus(SC_NOT_FOUND);
				}
			});
		}
	}

	private byte[] getIndexEntry(Pack pack) {
		var data = (CargoData) pack.getData();
		try {
			var publishMetadata = (ObjectNode) objectMapper.readTree(data.getMetadata());
			var indexEntry = objectMapper.createObjectNode();
			indexEntry.put("name", publishMetadata.path("name").asText());
			indexEntry.put("vers", publishMetadata.path("vers").asText());
			var depsNode = publishMetadata.path("deps");
			indexEntry.set("deps", depsNode instanceof ArrayNode ? toIndexDeps((ArrayNode) depsNode) : objectMapper.createArrayNode());
			indexEntry.put("cksum", data.getSha256BlobHash());
			indexEntry.set("features", publishMetadata.path("features"));
			indexEntry.put("yanked", data.isYanked());
			if (publishMetadata.hasNonNull("links"))
				indexEntry.set("links", publishMetadata.get("links"));
			if (publishMetadata.hasNonNull("rust_version"))
				indexEntry.set("rust_version", publishMetadata.get("rust_version"));
			indexEntry.put("v", 2);
			return objectMapper.writeValueAsBytes(indexEntry);
		} catch (IOException e) {
			throw new ClientException(SC_BAD_REQUEST, "Invalid publish request body");
		}
	}

	private ArrayNode toIndexDeps(ArrayNode publishDeps) {
		var indexDeps = objectMapper.createArrayNode();
		for (var publishDep : publishDeps) {
			var dep = objectMapper.createObjectNode();
			var explicitName = publishDep.path("explicit_name_in_toml");
			dep.put("name", explicitName.isMissingNode() || explicitName.isNull()
					? publishDep.path("name").asText()
					: explicitName.asText());
			dep.put("req", publishDep.path("version_req").asText());
			dep.set("features", publishDep.path("features"));
			dep.put("optional", publishDep.path("optional").asBoolean(false));
			dep.put("default_features", publishDep.path("default_features").asBoolean(true));
			dep.set("target", publishDep.path("target"));
			dep.put("kind", publishDep.path("kind").asText("normal"));
			dep.set("registry", publishDep.path("registry"));
			if (!explicitName.isMissingNode() && !explicitName.isNull())
				dep.put("package", publishDep.path("name").asText());
			else
				dep.putNull("package");
			indexDeps.add(dep);
		}
		return indexDeps;
	}

	private PublishBody readPublishBody(HttpServletRequest request) {
		try (var is = request.getInputStream()) {
			var metadataLength = readIntLE(is);
			if (metadataLength < 0 || metadataLength > MAX_METADATA_SIZE)
				throw new ClientException(SC_NOT_ACCEPTABLE, "Package metadata exceeds maximum size: " + MAX_METADATA_SIZE);
			var metadata = readBytes(is, metadataLength);
			var crateLength = readIntLE(is);
			if (crateLength < 0 || crateLength > MAX_CRATE_SIZE)
				throw new ClientException(SC_NOT_ACCEPTABLE, "Crate archive exceeds maximum size: " + MAX_CRATE_SIZE);
			var crateFile = readBytes(is, crateLength);
			return new PublishBody(metadata, crateFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private int readIntLE(InputStream is) throws IOException {
		var b1 = is.read();
		var b2 = is.read();
		var b3 = is.read();
		var b4 = is.read();
		if ((b1 | b2 | b3 | b4) < 0)
			throw new EOFException();
		return b1 | b2 << 8 | b3 << 16 | b4 << 24;
	}

	private byte[] readBytes(InputStream is, int length) throws IOException {
		var bytes = new byte[length];
		var offset = 0;
		while (offset < length) {
			var count = is.read(bytes, offset, length - offset);
			if (count == -1)
				throw new EOFException();
			offset += count;
		}
		return bytes;
	}

	private String getNameFromIndexPath(List<String> pathSegments) {
		if (pathSegments.isEmpty())
			return null;
		var name = decodePath(pathSegments.get(pathSegments.size() - 1));
		var lowerName = name.toLowerCase();
		if (lowerName.length() == 1) {
			return pathSegments.equals(newArrayList("1", lowerName)) ? name : null;
		} else if (lowerName.length() == 2) {
			return pathSegments.equals(newArrayList("2", lowerName)) ? name : null;
		} else if (lowerName.length() == 3) {
			return pathSegments.equals(newArrayList("3", lowerName.substring(0, 1), lowerName)) ? name : null;
		} else {
			return pathSegments.equals(newArrayList(lowerName.substring(0, 2), lowerName.substring(2, 4), lowerName))
					? name
					: null;
		}
	}

	private void writeJson(HttpServletResponse response, Object object) {
		response.setContentType(MediaType.APPLICATION_JSON);
		try {
			objectMapper.writeValue(response.getOutputStream(), object);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeError(HttpServletResponse response, String message) {
		writeJson(response, Map.of("errors", List.of(Map.of("detail", message))));
	}

	private String getLockName(Long projectId, String name) {
		return "update-pack:" + projectId + ":" + TYPE + ":" + name;
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
	public String getApiKey(HttpServletRequest request) {
		var authzHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authzHeader != null) {
			if (authzHeader.toLowerCase().startsWith("bearer "))
				return StringUtils.substringAfter(authzHeader, " ");
			else
				return authzHeader;
		} else {
			return null;
		}
	}

	@Override
	public List<String> normalize(List<String> pathSegments) {
		return pathSegments;
	}

	private static class PublishBody {

		private final byte[] metadata;

		private final byte[] crateFile;

		private PublishBody(byte[] metadata, byte[] crateFile) {
			this.metadata = metadata;
			this.crateFile = crateFile;
		}
	}
}
