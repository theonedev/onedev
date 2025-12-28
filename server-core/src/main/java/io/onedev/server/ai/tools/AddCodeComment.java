package io.onedev.server.ai.tools;

import static io.onedev.server.ai.ToolUtils.convertToJson;

import java.util.Map;
import java.util.UUID;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.ai.ToolUtils;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestService;

/*
 * This tool is not used currently. To use it effectively, we need to add the ability to 
 * get existing code comments also. 
 */
public abstract class AddCodeComment implements TaskTool {
    
    private final boolean inDiffContext;

    public AddCodeComment(boolean inDiffContext) {
        this.inDiffContext = inDiffContext;
    }

    @Override
    public ToolSpecification getSpecification() {
        var paramsBuilder = JsonObjectSchema.builder().addStringProperty("comment").description("Comment to add");

        if (inDiffContext) 
            paramsBuilder.addBooleanProperty("oldRevision").description("Specify whether the code snippet being commented is from the old revision");
        
        paramsBuilder
            .addStringProperty("filePath").description("Path of the file containing the code snippet to comment")
            .addIntegerProperty("fromLineNumber").description("Start line number of the code snippet to comment")
            .addIntegerProperty("toLineNumber").description("End line number of the code snippet to comment")
            .required(ToolUtils.required(inDiffContext, "comment", "filePath", "fromLineNumber", "toLineNumber"));
        if (inDiffContext) 
            paramsBuilder.addBooleanProperty("oldRevision").description("Specify whether the code snippet being commented is from the old revision");
        return ToolSpecification.builder()
            .name("addCodeComment")
            .description("Add a comment to a code snippet")
            .parameters(paramsBuilder.build())
            .build();
    }

    @Override
    public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
        var project = OneDev.getInstance(ProjectService.class).load(getProjectId());
        var user = SecurityUtils.getUser(subject);
        if (!SecurityUtils.canReadCode(subject, project) || user == null)
            throw new UnauthorizedException();
        
        if (arguments.get("comment") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'comment' is required")), false);
        var content = arguments.get("comment").asText();
        if (inDiffContext && arguments.get("oldRevision") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'oldRevision' is required")), false);
        var oldRevision = arguments.get("oldRevision") != null ? arguments.get("oldRevision").asBoolean() : false;
        if (arguments.get("filePath") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'filePath' is required")), false);
        var filePath = arguments.get("filePath").asText();
        if (arguments.get("fromLineNumber") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'fromLineNumber' is required")), false);
        var fromLineNumber = arguments.get("fromLineNumber").asInt();
        if (arguments.get("toLineNumber") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'toLineNumber' is required")), false);
        var toLineNumber = arguments.get("toLineNumber").asInt();
        if (fromLineNumber > toLineNumber)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'fromLineNumber' must be less than or equal to 'toLineNumber'")), false);
        if (fromLineNumber <= 0)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'fromLineNumber' must be greater than 0")), false);

        var oldCommitId = getCommitId(true);
        var newCommitId = getCommitId(false);
        
        PullRequest request = null;
        ObjectId comparisonBase;
        if (getPullRequestId() != null) {
            var pullRequestService = OneDev.getInstance(PullRequestService.class);
            request = pullRequestService.load(getPullRequestId());
            comparisonBase = pullRequestService.getComparisonBase(request, oldCommitId, newCommitId);
        } else {
            comparisonBase = oldCommitId;
        }

        var comment = new CodeComment();
        comment.setUUID(UUID.randomUUID().toString());
        comment.setProject(project);
        comment.setUser(user);

        var commitHash = oldRevision? comparisonBase.name() : newCommitId.name();
        var blob = project.getBlob(new BlobIdent(commitHash, filePath), false);
        if (blob == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "File not found")), false);
        if (blob.getText() == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Not a text file")), false);
        var lines = blob.getText().getLines();
        if (toLineNumber > lines.size())
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'toLineNumber' must not exceed number of lines in the file")), false);
        var mark = new Mark();
        mark.setCommitHash(commitHash);
        mark.setPath(filePath);
        mark.setRange(new PlanarRange(fromLineNumber-1, 0, toLineNumber-1, lines.get(toLineNumber-1).length()));
        if (request == null) {
            /*
             * Outside of pull request, no one will be notified of the comment. So we automatically
             * mention authors of commented lines
             */
            StringBuilder mentions = new StringBuilder();
            for (var author: project.getAuthors(filePath, ObjectId.fromString(commitHash),
                    new LinearRange(mark.getRange().getFromRow(), mark.getRange().getToRow()))) {
                mentions.append("@").append(author.getName()).append(" ");
            }
            content = mentions.toString() + content;
        }
        comment.setContent(content);
        if (request != null) {
            mark = mark.permanentize(project, oldCommitId, newCommitId, comparisonBase);
            if (mark == null)
                return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Invalid patch section to comment on")), false);
        }
        comment.setMark(mark);

        var compareContext = new CompareContext();
        compareContext.setPullRequest(request);
        compareContext.setOldCommitHash(oldCommitId.name());
        compareContext.setNewCommitHash(newCommitId.name());
        comment.setCompareContext(compareContext);

        OneDev.getInstance(CodeCommentService.class).create(comment);
        return new ToolExecutionResult(convertToJson(Map.of("successful", true)), false);
    }
    
    protected abstract Long getProjectId();

    @Nullable
    protected abstract Long getPullRequestId();

    protected abstract ObjectId getCommitId(boolean oldRevision);

}
