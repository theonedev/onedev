package io.onedev.server.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.web.UrlService;

public class PullRequestHelper {
    
    private static ObjectMapper getObjectMapper() {
        return OneDev.getInstance(ObjectMapper.class);
    }

    private static UrlService getUrlService() {
        return OneDev.getInstance(UrlService.class);
    }

    public static Map<String, Object> getSummary(Project currentProject, 
            PullRequest pullRequest, boolean checkMergeConditionIfOpen) {        
        var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        var summary = getObjectMapper().convertValue(pullRequest, typeReference);
        summary.remove("id");
        if (pullRequest.isOpen() && checkMergeConditionIfOpen) {
            var errorMessage = pullRequest.checkMergeCondition();
            if (errorMessage != null) 
                summary.put("status", PullRequest.Status.OPEN.name() + " (" + errorMessage + ")");
            else
                summary.put("status", PullRequest.Status.OPEN.name() + " (ready to merge)");
        } 
        summary.remove("uuid");
        summary.remove("buildCommitHash");
        summary.remove("submitTimeGroups");
        summary.remove("closeTimeGroups");
        summary.remove("checkError");
        summary.remove("numberScopeId");
        summary.put("reference", pullRequest.getReference().toString(currentProject));
        summary.remove("submitterId");
        summary.put("submitter", pullRequest.getSubmitter().getName());
        summary.put("targetProject", pullRequest.getTarget().getProject().getPath());
        if (pullRequest.getSourceProject() != null)
            summary.put("sourceProject", pullRequest.getSourceProject().getPath());
        summary.remove("codeCommentsUpdateDate");
        summary.remove("lastActivity");
        for (var it = summary.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            if (entry.getKey().endsWith("Count"))
                it.remove();
        }
        return summary;
    }

    public static Map<String, Object> getDetail(Project currentProject, PullRequest pullRequest) {
        var detail = getSummary(currentProject, pullRequest, true);        
        detail.put("headCommitHash", pullRequest.getLatestUpdate().getHeadCommitHash());
        detail.put("assignees", pullRequest.getAssignees().stream().map(it->it.getName()).collect(Collectors.toList()));
        var reviews = new ArrayList<Map<String, Object>>();
        for (var review : pullRequest.getReviews()) {
            if (review.getStatus() == PullRequestReview.Status.EXCLUDED)
                continue;
            var reviewMap = new HashMap<String, Object>();
            reviewMap.put("reviewer", review.getUser().getName());
            reviewMap.put("status", review.getStatus());
            reviews.add(reviewMap);
        }        
        detail.put("reviews", reviews);
        var builds = new ArrayList<String>();
        for (var build : pullRequest.getBuilds()) {
            builds.add(build.getReference().toString(currentProject) + " (job: " + build.getJobName() + ", status: " + build.getStatus() + ")");
        }
        detail.put("builds", builds);
        detail.put("labels", pullRequest.getLabels().stream().map(it->it.getSpec().getName()).collect(Collectors.toList()));
        detail.put("link", getUrlService().urlFor(pullRequest, true));

        return detail;
    }

    public static List<Map<String, Object>> getComments(PullRequest pullRequest) {
        var comments = new ArrayList<Map<String, Object>>();
        pullRequest.getComments().stream().sorted(Comparator.comparing(PullRequestComment::getId)).forEach(comment -> {
            var commentMap = new HashMap<String, Object>();
            commentMap.put("user", comment.getUser().getName());
            commentMap.put("date", comment.getDate());
            commentMap.put("content", comment.getContent());
            comments.add(commentMap);
        });
        return comments;
    }

}
