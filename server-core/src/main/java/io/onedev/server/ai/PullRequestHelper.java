package io.onedev.server.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.exception.NotFoundException;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.DiffEntryFacade;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.UrlService;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.util.DiffPlanarRange;

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
        summary.put("lastActivityDate", pullRequest.getLastActivity().getDate());
        for (var it = summary.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            if (entry.getKey().endsWith("Count"))
                it.remove();
        }
        return summary;
    }

    public static Map<String, Object> getDetail(Project currentProject, PullRequest pullRequest) {
        var data = getSummary(currentProject, pullRequest, true);        
        data.put("headCommitHash", pullRequest.getLatestUpdate().getHeadCommitHash());
        data.put("assignees", pullRequest.getAssignees().stream().map(it->it.getName()).collect(Collectors.toList()));
        var reviews = new ArrayList<Map<String, Object>>();
        for (var review : pullRequest.getReviews()) {
            if (review.getStatus() == PullRequestReview.Status.EXCLUDED)
                continue;
            var reviewMap = new HashMap<String, Object>();
            reviewMap.put("reviewer", review.getUser().getName());
            reviewMap.put("status", review.getStatus());
            reviews.add(reviewMap);
        }        
        data.put("reviews", reviews);
        var builds = new ArrayList<String>();
        for (var build : pullRequest.getBuilds()) {
            builds.add(build.getReference().toString(currentProject) + " (job: " + build.getJobName() + ", status: " + build.getStatus() + ")");
        }
        data.put("builds", builds);
        data.put("labels", pullRequest.getLabels().stream().map(it->it.getSpec().getName()).collect(Collectors.toList()));
        data.put("link", getUrlService().urlFor(pullRequest, true));

        return data;
    }

    public static List<Map<String, Object>> getBuilds(Project currentProject, PullRequest pullRequest) {
        var builds = new ArrayList<Map<String, Object>>();
        pullRequest.getCurrentBuilds().stream()
            .forEach(it -> {
                var summary = BuildHelper.getSummary(currentProject, it);
                summary.put("link", getUrlService().urlFor(it, true));
                builds.add(summary);
        });
        return builds;
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

    public static CodeComment addCodeComment(PullRequest pullRequest, User user, String filePath,
            int fromLineNumber, int toLineNumber, String commentContent) {
        if (fromLineNumber <= 0)
            throw new NotAcceptableException("'fromLineNumber' must be greater than 0");
        if (fromLineNumber > toLineNumber)
            throw new NotAcceptableException("'fromLineNumber' must be less than or equal to 'toLineNumber'");

        var project = pullRequest.getProject();
        var pullRequestService = OneDev.getInstance(PullRequestService.class);
        var gitService = OneDev.getInstance(GitService.class);
        var codeCommentService = OneDev.getInstance(CodeCommentService.class);

        var oldCommitId = ObjectId.fromString(pullRequest.getBaseCommitHash());
        var newCommitId = ObjectId.fromString(pullRequest.getLatestUpdate().getHeadCommitHash());
        var comparisonBase = pullRequestService.getComparisonBase(pullRequest, oldCommitId, newCommitId);

        var newBlobIdent = new BlobIdent(newCommitId.name(), filePath, FileMode.REGULAR_FILE.getBits());
        var lines = project.readLines(newBlobIdent, WhitespaceOption.IGNORE_TRAILING, false);
        if (lines == null)
            throw new NotFoundException("File not found or not a text file in head commit: " + filePath);
        if (toLineNumber > lines.size())
            throw new NotAcceptableException("'toLineNumber' must not exceed number of lines in the file");

        DiffEntryFacade matchingEntry = null;
        for (var entry : gitService.diff(project, comparisonBase, newCommitId)) {
            if (filePath.equals(entry.getNewPath()) && entry.getChangeType() != ChangeType.DELETE) {
                matchingEntry = entry;
                break;
            }
        }
        if (matchingEntry == null)
            throw new NotAcceptableException("Cannot add code comment outside of diff: '" + filePath + "' is not changed");

        var changeType = matchingEntry.getChangeType();
        if (changeType == ChangeType.RENAME && matchingEntry.getOldPath().equals(matchingEntry.getNewPath()))
            changeType = ChangeType.MODIFY;
        var oldDiffBlobIdent = GitUtils.getOldBlobIdent(matchingEntry, comparisonBase.name());
        var newDiffBlobIdent = GitUtils.getNewBlobIdent(matchingEntry, newCommitId.name());
        var blobChange = new BlobChange(changeType, oldDiffBlobIdent, newDiffBlobIdent, WhitespaceOption.IGNORE_TRAILING) {

            private static final long serialVersionUID = 1L;

            @Override
            public Project getProject() {
                return project;
            }

        };

        var range = new PlanarRange(fromLineNumber - 1, 0, toLineNumber - 1, lines.get(toLineNumber - 1).length());
        if (!blobChange.isVisible(new DiffPlanarRange(false, range)))
            throw new NotAcceptableException("Cannot add code comment outside of diff");

        var comment = new CodeComment();
        comment.setProject(project);
        comment.setUser(user);
        comment.setContent(commentContent);

        var mark = new Mark();
        mark.setCommitHash(newCommitId.name());
        mark.setPath(filePath);
        mark.setRange(range);
        comment.setMark(mark);

        var compareContext = new CompareContext();
        compareContext.setPullRequest(pullRequest);
        compareContext.setOldCommitHash(oldCommitId.name());
        compareContext.setNewCommitHash(newCommitId.name());
        compareContext.setPathFilter(filePath);
        comment.setCompareContext(compareContext);

        codeCommentService.create(comment);
        return comment;
    }

    public static List<Map<String, Object>> getCodeComments(PullRequest pullRequest) {
        var project = pullRequest.getProject();
        var headCommitId = ObjectId.fromString(pullRequest.getLatestUpdate().getHeadCommitHash());
        Map<Pair<String, String>, Map<Integer, Integer>> lineMappingCache = new HashMap<>();

        var codeComments = new ArrayList<Map<String, Object>>();
        pullRequest.getCodeComments().stream()
                .sorted(Comparator.comparing(CodeComment::getId))
                .forEach(comment -> {
                    Mark mark = comment.getMark();
                    String commentCommitHash = mark.getCommitHash();
                    String path = mark.getPath();

                    PlanarRange mappedRange;
                    if (commentCommitHash.equals(headCommitId.name())) {
                        mappedRange = mark.getRange();
                    } else {
                        var key = new ImmutablePair<>(commentCommitHash, path);
                        var lineMapping = lineMappingCache.computeIfAbsent(key, k -> {
                            BlobIdent newBlobIdent = new BlobIdent(headCommitId.name(), path, FileMode.REGULAR_FILE.getBits());
                            List<String> newLines = project.readLines(newBlobIdent, WhitespaceOption.IGNORE_TRAILING, false);
                            if (newLines != null) {
                                BlobIdent oldBlobIdent = new BlobIdent(commentCommitHash, path, FileMode.REGULAR_FILE.getBits());
                                List<String> oldLines = project.readLines(oldBlobIdent, WhitespaceOption.IGNORE_TRAILING, false);
                                if (oldLines != null)
                                    return DiffUtils.mapLines(oldLines, newLines);
                            }
                            return new HashMap<Integer, Integer>();
                        });
                        mappedRange = DiffUtils.mapRange(lineMapping, mark.getRange());
                    }
                    if (mappedRange == null)
                        return;

                    var commentMap = new LinkedHashMap<String, Object>();
                    commentMap.put("id", comment.getId());
                    commentMap.put("filePath", path);
                    commentMap.put("startLine", mappedRange.getFromRow() + 1);
                    commentMap.put("endLine", mappedRange.getToRow() + 1);
                    commentMap.put("user", comment.getUser().getName());
                    commentMap.put("date", comment.getCreateDate());
                    commentMap.put("content", comment.getContent());
                    commentMap.put("resolved", comment.isResolved());

                    var replies = new ArrayList<Map<String, Object>>();
                    comment.getReplies().stream()
                            .sorted(Comparator.comparing(CodeCommentReply::getId))
                            .forEach(reply -> {
                                var replyMap = new LinkedHashMap<String, Object>();
                                replyMap.put("user", reply.getUser().getName());
                                replyMap.put("date", reply.getDate());
                                replyMap.put("content", reply.getContent());
                                replies.add(replyMap);
                            });
                    commentMap.put("replies", replies);

                    codeComments.add(commentMap);
                });
        return codeComments;
    }

}
