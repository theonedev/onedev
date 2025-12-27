package io.onedev.server.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.service.UrlService;

public class BuildHelper {
    
    private static ObjectMapper getObjectMapper() {
        return OneDev.getInstance(ObjectMapper.class);
    }

    private static UrlService getUrlService() {
        return OneDev.getInstance(UrlService.class);
    }

    public static Map<String, Object> getSummary(Project currentProject, Build build) {        
        var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        var summary = getObjectMapper().convertValue(build, typeReference);
        summary.remove("id");
        summary.remove("uuid");
        summary.remove("numberScopeId");
        summary.remove("workspacePath");
        summary.remove("checkoutPaths");
        summary.remove("submitSequence");
        summary.remove("finishTimeGroups");        
        summary.put("reference", build.getReference().toString(currentProject));
        summary.remove("submitterId");
        summary.put("submitter", build.getSubmitter().getName());
        summary.remove("cancellerId");
        if (build.getCanceller() != null)
            summary.put("canceller", build.getCanceller().getName());
        summary.remove("requestId");
        if (build.getRequest() != null)
            summary.put("pullRequest", build.getRequest().getReference().toString(currentProject));
        summary.remove("issueId");
        if (build.getIssue() != null)
            summary.put("issue", build.getIssue().getReference().toString(currentProject));
        summary.remove("agentId");
        if (build.getAgent() != null)
            summary.put("agent", build.getAgent().getName());
        
        summary.put("project", build.getProject().getPath());
        return summary;
    }

    public static Map<String, Object> getDetail(Project currentProject, Build build) {
        var detail = getSummary(currentProject, build);
        detail.put("params", build.getParamMap());
        detail.put("labels", build.getLabels().stream().map(it->it.getSpec().getName()).collect(Collectors.toList()));
        detail.put("link", getUrlService().urlFor(build, true));
        return detail;
    }
    
}
