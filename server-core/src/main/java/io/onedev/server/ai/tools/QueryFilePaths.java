package io.onedev.server.ai.tools;

import static io.onedev.server.ai.ToolUtils.convertToJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.server.OneDev;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.search.code.CodeSearchService;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.FileQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;

public abstract class QueryFilePaths implements TaskTool {

    private static final int PAGE_SIZE = 25;
    
    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("queryFilePaths")
            .description("Query file paths by specified file name pattern (supports wildcards * and ?) and return result in json format")
            .parameters(JsonObjectSchema.builder()
                .addStringProperty("fileNamePattern").description("File name pattern to query paths (supports wildcards * and ?)")
                .addBooleanProperty("caseSensitive").description("Whether to match file names case-sensitively")
                .addIntegerProperty("currentPage").description("Current page for the query. First page is 1")
                .required("fileNamePattern", "caseSensitive", "currentPage")
                .build())
            .build();
    }

    @Override
    public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
        var project = OneDev.getInstance(ProjectService.class).load(getProjectId());
        if (!SecurityUtils.canReadCode(subject, project))
            throw new UnauthorizedException();

        if (arguments.get("fileNamePattern") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'fileNamePattern' is required")), false);
        var fileNamePattern = arguments.get("fileNamePattern").asText();
        if (arguments.get("caseSensitive") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'caseSensitive' is required")), false);
        var caseSensitive = arguments.get("caseSensitive").asBoolean();
        if (arguments.get("currentPage") == null)
            return new ToolExecutionResult(convertToJson(Map.of("successful", false, "failReason", "Argument 'currentPage' is required")), false);
        var currentPage = arguments.get("currentPage").asInt();

        try {
            BlobQuery query = new FileQuery.Builder(fileNamePattern)
                    .caseSensitive(caseSensitive)
                    .count(currentPage * PAGE_SIZE + 1)
                    .build();
            var fileHits = OneDev.getInstance(CodeSearchService.class).search(project, getCommitId(), query);
            int fromIndex = (currentPage - 1) * PAGE_SIZE;
            int toIndex = Math.min(currentPage * PAGE_SIZE, fileHits.size());

            List<String> filePaths = new ArrayList<>();
            for (var i=fromIndex; i<toIndex; i++) {
                filePaths.add(fileHits.get(i).getFilePath());
            }
            var resultData = Map.of(
                "successful", true,
                "filePaths", filePaths, 
                "hasMorePages", fileHits.size() > toIndex);
            return new ToolExecutionResult(convertToJson(resultData), false);
        } catch (RuntimeException e) {
            if (ExceptionUtils.find(e, TooGeneralQueryException.class) != null) {
                var resultData = Map.of(
                    "successful", false, 
                    "failReason", "File name pattern is too short or too general, try a more specific one");
                return new ToolExecutionResult(convertToJson(resultData), false);
            } else {
                throw e;
            }
        }
    }

    protected abstract Long getProjectId();

    protected abstract ObjectId getCommitId();

}
