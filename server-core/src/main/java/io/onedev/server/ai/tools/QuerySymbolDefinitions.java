package io.onedev.server.ai.tools;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.onedev.server.OneDev;
import io.onedev.server.ai.ChatTool;
import io.onedev.server.ai.ChatToolUtils;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.model.Project;
import io.onedev.server.search.code.CodeSearchService;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.SymbolQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import io.onedev.server.web.websocket.ChatToolExecution;
import io.onedev.server.web.websocket.ChatToolExecution.Result;

public abstract class QuerySymbolDefinitions implements ChatTool {

    private static final int PAGE_SIZE = 25;

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("querySymbolDefinitions")
            .description("Query definitions of specified symbol name pattern (supports wildcards * and ?) and return result in json format")
            .parameters(JsonObjectSchema.builder()            
                .addStringProperty("symbolNamePattern").description("Symbol name pattern to query definitions (supports wildcards * and ?)")
                .addBooleanProperty("caseSensitive").description("Whether to match symbol names case-sensitively")
                .addStringProperty("fileNamePatterns").description("Optionally specify file name patterns to query definitions in. Pattern supports wildcards * and ?. Multiple patterns should be separated by comma")
                .addBooleanProperty("oldRevision").description("Optionally specify whether to query definitions in old revision in a diff context")
                .addIntegerProperty("currentPage").description("Current page for the query. First page is 1")
                .required("symbolNamePattern", "caseSensitive", "currentPage").build())
                .build();
    }

    @Override
    public CompletableFuture<Result> execute(IPartialPageRequestHandler handler, JsonNode arguments) {
        var symbolNamePattern = arguments.get("symbolNamePattern").asText();
        var caseSensitive = arguments.get("caseSensitive").asBoolean();
        var fileNamePatterns = arguments.get("fileNamePatterns") != null ? arguments.get("fileNamePatterns").asText() : null;
        var oldRevision = arguments.get("oldRevision") != null ? arguments.get("oldRevision").asBoolean() : false;
        var currentPage = arguments.get("currentPage").asInt();

        try {
            BlobQuery query = new SymbolQuery.Builder(symbolNamePattern)
                    .caseSensitive(caseSensitive)
                    .fileNames(fileNamePatterns)
                    .local(false)
                    .count(currentPage * PAGE_SIZE + 1)
                    .build();
            var symbolHits = OneDev.getInstance(CodeSearchService.class).search(getProject(), getCommitId(oldRevision), query);
            int fromIndex = (currentPage - 1) * PAGE_SIZE;
            int toIndex = Math.min(currentPage * PAGE_SIZE, symbolHits.size());        
            var result = Map.of(	
                "successful", true,
                "definitions", symbolHits.subList(fromIndex, toIndex), 
                "hasMorePages", symbolHits.size() > toIndex);
            return completedFuture(new ChatToolExecution.Result(ChatToolUtils.convertToJson(result), false));
        } catch (RuntimeException e) {
            if (ExceptionUtils.find(e, TooGeneralQueryException.class) != null) {
                var resultData = Map.of(
                    "successful", false, 
                    "failReason", "Symbol name pattern is too short or too general, try a more specific one");
                return completedFuture(new ChatToolExecution.Result(ChatToolUtils.convertToJson(resultData), false));
            } else {
                throw e;
            }
        }
    } 

    protected abstract Project getProject();

    protected abstract ObjectId getCommitId(boolean oldRevision);

}