package io.onedev.server.plugin.pack.gem;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
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
import io.onedev.server.plugin.pack.gem.marshal.Marshaller;
import io.onedev.server.plugin.pack.gem.marshal.RubyObject;
import io.onedev.server.plugin.pack.gem.marshal.UserDefined;
import io.onedev.server.plugin.pack.gem.marshal.UserMarshal;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.UrlUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.fileupload.util.Streams;
import org.apache.shiro.authz.UnauthorizedException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.plugin.pack.gem.GemPackSupport.TYPE;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static io.onedev.server.util.IOUtils.copyWithMaxSize;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.MAX_VALUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.*;

@Singleton
public class GemPackHandler implements PackHandler {
	public static final String HANDLER_ID = "rubygems";
	
	private static final int MAX_METADATA_SIZE = 10000000;
	
	private final SessionService sessionService;
	
	private final TransactionService transactionService;
	
	private final PackBlobService packBlobService;
	
	private final PackService packService;
	
	private final ProjectService projectService;
	
	private final BuildService buildService;
	
	@Inject
	public GemPackHandler(SessionService sessionService, TransactionService transactionService,
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
	
	@SuppressWarnings("deprecation")
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, Long projectId, 
						Long buildId, List<String> pathSegments) {
		var method = request.getMethod();
		
		var isGet = method.equals("GET");
		var isPost = method.equals("POST");
		var isDelete = method.equals("DELETE");
		
		if (pathSegments.equals(newArrayList("api", "v1", "gems"))) {
			if (!isPost)
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			
			sessionService.run(() -> {
				checkProject(projectId, true);
			});
			var tempFile = FileUtils.createTempFile("upload", "gem");
			try {
				try (var os = new BufferedOutputStream(new FileOutputStream(tempFile), BUFFER_SIZE)) {
					IOUtils.copy(request.getInputStream(), os);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				byte[] metadataBytes = null;
				try (var is = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(tempFile), BUFFER_SIZE))) {
					TarArchiveEntry entry;
					while ((entry = is.getNextTarEntry()) != null) {
						if (entry.getName().equals("metadata.gz")) {
							var baos = new ByteArrayOutputStream();
							copyWithMaxSize(new GZIPInputStream(is), baos, MAX_METADATA_SIZE);
							metadataBytes = baos.toByteArray();
							break;
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				if (metadataBytes == null)
					throw new ClientException(SC_BAD_REQUEST, "Metadata not found");

				String name = null;
				String version = null;
				String platform = null;
				var metadata = (MappingNode) new Yaml().compose(new InputStreamReader(new ByteArrayInputStream(metadataBytes)));
				for (var tuple : metadata.getValue()) {
					var keyNode = (ScalarNode) tuple.getKeyNode();
					if (keyNode.getValue().equals("name")) {
						name = ((ScalarNode) tuple.getValueNode()).getValue();
					} else if (keyNode.getValue().equals("version")) {
						var versionNode = (MappingNode) tuple.getValueNode();
						for (var versionTuple : versionNode.getValue()) {
							var versionKeyNode = (ScalarNode) versionTuple.getKeyNode();
							if (versionKeyNode.getValue().equals("version")) {
								version = ((ScalarNode) versionTuple.getValueNode()).getValue();
								break;
							}
						}
					} else if (keyNode.getValue().equals("platform")) {
						platform = ((ScalarNode) tuple.getValueNode()).getValue();
					}
				}
				if (name == null)
					throw new ClientException(SC_BAD_REQUEST, "Package name not found in metadata");
				if (version == null)
					throw new ClientException(SC_BAD_REQUEST, "Package version not found in metadata");

				String finalName = name;
				String finalVersion = version;
				String finalPlatform = platform;
				byte[] finalMetadataBytes = metadataBytes;
				LockUtils.run(getLockName(projectId, name), () -> transactionService.run(() -> {
					var project = projectService.load(projectId);

					PackBlob packBlob;
					try (var is = new FileInputStream(tempFile)) {
						packBlob = packBlobService.load(packBlobService.uploadBlob(projectId, is, null));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					GemData data;
					var pack = packService.findByNameAndVersion(project, TYPE, finalName, finalVersion);
					if (pack == null) {
						pack = new Pack();
						pack.setType(TYPE);
						pack.setName(finalName);
						pack.setVersion(finalVersion);
						pack.setPrerelease(isPrerelease(finalVersion));
						pack.setProject(project);
						data = new GemData(finalMetadataBytes, finalPlatform, new LinkedHashMap<>());
						pack.setData(data);
					} else {
						data = (GemData) pack.getData();
					}

					Build build = null;
					if (buildId != null)
						build = buildService.load(buildId);
					pack.setBuild(build);
					pack.setUser(SecurityUtils.getUser());
					pack.setPublishDate(new Date());

					String fileName;
					if (StringUtils.isBlank(finalPlatform) || finalPlatform.equals("ruby"))
						fileName = String.format("%s-%s.gem", finalName, finalVersion).toLowerCase();
					else
						fileName = String.format("%s-%s-%s.gem", finalName, finalVersion, finalPlatform).toLowerCase();

					if (data.getSha256BlobHashes().containsKey(fileName)) {
						var errorMessage = String.format("Package already exists (name: %s, version: %s)", finalName, finalVersion);
						throw new ClientException(SC_CONFLICT, errorMessage);
					}
					data.getSha256BlobHashes().put(fileName, packBlob.getSha256Hash());

					var packBlobs = data.getSha256BlobHashes().values().stream()
							.map(hash -> packBlobService.findBySha256Hash(projectId, hash))
							.filter(Objects::nonNull)
							.collect(toList());
					packService.createOrUpdate(pack, packBlobs, data.getSha256BlobHashes().size() == 1);
					response.setStatus(SC_OK);
				}));
			} finally {
				FileUtils.deleteFile(tempFile);
			}
		} else if (pathSegments.equals(newArrayList("api", "v1", "gems", "yank"))) {
			if (!isDelete)
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			String name;
			String version;
			try {
				var params = HttpUtils.parseQueryString(Streams.asString(request.getInputStream(), UTF_8.name()));
				name = params.get("gem_name")[0];
				version = params.get("version")[0];
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			LockUtils.run(getLockName(projectId, name), () -> transactionService.run(() -> {
				var project = checkProject(projectId, true);
				var pack = packService.findByNameAndVersion(project, TYPE, name, version);
				if (pack != null) {
					packService.delete(pack);
					response.setStatus(SC_OK);
				} else {
					response.setStatus(SC_NOT_FOUND);
				}
			}));
		} else if (pathSegments.equals(newArrayList("latest_specs.4.8.gz"))) {
			if (!isGet)
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			sessionService.run(() -> {
				var project = checkProject(projectId, false);
				var latestPacks = packService.queryLatests(project, TYPE, null, false, 0, MAX_VALUE);
				sendPacks(response, latestPacks);
			});
		} else if (pathSegments.equals(newArrayList("specs.4.8.gz"))) {
			if (!isGet)
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			sessionService.run(() -> {
				var project = checkProject(projectId, false);
				sendPacks(response, packService.query(project, TYPE, false));
			});
		} else if (pathSegments.equals(newArrayList("prerelease_specs.4.8.gz"))) {
			if (!isGet)
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			sessionService.run(() -> {
				var project = checkProject(projectId, false);
				sendPacks(response, packService.query(project, TYPE, true));
			});
		} else if (pathSegments.size() == 3 && pathSegments.subList(0, 2).equals(newArrayList("quick", "Marshal.4.8"))) {
			if (!isGet)
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			var fileName = UrlUtils.decodePath(pathSegments.get(2));
			if (!fileName.endsWith(".gemspec.rz"))
				throw new ClientException(SC_NOT_IMPLEMENTED);
			var nameAndVersion = fileName.substring(0, fileName.length() - ".gemspec.rz".length());
			
			sessionService.run(() -> {
				var project = checkProject(projectId, false);
				var pack = findPack(project, nameAndVersion);
				if (pack != null) {
					var data = (GemData) pack.getData();
					var metadata = (MappingNode) new Yaml().compose(new InputStreamReader(new ByteArrayInputStream(data.getMetadata())));

					var specValue = new ArrayList<>();

					var rubygemsVersion = "3.5.4";
					var specVersion = 4;
					String summary = null;
					String email = null;
					var authors = new ArrayList<>();
					String description = null;
					String homePage = null;
					var licenses = new ArrayList<>();
					
					UserMarshal requiredRubyVersion = null;
					UserMarshal requiredRubygemsVersion = null;
					List<RubyObject> dependencies = new ArrayList<>();
					
					for (var tuple: metadata.getValue()) {
						switch (((ScalarNode) tuple.getKeyNode()).getValue()) {
							case "rubygems_version":
								rubygemsVersion = ((ScalarNode) tuple.getValueNode()).getValue();
								break;
							case "specification_version":
								specVersion = Integer.parseInt(((ScalarNode) tuple.getValueNode()).getValue());
								break;
							case "summary":
								summary = ((ScalarNode) tuple.getValueNode()).getValue();
								break;
							case "email":
								email = ((ScalarNode) tuple.getValueNode()).getValue();
								break;
							case "authors":
								for (var authorNode : ((SequenceNode)tuple.getValueNode()).getValue())
									authors.add(((ScalarNode) authorNode).getValue());
								break;
							case "description":
								description = ((ScalarNode) tuple.getValueNode()).getValue();
								break;
							case "homepage":
								homePage = ((ScalarNode) tuple.getValueNode()).getValue();
								break;
							case "licenses":
								for (var licenseNode : ((SequenceNode)tuple.getValueNode()).getValue())
									licenses.add(((ScalarNode) licenseNode).getValue());
								break;
							case "required_ruby_version":
								requiredRubyVersion = getGemRequirement((MappingNode) tuple.getValueNode());
								break;
							case "required_rubygems_version":
								requiredRubygemsVersion = getGemRequirement((MappingNode) tuple.getValueNode());
								break;
							case "dependencies":
								for (var dependencyNode: ((SequenceNode)tuple.getValueNode()).getValue())
									dependencies.add(getGemDependency((MappingNode) dependencyNode));
								break;
						}
					}

					specValue.add(rubygemsVersion);
					specValue.add(specVersion);
					specValue.add(pack.getName());
					specValue.add(getGemVersion(pack.getVersion()));
					specValue.add(null);
					specValue.add(summary);
					specValue.add(requiredRubyVersion);
					specValue.add(requiredRubygemsVersion);
					specValue.add(data.getPlatform());
					specValue.add(dependencies);
					specValue.add(null);
					specValue.add(email);
					specValue.add(authors);
					specValue.add(description);
					specValue.add(homePage);
					specValue.add(true);
					specValue.add(data.getPlatform());
					specValue.add(null);
					specValue.add(licenses);

					var spec = new UserDefined("Gem::Specification", specValue);

					response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
					try {
						var dos = new DeflaterOutputStream(response.getOutputStream());
						new Marshaller(dos).marshal(spec);
						dos.finish();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					response.setStatus(SC_OK);
				} else {
					response.setStatus(SC_NOT_FOUND);
				}
			});
		} else if (pathSegments.size() == 2 && pathSegments.get(0).equals("gems")) {
			if (!isGet)
				throw new ClientException(SC_METHOD_NOT_ALLOWED);
			
			var fileName = UrlUtils.decodePath(pathSegments.get(1));
			if (!fileName.endsWith(".gem"))
				throw new ClientException(SC_NOT_IMPLEMENTED);
			
			var nameAndVersion = fileName.substring(0, fileName.length() - ".gem".length());
			sessionService.run(() -> {
				var project = checkProject(projectId, false);
				var pack = findPack(project, nameAndVersion);
				if (pack != null) {
					var data = (GemData) pack.getData();
					var sha256BlobHash = data.getSha256BlobHashes().get(fileName);
					if (sha256BlobHash != null) {
						PackBlob packBlob;
						if ((packBlob = packBlobService.findBySha256Hash(projectId, sha256BlobHash)) != null) {
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
	
	private boolean isPrerelease(String version) {
		for (var ch: version.toCharArray()) {
			if (Character.isLetter(ch))
				return true;
		}
		return false;
	}
	
	private void sendPacks(HttpServletResponse response, List<Pack> packs) {
		var specs = new ArrayList<>();
		for (var pack: packs) {
			var spec = new ArrayList<>();
			spec.add(pack.getName());
			spec.add(getGemVersion(pack.getVersion()));
			var data = (GemData) pack.getData();
			spec.add(data.getPlatform());
			specs.add(spec);
		}

		response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		try {
			var zos = new GZIPOutputStream(response.getOutputStream());
			new Marshaller(zos).marshal(specs);
			zos.finish();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		response.setStatus(SC_OK);
	}
	
	@Nullable
	private Pack findPack(Project project, String nameAndVersion) {
		var dashIndex = nameAndVersion.indexOf('-');
		while (dashIndex != -1) {
			var name = nameAndVersion.substring(0, dashIndex);
			var version = nameAndVersion.substring(dashIndex + 1, nameAndVersion.length());
			var pack = packService.findByNameAndVersion(project, TYPE, name, version);
			if (pack != null) 
				return pack;
			dashIndex = nameAndVersion.indexOf('-', dashIndex + 1);
		}
		return null;
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
		if (!project.isPackManagement()) {
			throw new ClientException(SC_NOT_ACCEPTABLE, "Package management not enabled for project '" + project.getPath() + "'");
		} else if (needsToWrite && !SecurityUtils.canWritePack(project)) {
			throw new UnauthorizedException("No package write permission for project: " + project.getPath());
		} else if (!needsToWrite && !SecurityUtils.canReadPack(project)) {
			throw new UnauthorizedException("No package read permission for project: " + project.getPath());
		}
		return project;
	}

	private UserMarshal getGemVersion(String version) {
		return new UserMarshal("Gem::Version", newArrayList(version));
	}
	
	private UserMarshal getGemRequirement(Map<String, UserMarshal> requiredVersions) {
		var requirements = new ArrayList<>();
		for (var entry: requiredVersions.entrySet()) 
			requirements.add(newArrayList(entry.getKey(), entry.getValue()));
		var value = new ArrayList<>();
		value.add(requirements);
		return new UserMarshal("Gem::Requirement", value);	
	}
	
	private UserMarshal getGemVersion(MappingNode node) {
		checkArgument(node.getTag().getValue().equals("!ruby/object:Gem::Version"));	
		for (var tuple: node.getValue()) {
			if (((ScalarNode)tuple.getKeyNode()).getValue().equals("version"))
				return getGemVersion(((ScalarNode)tuple.getValueNode()).getValue());
		}
		throw new ExplicitException("Unable to find version node");
	}
	
	private UserMarshal getGemRequirement(MappingNode node) {
		checkArgument(node.getTag().getValue().equals("!ruby/object:Gem::Requirement"));
		for (var tuple: node.getValue()) {
			if (((ScalarNode)tuple.getKeyNode()).getValue().equals("requirements")) {
				var requiredVersions = new LinkedHashMap<String, UserMarshal>();
				for (var requirementNode: ((SequenceNode)tuple.getValueNode()).getValue()) {
					var operatorAndVersion = ((SequenceNode)requirementNode).getValue();
					var operator = ((ScalarNode)operatorAndVersion.get(0)).getValue();
					var gemVersion = getGemVersion((MappingNode) operatorAndVersion.get(1));
					requiredVersions.put(operator, gemVersion);
				}
				return getGemRequirement(requiredVersions);
			}
		}
		throw new ExplicitException("Unable to find requirements node");
	}
	
	private RubyObject getGemDependency(MappingNode node) {
		checkArgument(node.getTag().getValue().equals("!ruby/object:Gem::Dependency"));

		var members = new HashMap<String, Object>();
		
		for (var tuple: node.getValue()) {
			var keyValue = ((ScalarNode)tuple.getKeyNode()).getValue();
			switch (keyValue) {
				case "name":
				case "type":
					members.put("@" + keyValue, ((ScalarNode)tuple.getValueNode()).getValue());	
					break;
				case "requirement":
				case "version_requirements":
					members.put("@" + keyValue, getGemRequirement((MappingNode) tuple.getValueNode()));
					break;
				case "prerelease":
					members.put("@prerelease", parseBoolean(((ScalarNode)tuple.getValueNode()).getValue()));
					break;
			}
		}
		
		return new RubyObject("Gem::Dependency", members);
	}

	@Override
	public List<String> normalize(List<String> pathSegments) {
		return pathSegments;
	}
	
}
