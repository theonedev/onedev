package io.onedev.server.plugin.pack.helm;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.yaml.snakeyaml.Yaml;

import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.PackBlobManager;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.exception.DataTooLargeException;
import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.pack.PackService;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.Pair;

@Singleton
public class HelmPackService implements PackService {

    public static final String SERVICE_ID = "helm";

    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final ProjectManager projectManager;

    private final PackManager packManager;

    private final PackBlobManager packBlobManager;

    private final SessionManager sessionManager;

    private final TransactionManager transactionManager;

    private final BuildManager buildManager;

    @Inject
    public HelmPackService(ProjectManager projectManager, PackManager packManager, 
            PackBlobManager packBlobManager, SessionManager sessionManager, 
            TransactionManager transactionManager, BuildManager buildManager) {
        this.projectManager = projectManager;
        this.packManager = packManager;
        this.packBlobManager = packBlobManager;
        this.sessionManager = sessionManager;
        this.transactionManager = transactionManager;
        this.buildManager = buildManager;
    }

    @Override
    public String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response, 
            Long projectId, Long buildId, List<String> pathSegments) {
        if (request.getMethod().equals("GET")) {
            if (pathSegments.size() == 1) {
                var fileName = pathSegments.get(0);
                if (fileName.equals("index.yaml")) {
                    var index = new HashMap<String, Object>();
                    index.put("apiVersion", "v1");
                    index.put("generated", new Date());

                    var entries = new HashMap<String, List<Map<String, Object>>>();
                    sessionManager.run(() -> {
                        var project = checkProject(projectId, false);
                        var packs = packManager.query(project, HelmPackSupport.TYPE, null);
                        
                        for (var pack : packs) {
                            var data = (HelmData) pack.getData();
                            var entry = entries.computeIfAbsent((String)data.getMetadata().get("name"), k -> new ArrayList<>());
                            
                            var versionInfo = new HashMap<String, Object>();      
                            versionInfo.putAll(data.getMetadata());                  
                            versionInfo.put("created", pack.getPublishDate());                        
                            versionInfo.put("urls", List.of(getDownloadUrl(pack)));
                            entry.add(versionInfo);
                        }
                    });
                    
                    index.put("entries", entries);
                    response.setContentType("application/yaml");
                    try (var writer = response.getWriter()) {
                        new Yaml().dump(index, writer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    var packBlobInfo = sessionManager.call(() -> {
                        var project = checkProject(projectId, false);
                        var fileNameWithoutSuffix = StringUtils.substringBeforeLast(fileName, ".");
                        var chartName = StringUtils.substringBeforeLast(fileNameWithoutSuffix, "-");
                        var chartVersion = StringUtils.substringAfterLast(fileNameWithoutSuffix, "-");
                        
                        var pack = packManager.findByNameAndVersion(project, HelmPackSupport.TYPE, chartName, chartVersion);
                        if (pack != null && !pack.getBlobReferences().isEmpty()) {
                            var packBlob = pack.getBlobReferences().iterator().next().getPackBlob();
                            return new Pair<>(packBlob.getSha256Hash(), packBlob.getSize());
                        } else {
                            return null;
                        }
                    });
                    if (packBlobInfo != null) {
                        try (var os = response.getOutputStream()) {
                            response.setContentType("application/gzip");
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                            response.setContentLengthLong(packBlobInfo.getRight());
                            packBlobManager.downloadBlob(projectId, packBlobInfo.getLeft(), os);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        response.setStatus(SC_NOT_FOUND);
                    }
                }
            } else {
                response.setStatus(SC_NOT_FOUND);
            }
        } else if (request.getMethod().equals("POST")) {            
            sessionManager.run(() -> {
                checkProject(projectId, true);
            });
            var baos = new ByteArrayOutputStream();
            var contentType = request.getHeader("Content-Type");
            if (contentType != null && (contentType.contains("multipart/form-data") || contentType.contains("application/x-www-form-urlencoded"))) {
                try {
                    var upload = new ServletFileUpload();
                    var items = upload.getItemIterator(request);
                    if (items.hasNext()) {
                        var item = items.next();
                        try (var is = item.openStream()) {
                            IOUtils.copyWithMaxSize(is, baos, MAX_FILE_SIZE);
                        } catch (DataTooLargeException e) {
                            throw new ClientException(SC_BAD_REQUEST, "Chart archive is too large");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw new ClientException(SC_BAD_REQUEST, "Chart archive not found");
                    }
                } catch (FileUploadException|IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try (var is = request.getInputStream()) {
                    IOUtils.copyWithMaxSize(is, baos, MAX_FILE_SIZE);
                } catch (DataTooLargeException e) {
                    throw new ClientException(SC_BAD_REQUEST, "Chart archive is too large");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }    
            }
            Map<String, Object> metadata = null;
            var bytes = baos.toByteArray();

            try (var is = new TarInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)))) {
                TarEntry entry;
                while ((entry = is.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    if (entryName.equals("Chart.yaml") || entryName.endsWith("/Chart.yaml")) {
                        byte[] content = new byte[(int) entry.getSize()];
                        is.read(content);
                        metadata = new Yaml().load(new ByteArrayInputStream(content));
                        break;
                    }
                }                
            } catch (IOException e) {
                throw new ClientException(SC_BAD_REQUEST, "Error reading chart archive: " + e.getMessage());
            }

            if (metadata == null) {
                throw new ClientException(SC_BAD_REQUEST, "Chart.yaml not found in the archive");
            }
            
            var chartName = (String) metadata.get("name");
            var chartVersion = (String) metadata.get("version");
            
            if (chartName == null || chartVersion == null) {
                throw new ClientException(SC_BAD_REQUEST, "Chart name or version not specified");
            }            

            var finalMetadata = metadata;
			var lockName = "update-pack:" + projectId + ":" + HelmPackSupport.TYPE + ":" + chartName + ":" + chartVersion;
            LockUtils.run(lockName, () -> transactionManager.run(() -> {
                var project = projectManager.load(projectId);

                PackBlob packBlob = packBlobManager.load(packBlobManager.uploadBlob(projectId, bytes, null));
                var data = new HelmData(finalMetadata, packBlob.getSha256Hash());
                Pack pack = packManager.findByNameAndVersion(project, HelmPackSupport.TYPE, chartName, chartVersion);
                if (pack == null) {
                    pack = new Pack();
                    pack.setProject(project);
                    pack.setType(HelmPackSupport.TYPE);
                    pack.setName(chartName);
                    pack.setVersion(chartVersion);
                }
                pack.setData(data);
                Build build = null;
                if (buildId != null)
                    build = buildManager.load(buildId);
                pack.setBuild(build);
                pack.setUser(SecurityUtils.getUser());
                pack.setPublishDate(new Date());

                packManager.createOrUpdate(pack, List.of(packBlob), true);
                response.setStatus(SC_CREATED);
            }));
        } else {
            throw new ClientException(SC_METHOD_NOT_ALLOWED, "Method not allowed");
        }
    }

    @Override
    public String getApiKey(HttpServletRequest request) {
        return null;
    }

	private Project checkProject(Long projectId, boolean needsToWrite) {
		var project = projectManager.load(projectId);
		if (!project.isPackManagement()) {
			throw new ClientException(SC_NOT_ACCEPTABLE, "Package management not enabled for project '" + project.getPath() + "'");
		} else if (needsToWrite && !SecurityUtils.canWritePack(project)) {
			throw new UnauthorizedException("No package write permission for project: " + project.getPath());
		} else if (!needsToWrite && !SecurityUtils.canReadPack(project)) {
			throw new UnauthorizedException("No package read permission for project: " + project.getPath());
		}
		return project;
	}

	private String getDownloadUrl(Pack pack) {
		return String.format("/%s/~helm/%s-%s.tgz",
				pack.getProject().getPath(), pack.getName(), pack.getVersion());
	}

	@Override
	public List<String> normalize(List<String> pathSegments) {
        pathSegments = new ArrayList<>(pathSegments);
        if (pathSegments.get(pathSegments.size() - 1).equals("charts")) {
            pathSegments.remove(pathSegments.size() - 1);
            if (pathSegments.get(0).equals("api")) 
                pathSegments.remove(0);
        }        
		return pathSegments;
	}

}
