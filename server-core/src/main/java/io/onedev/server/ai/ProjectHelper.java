package io.onedev.server.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.service.UrlService;

public class ProjectHelper {
    
    private static UrlService getUrlService() {
        return OneDev.getInstance(UrlService.class);
    }

	public static Map<String, Object> getDetail(Project project) {
		var data = getSummary(project);
		data.put("childPaths", project.getChildren().stream()
			.map(it -> it.getPath())
			.collect(Collectors.toList()));
		return data;
	}

    public static Map<String, Object> getSummary(Project project) {
        var summary = new LinkedHashMap<String, Object>();
        summary.put("id", project.getId());
        summary.put("key", project.getKey());
        summary.put("name", project.getName());
		if (project.getParent() != null)
			summary.put("parentPath", project.getParent().getPath());
		else
			summary.put("parentPath", null);
        summary.put("path", project.getPath());
        summary.put("description", project.getDescription());
        summary.put("codeManagement", project.isCodeManagement());
        summary.put("packManagement", project.isPackManagement());
        summary.put("issueManagement", project.isIssueManagement());
        summary.put("timeTracking", project.isTimeTracking());
        summary.put("defaultBranch", project.getDefaultBranch());
        summary.put("labels", project.getLabels().stream()
                .map(it -> it.getSpec().getName())
                .collect(Collectors.toList()));
        summary.put("link", getUrlService().urlFor(project, true));
        return summary;
    }

}
