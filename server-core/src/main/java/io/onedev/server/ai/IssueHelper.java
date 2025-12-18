package io.onedev.server.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.service.UrlService;

public class IssueHelper {
    
    private static ObjectMapper getObjectMapper() {
        return OneDev.getInstance(ObjectMapper.class);
    }

    private static UrlService getUrlService() {
        return OneDev.getInstance(UrlService.class);
    }

    public static Map<String, Object> getSummary(Project currentProject, Issue issue) {        
        var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        var summary = getObjectMapper().convertValue(issue, typeReference);
        summary.remove("id");
        summary.remove("stateOrdinal");
        summary.remove("uuid");
        summary.remove("messageId");
        summary.remove("pinDate");
        summary.remove("boardPosition");
        summary.remove("numberScopeId");
        summary.put("reference", issue.getReference().toString(currentProject));
        summary.remove("submitterId");
        summary.put("submitter", issue.getSubmitter().getName());
        summary.put("Project", issue.getProject().getPath());
        summary.remove("lastActivity");
        for (var it = summary.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            if (entry.getKey().endsWith("Count"))
                it.remove();
        }
        return summary;
    }

    public static List<Map<String, Object>> getComments(Issue issue) {
        var comments = new ArrayList<Map<String, Object>>();
        issue.getComments().stream().sorted(Comparator.comparing(IssueComment::getId)).forEach(comment -> {
            var commentMap = new HashMap<String, Object>();
            commentMap.put("user", comment.getUser().getName());
            commentMap.put("date", comment.getDate());
            commentMap.put("content", comment.getContent());
            comments.add(commentMap);
        });
        return comments;
    }

    public static Map<String, Object> getDetail(Project currentProject, Issue issue) {
        var detail = getSummary(currentProject, issue);
        for (var entry : issue.getFieldInputs().entrySet()) {
            detail.put(entry.getKey(), entry.getValue().getValues());
        }
        
        Map<String, Collection<String>> linkedIssues = new HashMap<>();
        for (var link: issue.getTargetLinks()) {
            linkedIssues.computeIfAbsent(link.getSpec().getName(), k -> new ArrayList<>())
                    .add(link.getTarget().getReference().toString(currentProject));
        }
        for (var link : issue.getSourceLinks()) {
            if (link.getSpec().getOpposite() != null) {
                linkedIssues.computeIfAbsent(link.getSpec().getOpposite().getName(), k -> new ArrayList<>())
                        .add(link.getSource().getReference().toString(currentProject));
            } else {
                linkedIssues.computeIfAbsent(link.getSpec().getName(), k -> new ArrayList<>())
                        .add(link.getSource().getReference().toString(currentProject));
            }
        }
        detail.putAll(linkedIssues);
        detail.put("link", getUrlService().urlFor(issue, true));

        return detail;
    }
}
