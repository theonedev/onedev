package io.onedev.server.ai.tools;

import static io.onedev.server.ai.ChatToolUtils.convertToJson;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.server.OneDev;
import io.onedev.server.ai.ChatTool;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.model.Project;
import io.onedev.server.search.code.CodeSearchService;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.FileQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import io.onedev.server.web.websocket.ChatToolExecution;
import io.onedev.server.web.websocket.ChatToolExecution.Result;

public abstract class QueryFilePaths implements ChatTool {

    private static final int PAGE_SIZE = 25;
    
    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("queryFilePaths")
            .description("Query file paths by specified file name pattern (supports wildcards * and ?) and return result in json format")
            .parameters(JsonObjectSchema.builder()
                .addStringProperty("fileNamePattern").description("File name pattern to query paths (supports wildcards * and ?)")
                .addBooleanProperty("caseSensitive").description("Whether to match file names case-sensitively")
                .addBooleanProperty("oldRevision").description("Optionally specify whether to query file paths in old revision in a diff context")
                .addIntegerProperty("currentPage").description("Current page for the query. First page is 1")
                .required("fileNamePattern", "currentPage").build())
                .build();
    }

    @Override
    public CompletableFuture<Result> execute(IPartialPageRequestHandler handler, JsonNode arguments) {
        var fileNamePattern = arguments.get("fileNamePattern").asText();
        var caseSensitive = arguments.get("caseSensitive").asBoolean();
        var oldRevision = arguments.get("oldRevision") != null ? arguments.get("oldRevision").asBoolean() : false;
        var currentPage = arguments.get("currentPage").asInt();

        try {
            BlobQuery query = new FileQuery.Builder(fileNamePattern)
                    .caseSensitive(caseSensitive)
                    .count(currentPage * PAGE_SIZE + 1)
                    .build();
            var fileHits = OneDev.getInstance(CodeSearchService.class).search(getProject(), getCommitId(oldRevision), query);
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
            return completedFuture(new ChatToolExecution.Result(convertToJson(resultData), false));
        } catch (RuntimeException e) {
            if (ExceptionUtils.find(e, TooGeneralQueryException.class) != null) {
                var resultData = Map.of(
                    "successful", false, 
                    "failReason", "File name pattern is too short or too general, try a more specific one");
                return completedFuture(new ChatToolExecution.Result(convertToJson(resultData), false));
            } else {
                throw e;
            }
        }
    }

    protected abstract Project getProject();

    protected abstract ObjectId getCommitId(boolean oldRevision);

}
